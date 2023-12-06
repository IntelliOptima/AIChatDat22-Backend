package com.example.aichatprojectdat.chatroom.controller;

import com.example.aichatprojectdat.OpenAIModels.dall_e.service.IDALL_E3ServiceStandard;
import com.example.aichatprojectdat.OpenAIModels.gpt.service.interfaces.IGPT3Service;
import com.example.aichatprojectdat.OpenAIModels.gpt.service.interfaces.IGPT4Service;
import com.example.aichatprojectdat.message.model.ChunkData;
import com.example.aichatprojectdat.message.model.Message;

import com.example.aichatprojectdat.message.service.IMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Slf4j
@RestController
@RequiredArgsConstructor
public class NewChatroomController {

    private final IMessageService messageService;
    private final IGPT3Service gpt3Service;
    private final IGPT4Service gpt4Service;
    private final IDALL_E3ServiceStandard iDallE3ServiceStandard;

    @MessageMapping("chat.{chatroomId}")
    public Flux<ChunkData> handleMessages(@DestinationVariable String chatroomId, Flux<ChunkData> incomingChunkData) {
        log.info("user connected!");
        return incomingChunkData
                .groupBy(ChunkData::identifier) // Group by chunkIdentifier
                .flatMap(groupedFlux ->
                        groupedFlux.collectList() // Collect all ChunkData in the group into a list
                                .flatMapMany(chunkDataList -> {
                                    if (isLastChunkReceived(chunkDataList)) {
                                        if (isGptMessage(chunkDataList)) {
                                            return handleGptContextMessage(chatroomId, chunkDataList);
                                        } else if (isDalleMessage(chunkDataList)) {
                                            return handleDallEMessage(chatroomId, chunkDataList);
                                        } else {
                                            return Flux.fromIterable(chunkDataList)
                                                    .flatMap(chunkData -> processRegularMessage(chunkData.chunk()));
                                        }
                                    } else {
                                        // If the last chunk hasn't been received, continue collecting chunks
                                        return Flux.empty();
                                    }
                                })
                );
    }


    private Flux<ChunkData> handleGptContextMessage(String chatroomId, List<ChunkData> chunkDataList) {
        List<Message> messages = chunkDataList.stream()
                .map(ChunkData::chunk)
                .collect(Collectors.toList());

        log.info("The size of messages is {}", messages.size());

        // Extract the original message and emit it first
        Message originalMessage = chunkDataList.get(0).chunk();
        ChunkData originalChunkData = ChunkData.of(
                UUID.randomUUID().toString(),
                originalMessage,
                1L,
                null,
                true);

        // Save and then emit the original question message as a Flux
        Flux<ChunkData> originalMessageFlux = Flux.just(originalChunkData)
                .flatMap(chunkData -> messageService.create(chunkData.chunk())
                        .then(Mono.just(chunkData)));

        log.info("Handling GPT context message");
        String gptMessageId = UUID.randomUUID().toString();
        Instant createdDate = Instant.now();
        StringBuilder gptAnswer = new StringBuilder();

        Flux<ChunkData> gptResponseFlux = gpt3Service.streamChatContext(messages)
                .flatMap(chunk -> {
                    gptAnswer.append(chunk);
                    // Emit each chunk as a separate message
                    return Flux.just(
                            ChunkData.of(gptMessageId,
                                    Message.builder()
                                    .id(gptMessageId)
                                    .userId(1L)
                                    .textMessage(chunk)
                                    .chatroomId(chatroomId)
                                    .createdDate(createdDate)
                                    .build(),
                                    (long) gptAnswer.length(),
                                    null,
                                    false));
                })
                .concatWith(Mono.defer(() -> {
                    // Construct and emit the complete message after all chunks are processed
                    ChunkData gptCompleteChunkData = ChunkData.of(gptMessageId,
                            Message.builder()
                                    .id(gptMessageId)
                                    .userId(1L)
                                    .textMessage(gptAnswer.toString())
                                    .chatroomId(chatroomId)
                                    .createdDate(createdDate)
                                    .build(),
                            (long) gptAnswer.length(),
                            null,
                            false);

                    return messageService.create(gptCompleteChunkData.chunk()).thenReturn(gptCompleteChunkData);
                }));
        return Flux.concat(originalMessageFlux, gptResponseFlux);
    }

    private Flux<ChunkData> processRegularMessage(Message message) {
        // Process a regular chat message
        ChunkData chunkData = ChunkData.of(UUID.randomUUID().toString(), message, 1L, 1L,true);
        return messageService.create(chunkData.chunk()) // Assuming messageService.create returns a Mono<Message>
                .then(Mono.just(chunkData))
                .flux();
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
        Message chunk = chunkDataList.get(0).chunk();
        return chunk != null && chunk.getTextMessage().toLowerCase().startsWith("@gpt");
    }

    private boolean isDalleMessage(List<ChunkData> chunkDataList) {
        log.info(chunkDataList.toString());
        Message chunk = chunkDataList.get(0).chunk();
        return chunk != null && chunk.getTextMessage().toLowerCase().startsWith("@dalle");
    }

    private boolean isLastChunkReceived(List<ChunkData> chunkDataList) {
        return chunkDataList.stream().anyMatch(ChunkData::isLastChunk);
    }
}
