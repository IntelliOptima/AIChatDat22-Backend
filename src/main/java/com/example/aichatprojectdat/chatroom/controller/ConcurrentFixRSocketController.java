package com.example.aichatprojectdat.chatroom.controller;

import com.example.aichatprojectdat.ai_models.OpenAIModels.dall_e.model.generation.ImageGenerationRequest;
import com.example.aichatprojectdat.ai_models.OpenAIModels.dall_e.service.IDALL_E3ServiceStandard;
import com.example.aichatprojectdat.ai_models.OpenAIModels.gpt.service.interfaces.IGPT3Service;
import com.example.aichatprojectdat.ai_models.OpenAIModels.gpt.service.interfaces.IGPT4Service;
import com.example.aichatprojectdat.ai_models.google_ai_models.gemini.service.interfaces.IGeminiService;
import com.example.aichatprojectdat.ai_models.stability_ai_models.stablediffusion.models.text_to_image.JSONStructureRequest.StableDiffusionTextToImageRequest;
import com.example.aichatprojectdat.ai_models.stability_ai_models.stablediffusion.models.text_to_image.StableDiffusionTextToImageEngineList;
import com.example.aichatprojectdat.ai_models.stability_ai_models.stablediffusion.service.interfaces.IStableDiffusionService;
import com.example.aichatprojectdat.message.model.ChunkData;
import com.example.aichatprojectdat.message.model.Message;
import com.example.aichatprojectdat.message.model.ReadReceipt;
import com.example.aichatprojectdat.message.service.IMessageService;
import com.example.aichatprojectdat.message.service.IReadReceiptService;
import com.example.aichatprojectdat.utilities.ReactiveWebsocketMethods;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@CrossOrigin
public class ConcurrentFixRSocketController {

    private final IMessageService messageService;
    private final IReadReceiptService readReceiptService;
    private final IGPT3Service gpt3Service;
    private final IGPT4Service gpt4Service;
    private final IDALL_E3ServiceStandard iDallE3ServiceStandard;
    private final IGeminiService geminiService;
    private final IStableDiffusionService stableDiffusionService;
    private final ReactiveWebsocketMethods utilityMethods;


    private final Map<String, Sinks.Many<ChunkData>> chatroomSinks = new ConcurrentHashMap<>();
    private final Map<String, String> connectionToChatroomMap = new ConcurrentHashMap<>();
    private final Map<String, List<ChunkData>> chunkStream = new ConcurrentHashMap<>();

    // Map to keep track of connected users in each chatroom
    private final Map<String, Set<Long>> onlineUsers = new ConcurrentHashMap<>();

    // Map to hold and multicast updates about online users
    private final Map<String, Sinks.Many<Long>> userSinks = new ConcurrentHashMap<>();

    @MessageMapping("chat.stream.{chatroomId}")
    public Flux<ChunkData> streamMessages(
            @DestinationVariable String chatroomId,
            Mono<String> requestMessage
    ) {
        AtomicReference<String> messageUserId = new AtomicReference<>();

        return requestMessage.doOnNext(userId -> {
                    log.info("User with id: " + userId + " has connected!");
                    messageUserId.set(userId);
                    connectionToChatroomMap.put(userId, chatroomId);
                })
                .thenMany(chatroomSinks.computeIfAbsent(chatroomId, id ->
                                Sinks.many()
                                        .replay()
                                        .latest())
                        .asFlux().onBackpressureBuffer())
                .doOnCancel(() -> {
                    // Handle the cancellation event here
                    log.info("Connection closed for chatroom: " + chatroomId);
                    // Remove the sink associated with this connection
                    connectionToChatroomMap.get(messageUserId.get());
                    chatroomSinks.remove(chatroomId);
                    connectionToChatroomMap.remove(messageUserId.get());
                });
    }

    @MessageMapping("chat.send.stableDiffusion.{chatroomId}")
    public Mono<Void> receiveStableDiffusionMessage(
            @DestinationVariable String chatroomId,
            ChunkData chunkData,
            StableDiffusionTextToImageRequest request) {

        Sinks.Many<ChunkData> sink = chatroomSinks.computeIfAbsent(chatroomId, id ->
                Sinks.many()
                        .multicast()
                        .onBackpressureBuffer());

        log.info("Received StableDiffusion Request");

        return processRegularMessage(chunkData, sink)
                .then(Mono.defer(() -> handleStableDiffusionRequest(request, chunkData.chunk(), sink)));
    }

    @MessageMapping("chat.send.{chatroomId}")
    public Mono<Void> receiveMessage(@DestinationVariable String chatroomId, ChunkData chunkData) {
        log.info("Received message: {}", chunkData.chunk().getTextMessage());

        // Store chunk in temporary storage
        chunkStream.computeIfAbsent(chunkData.identifier(), k -> new ArrayList<>()).add(chunkData);

        if (utilityMethods.isCompleteMessage(chunkData.identifier(), chunkStream)) {
            List<ChunkData> completeChunks = chunkStream.remove(chunkData.identifier());
            return processCompleteMessage(completeChunks, chatroomId);
        }

        return Mono.empty();
    }



