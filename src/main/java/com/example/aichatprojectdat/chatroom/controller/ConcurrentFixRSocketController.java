package com.example.aichatprojectdat.chatroom.controller;

import com.example.aichatprojectdat.OpenAIModels.dall_e.model.generation.ImageGenerationRequest;
import com.example.aichatprojectdat.OpenAIModels.dall_e.service.IDALL_E3ServiceStandard;
import com.example.aichatprojectdat.OpenAIModels.gpt.service.interfaces.IGPT3Service;
import com.example.aichatprojectdat.OpenAIModels.gpt.service.interfaces.IGPT4Service;
import com.example.aichatprojectdat.message.model.ChunkData;
import com.example.aichatprojectdat.message.model.Message;
import com.example.aichatprojectdat.message.model.ReadReceipt;
import com.example.aichatprojectdat.message.service.IMessageService;
import com.example.aichatprojectdat.message.service.IReadReceiptService;
import com.example.aichatprojectdat.utilities.ReactiveWebsocketMethods;
import io.rsocket.internal.jctools.queues.MessagePassingQueue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
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
        String connectionId = UUID.randomUUID().toString(); // Generate a unique ID for this connection
        connectionToChatroomMap.put(connectionId, chatroomId); // Map the connection ID to the chatroom ID

        return requestMessage.doOnNext(s -> log.info("Received message text: " + s))
                .thenMany(chatroomSinks.computeIfAbsent(chatroomId, id ->
                                Sinks.many()
                                        .replay()
                                        .latest())
                        .asFlux().onBackpressureBuffer())
                .doOnCancel(() -> {
                    // Handle the cancellation event here
                    log.info("Connection closed for chatroom: " + chatroomId);
                    // Remove the sink associated with this connection
                    connectionToChatroomMap.get(connectionId);
                    chatroomSinks.remove(chatroomId);
                    connectionToChatroomMap.remove(connectionId);
                });
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

            return processRegularMessage(sortedChunks.get(sortedChunks.size() - 1), sink)
                    .then(Mono.defer(() -> handleDallEMessage(sortedChunks.get(0).chunk(), sink)));
        } else {
            log.info("Handling regular message: " + sortedChunks.get(0));
            return processRegularMessage(sortedChunks.get(0), sink);
        }
    }

    private Mono<Void> handleGptContextMessage(String chatroomId, List<ChunkData> chunkDataList, Sinks.Many<ChunkData> sink) {
        List<Message> messages = chunkDataList.stream()
                .map(ChunkData::chunk)
                .collect(Collectors.toList());

        Message originalMessage = chunkDataList.get(chunkDataList.size() - 1).chunk();
        Instant now = Instant.now();
        originalMessage.setCreatedDate(now);
        originalMessage.setLastModifiedDate(now);

        String gptMessageId = UUID.randomUUID().toString();
        StringBuilder gptAnswer = new StringBuilder();
        Instant gptCreatedAnswer = now.plusSeconds(5);

        Flux<ChunkData> gptResponseStream = gpt3Service.streamChatContext(messages)
                .map(chunk -> {
                    gptAnswer.append(chunk);
                    Message newMessage = Message.builder()
                            .id(gptMessageId)
                            .userId(1L)
                            .textMessage(chunk)
                            .chatroomId(chatroomId)
                            .createdDate(gptCreatedAnswer)
                            .lastModifiedDate(gptCreatedAnswer)
                            .build();

                    ChunkData newChunk = ChunkData.of(gptMessageId, newMessage, (long) chunk.length(), null, false);
                    sink.emitNext(newChunk, Sinks.EmitFailureHandler.FAIL_FAST);
                    return newChunk;
                });

        return gptResponseStream
                .publishOn(Schedulers.boundedElastic())
                .doOnComplete(() -> {
                    Message completeMessage = Message.builder()
                            .id(gptMessageId)
                            .userId(1L)
                            .textMessage(gptAnswer.toString())
                            .chatroomId(chatroomId)
                            .createdDate(gptCreatedAnswer)
                            .build();

                    messageService.create(completeMessage).subscribe();
                })
                .then();
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

    private Mono<Void> processRegularMessage(ChunkData messageChunk, Sinks.Many<ChunkData> sink ) {
        log.info("Message chunk to be processed {}", messageChunk);
        return emitReceivedMessage(messageChunk.chunk(), sink);
    }

    public Mono<Void> emitReceivedMessage(Message chatMessage, Sinks.Many<ChunkData> sink) {

        Message receivedMessage = Message.builder()
                .id(chatMessage.getId())
                .userId(chatMessage.getUserId())
                .textMessage(chatMessage.getTextMessage())
                .chatroomId(chatMessage.getChatroomId())
                .build();

        messageService.create(receivedMessage)
                .flatMap(savedMessage -> {
                    ReadReceipt newMessageReceipt = ReadReceipt.of(savedMessage.getId(), savedMessage.getUserId(), true);
                    return readReceiptService.createReadReceipt(newMessageReceipt)
                            .thenReturn(savedMessage); // Chain read receipt creation in the reactive flow
                })
                .subscribe(savedMessage -> {
                    log.info("Read receipt created for message {}", savedMessage);

                    Message messageWithReceipt = Message.builder()
                            .id(savedMessage.getId())
                            .userId(savedMessage.getUserId())
                            .textMessage(savedMessage.getTextMessage())
                            .chatroomId(savedMessage.getChatroomId())
                            .readReceipt(Map.of(savedMessage.getUserId(), true))
                            .createdDate(savedMessage.getCreatedDate())
                            .lastModifiedDate(savedMessage.getLastModifiedDate())
                            .version(savedMessage.getVersion())
                            .build();

                    ChunkData messageChunkData = ChunkData.of(UUID.randomUUID().toString(), messageWithReceipt, 0L, 1L, true);

                    sink.emitNext(messageChunkData, Sinks.EmitFailureHandler.FAIL_FAST);
                }, error -> {
                    log.error("Error creating read receipt", error);
                });

        log.info("Emitting message {}", chatMessage);
        return Mono.empty();
    }

}
