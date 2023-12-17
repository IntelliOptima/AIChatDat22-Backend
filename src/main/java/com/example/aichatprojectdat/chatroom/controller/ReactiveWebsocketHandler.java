package com.example.aichatprojectdat.chatroom.controller;

import com.example.aichatprojectdat.OpenAIModels.dall_e.service.IDALL_E3ServiceStandard;
import com.example.aichatprojectdat.OpenAIModels.gpt.service.interfaces.IGPT3Service;
import com.example.aichatprojectdat.OpenAIModels.gpt.service.interfaces.IGPT4Service;
import com.example.aichatprojectdat.chatroom.model.ChatroomSink;
import com.example.aichatprojectdat.message.model.ChunkData;
import com.example.aichatprojectdat.message.model.Message;
import com.example.aichatprojectdat.message.service.IMessageService;
import com.example.aichatprojectdat.utilities.ReactiveWebsocketMethods;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component("ReactiveWebSocketHandler")
@Slf4j
public class ReactiveWebsocketHandler implements WebSocketHandler {

    private final IMessageService messageService;
    private final IGPT3Service gpt3Service;
    private final IGPT4Service gpt4Service;
    private final IDALL_E3ServiceStandard iDallE3ServiceStandard;
    private final ReactiveWebsocketMethods reactiveWebsocketMethods;
    private final ObjectMapper jsonMapper;
    private final Map<String, ChatroomSink> chatroomSinks = new ConcurrentHashMap<>();
    private final Map<String, List<ChunkData>> chunkStream = new ConcurrentHashMap<>();

    public ReactiveWebsocketHandler (
            @Qualifier("jsonObjectMapperUtil") ObjectMapper objectMapper,
            IMessageService messageService,
            IGPT3Service gpt3Service,
            IGPT4Service gpt4Service,
            IDALL_E3ServiceStandard idallE3ServiceStandard,
            ReactiveWebsocketMethods reactiveWebsocketMethods) {
        this.messageService =messageService;
        this.gpt3Service = gpt3Service;
        this.gpt4Service = gpt4Service;
        this.iDallE3ServiceStandard = idallE3ServiceStandard;
        this.jsonMapper = objectMapper;
        this.reactiveWebsocketMethods = reactiveWebsocketMethods;
    }
    @Override
    public @NotNull Mono<Void> handle(WebSocketSession session) {
        String uriPath = session.getHandshakeInfo().getUri().getPath();

        return session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .flatMap(messageAsString -> {
                    String[] splitMessage = messageAsString.split(":", 2);
                    String messageType = splitMessage[0];
                    String payload = splitMessage.length > 1 ? splitMessage[1] : "";

                    System.out.println("THIS IS PAYLOAD: " + payload);
                    System.out.println("This is size of chatroomSinks: " + chatroomSinks.size());

                    return switch (messageType) {
                        case "SUBSCRIBE" ->
                                handleSubscription(session, payload, reactiveWebsocketMethods.extractChatroomId(uriPath));
                        case "MESSAGE" -> Mono.defer(() -> {
                            ChunkData chunk = reactiveWebsocketMethods.convertToChunkData(payload);

                            // Store chunk in temporary storage
                            chunkStream.computeIfAbsent(chunk.identifier(), k -> new ArrayList<>()).add(chunk);

                            // Check if all chunks for this identifier have been received
                            if (reactiveWebsocketMethods.isCompleteMessage(chunk.identifier(), chunkStream)) {
                                List<ChunkData> completeChunks = chunkStream.remove(chunk.identifier());
                                return processCompleteMessage(completeChunks, reactiveWebsocketMethods.extractChatroomId(uriPath));
                            }
                            return Mono.empty();
                        });
                        case "CLOSE" ->
                                handleUnsubscription(session, payload, reactiveWebsocketMethods.extractChatroomId(uriPath));
                        default -> Mono.error(new RuntimeException("Unknown message type"));
                    };
                })
                .then();
    }


    private Mono<Void> processCompleteMessage(List<ChunkData> chunks, String chatroomId) {
        // Sort the chunks by startIndex to ensure they are in the correct order
        List<ChunkData> sortedChunks = chunks.stream()
                .sorted(Comparator.comparing(ChunkData::startIndex))
                .toList();

        if (reactiveWebsocketMethods.isGptMessage(sortedChunks) && reactiveWebsocketMethods.isLastChunkReceived(sortedChunks)) {
            log.info("Handling GPT message!");
             return handleGptContextMessage(chatroomId, sortedChunks);
        } else if (reactiveWebsocketMethods.isDalleMessage(sortedChunks)) {
            return handleDallEMessage(chatroomId, sortedChunks);
        } else {
            log.info("Handling regular message: " + sortedChunks.get(0));
            return processRegularMessage(sortedChunks.get(0), chatroomId);
        }
    }


