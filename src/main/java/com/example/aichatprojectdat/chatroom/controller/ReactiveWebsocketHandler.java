package com.example.aichatprojectdat.chatroom.controller;

import com.example.aichatprojectdat.OpenAIModels.dall_e.service.IDALL_E3ServiceStandard;
import com.example.aichatprojectdat.OpenAIModels.gpt.service.interfaces.IGPT3Service;
import com.example.aichatprojectdat.OpenAIModels.gpt.service.interfaces.IGPT4Service;
import com.example.aichatprojectdat.chatroom.model.ChatroomSink;
import com.example.aichatprojectdat.message.model.ChunkData;
import com.example.aichatprojectdat.message.model.Message;
import com.example.aichatprojectdat.message.service.IMessageService;
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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component("ReactiveWebSocketHandler")
@Slf4j
public class ReactiveWebsocketHandler implements WebSocketHandler {

    private final IMessageService messageService;
    private final IGPT3Service gpt3Service;
    private final IGPT4Service gpt4Service;
    private final IDALL_E3ServiceStandard iDallE3ServiceStandard;
    @Qualifier(value = "jsonObjectMapperUtil")
    private final ObjectMapper jsonMapper;
    private final Map<String, ChatroomSink> chatroomSinks = new ConcurrentHashMap<>();

    public ReactiveWebsocketHandler (
            @Qualifier("jsonObjectMapperUtil") ObjectMapper objectMapper,
            IMessageService messageService,
            IGPT3Service gpt3Service,
            IGPT4Service gpt4Service,
            IDALL_E3ServiceStandard idallE3ServiceStandard) {
        this.messageService =messageService;
        this.gpt3Service = gpt3Service;
        this.gpt4Service = gpt4Service;
        this.iDallE3ServiceStandard = idallE3ServiceStandard;
        this.jsonMapper = objectMapper;
    }


    @Override
    public @NotNull Mono<Void> handle(WebSocketSession session) {
        String uriPath = session.getHandshakeInfo().getUri().getPath();

        return session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .flatMap(messageAsString -> {
                    String[] splitMessage = messageAsString.split(":", 2);
                    String messageType = splitMessage[0];
                    String payload = splitMessage.length > 1 ? splitMessage[1] : ""; // will be the main object from client either userId or message...

                    System.out.println("THIS IS PAYLOAD: " + payload);
                    System.out.println("This is size of chatroomSinks: " + chatroomSinks.size());

                    return switch (messageType) {
                        case "SUBSCRIBE" ->
                                handleSubscription(session, payload, extractChatroomId(uriPath));
                        case "MESSAGE" -> Flux.from(Flux.just(convertToChunkData(payload)))
                                .groupBy(ChunkData::identifier)
                                .flatMap(groupedFlux ->
                                        groupedFlux.collectList()
                                                .flatMapMany(chunkDataList -> {

                                                    if (chunkDataList.isEmpty()) {
                                                        log.warn("Received empty chunk data list");
                                                        return Flux.empty();
                                                    }

                                                    chunkDataList.sort(Comparator.comparing(ChunkData::startIndex));
                                                    ChunkData lastChunk = chunkDataList.get(chunkDataList.size() - 1);


                                                    if (isGptMessage(chunkDataList) && isLastChunkReceived(chunkDataList)) {
                                                        return handleGptContextMessage(extractChatroomId(uriPath), chunkDataList);
                                                    } else if (isDalleMessage(chunkDataList)) {
                                                        return handleDallEMessage(extractChatroomId(uriPath), chunkDataList);
                                                    } else {
                                                        log.info("Handling regular message");
                                                        return processRegularMessage(lastChunk, extractChatroomId(uriPath))
                                                                .then();
                                                    }
                                                })

                                );
                        case "CLOSE" ->
                                handleUnsubscription(session, payload, extractChatroomId(uriPath));
                        default -> Mono.error(new RuntimeException("Unknown message type"));
                    };
                }).then();
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
                                    Message newMessage = Message.builder()
                                            .id(gptMessageId)
                                            .userId(1L)
                                            .textMessage(chunk)
                                            .chatroomId(chatroomId)
                                            .createdDate(Instant.now())
                                            .build();

                                    ChunkData newChunk = ChunkData.of(UUID.randomUUID().toString(),
                                            newMessage,
                                            (long) chunk.length(),
                                            null,
                                            false);

                                    broadcastMessage(newChunk, chatroomId);

                                    // Instead of Mono.empty(), return Mono.just(newChunk)
                                    return Mono.just(newChunk);
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
                    return messageService.create(dalleMessage)
                            .flatMap(message -> {
                                broadcastMessage(dalleChunkData, message.getChatroomId());
                                return Mono.empty();
                            });
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
        if (chunkDataList.size() == chunkDataList.get(0).totalChunks()) {
            return chunkDataList.stream().anyMatch(ChunkData::isLastChunk);
        } else {
            return false;
        }
    }

    private ChunkData convertToChunkData(String messageContent) {
        try {
            log.info("processMessage received: {} ", messageContent);
            return jsonMapper.readValue(messageContent, ChunkData.class);
        } catch (JsonProcessingException e) {
            return ChunkData.empty();
        }
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

    private Mono<String> extractUserId(WebSocketSession session) {
        return session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .next() // Assuming you are interested in the first message only
                .flatMap(userId -> {

                    try {
                        return Mono.just(jsonMapper.readValue(userId, String.class));
                    } catch (JsonProcessingException e) {
                        log.info(e.toString());
                        return Mono.empty(); // or Mono.error(e);
                    }
                });
    }


    private String extractChatroomId(String uriPath) {
        // Extract the chatroomId from the URI path
        return uriPath.substring(uriPath.lastIndexOf('/') + 1);
    }
}