    private Mono<Void> processCompleteMessage(List<ChunkData> chunks, String chatroomId) {
        Sinks.Many<ChunkData> sink = chatroomSinks.computeIfAbsent(chatroomId, id ->
                Sinks.many()
                        .multicast()
                        .onBackpressureBuffer());


        // Sort the chunks by startIndex to ensure they are in the correct order
        List<ChunkData> sortedChunks = chunks.stream()
                .sorted(Comparator.comparing(ChunkData::startIndex))
                .toList();

        if (utilityMethods.isGptMessage(sortedChunks) && utilityMethods.isLastChunkReceived(sortedChunks)) {
            log.info("Handling GPT message!");

            return processRegularMessage(sortedChunks.get(sortedChunks.size() - 1), sink)
                    .then(Mono.defer(() -> handleGptContextMessage(chatroomId, sortedChunks, sink)));

        } else if (utilityMethods.isDalleMessage(sortedChunks)) {
            log.info("Handling DALL-E 3 message");

            return processRegularMessage(sortedChunks.get(sortedChunks.size() - 1), sink)
                    .then(Mono.defer(() -> handleDallEMessage(sortedChunks.get(sortedChunks.size() - 1).chunk(), sink)));
        } else if (utilityMethods.isGeminiMessage(sortedChunks)) {
            log.info("Handling Gemini message");

            return processRegularMessage(sortedChunks.get(sortedChunks.size() - 1), sink)
                    .then(Mono.defer(() -> handleGeminiContextMessage(chatroomId, sortedChunks, sink)));
        } else {
            log.info("Handling regular message: " + sortedChunks.get(0));
            return processRegularMessage(sortedChunks.get(0), sink);
        }
    }

    private Mono<Void> handleGptContextMessage(String chatroomId, List<ChunkData> chunkDataList, Sinks.Many<ChunkData> sink) {
        List<Message> messages = chunkDataList.stream()
                .map(ChunkData::chunk)
                .collect(Collectors.toList());

        String gptMessageId = UUID.randomUUID().toString();
        StringBuilder gptAnswer = new StringBuilder();
        Instant gptCreatedAnswer = Instant.now().plusSeconds(5);

        Flux<ChunkData> gptResponseStream = gpt3Service.streamChatContext(messages)
                .map(chunk -> {
                    synchronized (gptAnswer) {
                        gptAnswer.append(chunk);
                    }
                    Message newMessage = Message.builder()
                            .id(gptMessageId)
                            .userId(1L)
                            .textMessage(chunk)
                            .chatroomId(chatroomId)
                            .createdDate(gptCreatedAnswer)
                            .lastModifiedDate(gptCreatedAnswer)
                            .build();

                    return ChunkData.of(gptMessageId, newMessage, (long) chunk.length(), null, false);
                })
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(sink::tryEmitNext) // Use tryEmitNext for backpressure handling
                .doOnError(error -> {
                    // Log and handle error
                    System.err.println("Error in processing stream: " + error.getMessage());
                })
                .doOnComplete(() -> {
                    Message completeMessage = Message.builder()
                            .id(gptMessageId)
                            .userId(1L)
                            .textMessage(gptAnswer.toString())
                            .chatroomId(chatroomId)
                            .createdDate(gptCreatedAnswer)
                            .build();

                    messageService.create(completeMessage).subscribe();
                });

        return gptResponseStream
                .then()
                .doOnError(error -> {
                    // Log and handle error
                    System.err.println("Error in finalizing stream: " + error.getMessage());
                });
    }

    public Mono<Void> handleGeminiContextMessage(String chatroomId, List<ChunkData> chunkDataList, Sinks.Many<ChunkData> sink) {
        List<Message> messages = chunkDataList.stream()
                .map(ChunkData::chunk)
                .collect(Collectors.toList());

        String geminiMessageId = UUID.randomUUID().toString();
        StringBuilder geminiAnswer = new StringBuilder();
        Instant geminiCreatedAnswer = Instant.now().plusSeconds(5);

        Flux<ChunkData> geminiResponseStream = geminiService.streamChatContext(messages)
                .map(chunk -> {
                    synchronized (geminiAnswer) {
                        geminiAnswer.append(chunk);
                    }
                    Message newMessage = Message.builder()
                            .id(geminiMessageId)
                            .userId(3L)
                            .textMessage(chunk)
                            .chatroomId(chatroomId)
                            .createdDate(geminiCreatedAnswer)
                            .lastModifiedDate(geminiCreatedAnswer)
                            .build();

                    return ChunkData.of(geminiMessageId, newMessage, (long) chunk.length(), null, false);
                })
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(sink::tryEmitNext) // Use tryEmitNext for backpressure handling
                .doOnError(error -> {
                    // Log and handle error
                    System.err.println("Error in processing stream: " + error.getMessage());
                })
                .doOnComplete(() -> {
                    Message completeMessage = Message.builder()
                            .id(geminiMessageId)
                            .userId(1L)
                            .textMessage(geminiAnswer.toString())
                            .chatroomId(chatroomId)
                            .createdDate(geminiCreatedAnswer)
                            .build();

                    messageService.create(completeMessage).subscribe();
                });

        return geminiResponseStream
                .then()
                .doOnError(error -> {
                    // Log and handle error
                    System.err.println("Error in finalizing stream: " + error.getMessage());
                });
    }