    private Mono<Void> handleGptContextMessage(String chatroomId, List<ChunkData> chunkDataList) {
        List<Message> messages = chunkDataList.stream()
                .map(ChunkData::chunk)
                .collect(Collectors.toList());

        // Extract the original message
        Message originalMessage = chunkDataList.get(chunkDataList.size() - 1).chunk();
        Instant now = Instant.now();
        originalMessage.setCreatedDate(now);
        originalMessage.setLastModifiedDate(now);

        String gptMessageId = UUID.randomUUID().toString();
        StringBuilder gptAnswer = new StringBuilder();

        // Process the original message
        Mono<Void> processOriginal = processRegularMessage(ChunkData.of(UUID.randomUUID().toString(), originalMessage, 0L, 1L, true), chatroomId);

        // Stream for processing GPT-3 responses
        Flux<ChunkData> gptResponseStream = gpt3Service.streamChatContext(messages)
                .map(chunk -> {
                    gptAnswer.append(chunk);
                    Message newMessage = Message.builder()
                            .id(gptMessageId)
                            .userId(1L)
                            .textMessage(chunk)
                            .chatroomId(chatroomId)
                            .createdDate(Instant.now())
                            .build();

                    ChunkData newChunk = ChunkData.of(gptMessageId, newMessage, (long) chunk.length(), null, false);
                    broadcastMessage(newChunk, chatroomId);

                    return newChunk;
                });

        return processOriginal
                .thenMany(gptResponseStream)
                .publishOn(Schedulers.boundedElastic())
                .doOnComplete(() -> {
                    Message completeMessage = Message.builder()
                            .id(gptMessageId)
                            .userId(1L)
                            .textMessage(gptAnswer.toString())
                            .chatroomId(chatroomId)
                            .createdDate(now)
                            .build();

                    messageService.create(completeMessage).subscribe();
                })
                .then();
    }



    private Mono<Void> handleDallEMessage(String chatroomId, List<ChunkData> chunkData) {
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
                    return messageService.create(dalleMessage)
                            .flatMap(message -> {
                                broadcastMessage(dalleChunkData, message.getChatroomId());
                                return Mono.empty();
                            });
                }).then();
    }


    private Mono<Void> processRegularMessage(ChunkData messageChunk, String chatroomId) {
        log.info("Message chunk to be processed {}", messageChunk);
        return messageService.create(messageChunk.chunk()) // Assuming messageService.create returns a Mono<Message>
                .flatMap(createdMessage -> {
                    ChunkData chunkData = ChunkData.of(UUID.randomUUID().toString(), createdMessage, 1L, 1L, true);
                    broadcastMessage(chunkData, chatroomId);
                    return Mono.empty();
                }).then();
    }


    private Mono<Void> handleSubscription(WebSocketSession session, String userId, String chatroomId) {
        log.info("Client subscribed to chatroom: {}", chatroomId);
        chatroomSinks.computeIfAbsent(chatroomId, id -> new ChatroomSink());
        chatroomSinks.get(chatroomId).addSubscriber(userId, session);
        return Mono.empty();
    }


    private Mono<Void> handleUnsubscription(WebSocketSession session, String userId, String chatroomId) {
        chatroomSinks.get(chatroomId).removeSubscriber(userId, session);
        System.out.println("This is amount of subscriber for given chatroomSink: " +
                chatroomSinks.get(chatroomId).getSubscribers().size());

        if (!chatroomSinks.get(chatroomId).hasSubscribers()) {
            chatroomSinks.remove(chatroomId);
        }
        return Mono.empty();
    }

    private void broadcastMessage(ChunkData chunkData, String chatroomId) {
        chatroomSinks.get(chatroomId).getWebSockets()
                .forEach(session -> {
                    try {
                        String messageString = jsonMapper.writeValueAsString(chunkData);
                        log.info("Emitting message! {}", messageString);
                        session.send(Flux.just(session.textMessage(messageString)))
                                .subscribe();
                    } catch (JsonProcessingException e) {
                        log.info("Error occurred mapping the ChunkData: {}, to JsonFormat", chunkData);
                    }
                });
    }


}
