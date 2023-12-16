package com.example.aichatprojectdat.chatroom.controller;

import com.example.aichatprojectdat.OpenAIModels.dall_e.service.IDALL_E3ServiceStandard;
import com.example.aichatprojectdat.OpenAIModels.gpt.service.interfaces.IGPT3Service;
import com.example.aichatprojectdat.OpenAIModels.gpt.service.interfaces.IGPT4Service;
import com.example.aichatprojectdat.chatroom.model.ChatroomSink;
import com.example.aichatprojectdat.message.model.ChunkData;
import com.example.aichatprojectdat.message.model.Message;

import com.example.aichatprojectdat.message.service.IMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@Slf4j
@RestController
@RequiredArgsConstructor
public class NewChatroomController {

    private final IMessageService messageService;
    private final IGPT3Service gpt3Service;
    private final IGPT4Service gpt4Service;
    private final IDALL_E3ServiceStandard iDallE3ServiceStandard;


    private final Map<String, ChatroomSink> chatroomSinks = new ConcurrentHashMap<>();

    private ChatroomSink getOrCreateChatroomSink(String chatroomId) {
        return chatroomSinks.computeIfAbsent(chatroomId, id -> new ChatroomSink());
    }



    @MessageMapping("chat.{chatroomId}")
    public Flux<ChunkData> handleMessages(@DestinationVariable String chatroomId, Flux<ChunkData> incomingChunkData) {
        ChatroomSink chatroomSinkWrapper = getOrCreateChatroomSink(chatroomId);
        Sinks.Many<ChunkData> chatroomSink = chatroomSinkWrapper.getSink();
        log.info("User connected to chatroom {}", chatroomId);

        return incomingChunkData
                .groupBy(ChunkData::identifier) // Group by chunkIdentifier
                .flatMap(groupedFlux ->
                        groupedFlux
                                .collectList() // Collect all ChunkData in the group into a list
                                .flatMapMany(chunkDataList -> {
                                    if (chunkDataList.isEmpty()) {
                                        log.warn("Received empty chunk data list");
                                        return Flux.empty();
                                    }

                                    chunkDataList.sort(Comparator.comparing(ChunkData::startIndex));
                                    ChunkData lastChunk = chunkDataList.get(chunkDataList.size() - 1);

                                    if (isGptMessage(chunkDataList) && isLastChunkReceived(chunkDataList)) {
                                        return handleGptContextMessage(chatroomId, chunkDataList);
                                    } else if (isDalleMessage(chunkDataList)) {
                                        return handleDallEMessage(chatroomId, chunkDataList);
                                    } else {
                                        log.info("Handling regular message");
                                        return processRegularMessage(lastChunk.chunk());
                                    }
                                })
                )
                .doOnNext(chatroomSink::tryEmitNext)
                .publishOn(Schedulers.boundedElastic());
    }

    private Flux<ChunkData> processRegularMessage(Message message) {
        // Process a regular chat message
        log.info("Processing regular message: {}", message);
        ChunkData chunkData = ChunkData.of(UUID.randomUUID().toString(), message, 1L, 1L, true);

        return messageService.create(chunkData.chunk()) // Assuming messageService.create returns a Mono<Message>
                .flatMap(createdMessage -> Mono.just(chunkData))
                .flux();
    }


