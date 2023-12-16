package com.example.aichatprojectdat.chatroom.model;

import com.example.aichatprojectdat.message.model.ChunkData;
import lombok.*;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Sinks;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class ChatroomSink {

    private final Sinks.Many<ChunkData> sink = Sinks.many().multicast().onBackpressureBuffer();
    private final Set<String> subscribers = ConcurrentHashMap.newKeySet();
    private final Set<WebSocketSession> webSockets = ConcurrentHashMap.newKeySet();

    public void addSubscriber(String subscriberId, WebSocketSession websocket) {
        subscribers.add(subscriberId);
        webSockets.add(websocket);
    }

    public void removeSubscriber(String subscriberId, WebSocketSession session) {
        subscribers.remove(subscriberId);
        webSockets.remove(session);
    }

    public boolean hasSubscribers() {
        return !subscribers.isEmpty();
    }

}