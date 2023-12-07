package com.example.aichatprojectdat.chatroom.model;

import com.example.aichatprojectdat.message.model.ChunkData;
import lombok.*;
import reactor.core.publisher.Sinks;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class ChatroomSink {

    private final Sinks.Many<ChunkData> sink = Sinks.many().multicast().onBackpressureBuffer();
    private final Set<String> subscribers = ConcurrentHashMap.newKeySet();

    public void addSubscriber(String subscriberId) {
        subscribers.add(subscriberId);
    }

    public void removeSubscriber(String subscriberId) {
        subscribers.remove(subscriberId);
    }

    public boolean hasSubscribers() {
        return !subscribers.isEmpty();
    }

}