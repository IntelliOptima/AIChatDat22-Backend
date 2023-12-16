package com.example.aichatprojectdat.chatroom.controller;

import com.example.aichatprojectdat.chatroom.model.ChatroomSink;
import com.example.aichatprojectdat.message.model.ChunkData;
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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component("ReactiveWebSocketHandler")
@Slf4j
public class ReactiveWebsocketHandler implements WebSocketHandler {

    @Qualifier(value = "jsonObjectMapperUtil")
    private final ObjectMapper jsonMapper;
    private final Map<String, ChatroomSink> chatroomSinks = new ConcurrentHashMap<>();

    public ReactiveWebsocketHandler (@Qualifier("jsonObjectMapperUtil") ObjectMapper objectMapper ) {
        this.jsonMapper = objectMapper;
    }

    @Override
    public @NotNull Mono<Void> handle(WebSocketSession session) {
        String uriPath = session.getHandshakeInfo().getUri().getPath();

        return session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .flatMap(command -> {
                    if ("subscribe".equals(command)) {
                        // Process subscription and return a Mono
                        return processSubscription(session, uriPath);
                    } else {
                        // Handle regular chat messages and return a Mono
                        return handleEventEmitter(session, extractChatroomId(uriPath));
                    }
                })
                .then(); // This ensures the completion of the pipeline
    }

    private Mono<Void> processSubscription(WebSocketSession session, String uriPath) {
        return extractUserId(session)
                .flatMap(userId -> userId != null ?
                        handleSubscription(session, userId, extractChatroomId(uriPath)) :
                        Mono.empty());
    }

    private Mono<Void> handleSubscription(WebSocketSession session, String userId, String chatroomId) {
        log.info("Client subscribed to chatroom: {}", chatroomId);
        chatroomSinks.computeIfAbsent(chatroomId, id -> new ChatroomSink());
        chatroomSinks.get(chatroomId).addSubscriber(userId, session);
        return Mono.empty();
    }


    private Mono<Void> handleEventEmitter(WebSocketSession session, String chatroomId) {
        return session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .flatMap(message -> processMessage(message, chatroomId))
                .then();
    }


    private Mono<Void> processMessage(String messageAsString, String chatroomId) {
        try {
            ChunkData messageChunk = jsonMapper.readValue(messageAsString, ChunkData.class);
            log.info("processMessage received: {} ", messageChunk.toString());
            broadcastMessage(messageChunk, chatroomId);
            return Mono.empty();
        } catch (JsonProcessingException e) {
            return Mono.error(new RuntimeException("Error processing message", e));
        }
    }

    private void broadcastMessage(ChunkData chunkData, String chatroomId) {
        chatroomSinks.get(chatroomId).getWebSockets()
                .forEach(session -> {
                    try {
                        String messageString = jsonMapper.writeValueAsString(chunkData);
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