    private Flux<ChunkData> handleGptContextMessage(String chatroomId, List<ChunkData> chunkDataList) {
        List<Message> messages = chunkDataList.stream()
                .map(ChunkData::chunk)
                .collect(Collectors.toList());

        log.info("The size of messages is {}", messages.size());

        // Extract the original message
        Message originalMessage = chunkDataList.get(chunkDataList.size() - 1).chunk();
        Instant originalMessageCreated = Instant.now();
        originalMessage.setCreatedDate(originalMessageCreated);
        originalMessage.setLastModifiedDate(originalMessageCreated);

        log.info("Processing original message: {} ", originalMessage);

        String gptMessageId = UUID.randomUUID().toString();
        Instant createdDate = Instant.now();
        StringBuilder gptAnswer = new StringBuilder();

        // Save and then emit the original message
        return messageService.create(originalMessage)
                .thenMany(Flux.just(ChunkData.of(
                        UUID.randomUUID().toString(),
                        originalMessage,
                        1L,
                        null,
                        true)))
                .concatWith(
                        gpt3Service.streamChatContext(messages)
                                .flatMap(chunk -> {
                                    gptAnswer.append(chunk);
                                    // Emit each chunk as a separate message
                                    return Flux.just(
                                            ChunkData.of(UUID.randomUUID().toString(),
                                                    Message.builder()
                                                            .id(gptMessageId)
                                                            .userId(1L)
                                                            .textMessage(chunk)
                                                            .chatroomId(chatroomId)
                                                            .createdDate(Instant.now())
                                                            .build(),
                                                    (long) chunk.length(),
                                                    null,
                                                    false));
                                }))
                .publishOn(Schedulers.boundedElastic())
                .doOnComplete(() -> {
                    // Save the complete message to the database after all chunks are processed
                    Message completeMessage = Message.builder()
                            .id(gptMessageId)
                            .userId(1L)
                            .textMessage(gptAnswer.toString())
                            .chatroomId(chatroomId)
                            .createdDate(createdDate)
                            .build();

                    messageService.create(completeMessage).subscribe();
                });
    }


    private Flux<ChunkData> handleDallEMessage(String chatroomId, List<ChunkData> chunkData) {
        // Logic to handle Dall-E message
        String dalleCommand = chunkData.get(0).chunk().getTextMessage().split("@dalle ")[1];
        String chunkDataIdentifier = UUID.randomUUID().toString();
        return iDallE3ServiceStandard.generateImage(dalleCommand)
                .flatMap(dalleResponse -> {
                    Instant createdTime = Instant.now();

                    Message dalleMessage = Message.builder()
                            .userId(2L) // Assuming 2L is the ID for Dall-E responses
                            .textMessage(dalleResponse.getImageList().get(0).getUrl())
                            .chatroomId(chatroomId)
                            .createdDate(createdTime)
                            .lastModifiedDate(createdTime)
                            .build();

                    ChunkData dalleChunkData = ChunkData.of(
                            chunkDataIdentifier,
                            dalleMessage,
                            (long) dalleMessage.getTextMessage().length(),
                            null,
                            true
                    );

                    // Save the generated message and return it
                    return messageService.create(dalleMessage).thenReturn(dalleChunkData);
                });
    }


    private boolean isGptMessage(List<ChunkData> chunkDataList) {
        log.info(chunkDataList.toString());
        Message chunk = chunkDataList.get(chunkDataList.size() - 1).chunk();
        return chunk != null && chunk.getTextMessage().toLowerCase().startsWith("@gpt");
    }

    private boolean isDalleMessage(List<ChunkData> chunkDataList) {
        log.info(chunkDataList.toString());
        Message chunk = chunkDataList.get(chunkDataList.size() - 1).chunk();
        return chunk != null && chunk.getTextMessage().toLowerCase().startsWith("@dalle");
    }

    private boolean isLastChunkReceived(List<ChunkData> chunkDataList) {
        return chunkDataList.stream().anyMatch(ChunkData::isLastChunk);
    }



    // Sink logic:
    //@Scheduled(fixedDelay = 60000) // Run every 60 seconds, for example
    public void cleanUpInactiveChatrooms() {
        chatroomSinks.entrySet().removeIf(entry -> {
            String chatroomId = entry.getKey();
            ChatroomSink chatroomSink = entry.getValue();

            if (chatroomSink.hasSubscribers()) {
                // Additional cleanup logic if needed
                return true; // Remove the chatroom as it's inactive
            }
            return false;
        });
    }
}