    public Mono<Void> handleDallEMessage(Message chatMessage, Sinks.Many<ChunkData> sink) {

        AtomicReference<Message> dalleMessageRef = new AtomicReference<>(); // AtomicReference to hold the dalleMessage

        return iDallE3ServiceStandard.generateImage(chatMessage.getTextMessage().split("@dalle ")[1])
                .doFirst(() -> System.out.println(ImageGenerationRequest.of(chatMessage.getTextMessage()).toString()))
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(response -> {
                    Instant createdTime = Instant.now();
                    Message dalleMessage = Message.builder()
                            .userId(2L)
                            .textMessage(response.getImageList().get(0).getUrl())
                            .chatroomId(chatMessage.getChatroomId())
                            .createdDate(createdTime)
                            .lastModifiedDate(createdTime)
                            .build();
                    ChunkData dalleChunkData = ChunkData.of(UUID.randomUUID().toString(), dalleMessage, 0L, 1L, true);
                    dalleMessageRef.set(dalleChunkData.chunk()); // Set the dalleMessage in the AtomicReference

                    sink.emitNext(dalleChunkData, Sinks.EmitFailureHandler.FAIL_FAST);
                })
                .doFinally(signalType -> {
                    Message messageToSave = dalleMessageRef.get(); // Retrieve the dalleMessage
                    if (messageToSave != null) {
                        messageService.create(messageToSave).subscribe(); // Save the message
                    }
                }).then();
    }

    @GetMapping("/v1/engines/list")
    public Mono<List<StableDiffusionTextToImageEngineList>> getList() {
        return stableDiffusionService.getList();
    }
    public Mono<Void> handleStableDiffusionRequest(StableDiffusionTextToImageRequest request, Message chatMessage, Sinks.Many<ChunkData> sink) {
        String engineModel = utilityMethods.extractModelId(chatMessage.getTextMessage());
        AtomicReference<Message> stableDiffusionMessageRef = new AtomicReference<>(); // AtomicReference to hold the stableDiffusionMessage

        return stableDiffusionService.generateTextToImages(engineModel, request)
                .doFirst(() -> System.out.println(request))
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(response -> {
                    Instant createdTime = Instant.now();
                    Message stableDiffusionMessage = Message.builder()
                            .userId(4L)
                            .textMessage(response)
                            .chatroomId(chatMessage.getChatroomId())
                            .createdDate(createdTime)
                            .lastModifiedDate(createdTime)
                            .build();
                    ChunkData stableDiffusionChunkData = ChunkData.of(UUID.randomUUID().toString(), stableDiffusionMessage, 0L, 1L, true);
                    stableDiffusionMessageRef.set(stableDiffusionChunkData.chunk());

                    sink.emitNext(stableDiffusionChunkData, Sinks.EmitFailureHandler.FAIL_FAST);
                })
                .doFinally(signalType -> {
                    Message messageToSave = stableDiffusionMessageRef.get();
                    if (messageToSave != null) {
                        messageService.create(messageToSave).subscribe();
                    }
                }).then();
    }

    private Mono<Void> processRegularMessage(ChunkData messageChunk, Sinks.Many<ChunkData> sink ) {
        log.info("Message chunk to be processed {}", messageChunk);
        return emitReceivedMessage(messageChunk.chunk(), sink);
    }

    public Mono<Void> emitReceivedMessage(Message chatMessage, Sinks.Many<ChunkData> sink) {

        Instant createdDate = Instant.now();
        Message receivedMessage = Message.builder()
                .id(chatMessage.getId())
                .userId(chatMessage.getUserId())
                .textMessage(chatMessage.getTextMessage())
                .createdDate(createdDate)
                .readReceipt(Map.of(chatMessage.getUserId(), true))
                .lastModifiedDate(createdDate)
                .chatroomId(chatMessage.getChatroomId())
                .build();

        ChunkData messageChunkData = ChunkData.of(UUID.randomUUID().toString(), receivedMessage, 0L, 1L, true);
        sink.emitNext(messageChunkData, Sinks.EmitFailureHandler.FAIL_FAST);

        return messageService.create(receivedMessage)
                .flatMap(savedMessage -> {
                    ReadReceipt newMessageReceipt = ReadReceipt.of(savedMessage.getId(), savedMessage.getUserId(), true);
                    return readReceiptService.createReadReceipt(newMessageReceipt)
                            .thenReturn(savedMessage); // Chain read receipt creation in the reactive flow
                }).then();
    }

}
