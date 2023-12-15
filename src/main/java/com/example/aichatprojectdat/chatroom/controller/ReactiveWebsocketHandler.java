package com.example.aichatprojectdat.chatroom.controller;

import com.example.aichatprojectdat.message.model.ChunkData;
import com.example.aichatprojectdat.message.model.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.util.UUID.randomUUID;

@Component("ReactiveWebSocketHandler")
@RequiredArgsConstructor
@Slf4j
public class ReactiveWebsocketHandler implements WebSocketHandler {
    private final ObjectMapper jsonMapper;

    // A list to keep track of all sessions
    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    @Override
    public @NotNull Mono<Void> handle(@NotNull WebSocketSession session) {
        // Add session to the list of sessions
        sessions.add(session);

        // Process incoming messages and transform the flux to Mono<Void>
        Mono<Void> messageProcessor = session.receive()
                .log()
                .map(WebSocketMessage::getPayloadAsText)
                .flatMap(message -> processMessage(message, session))
                .then(); // This converts the Flux<Void> to Mono<Void>

        // Return Mono<Void> and remove the session on completion
        return messageProcessor.doFinally(sig -> sessions.remove(session));
    }


    private Mono<WebSocketMessage> processMessage(String messageAsString, WebSocketSession currentSession) {
        try {
            ChunkData messageChunk = jsonMapper.readValue(messageAsString, ChunkData.class);
            log.info("processMessage received: {} ", messageChunk.toString());
            broadcastMessage(messageChunk, currentSession);
            return Mono.empty();
        } catch (JsonProcessingException e) {
            return Mono.error(new RuntimeException("Error processing message", e));
        }
    }

    private void broadcastMessage(ChunkData chunkData, WebSocketSession senderSession) {
        sessions
                //.filter(session -> !session.equals(senderSession)) // Exclude the sender
                .forEach(session -> {
                    try {
                        String messageString = jsonMapper.writeValueAsString(chunkData);
                        session.send(Flux.just(session.textMessage(messageString))).subscribe();
                    } catch (JsonProcessingException e) {
                        log.info("Error occurred mapping the ChunkData: {}, to JsonFormat", chunkData);
                    }
                });
    }


}
