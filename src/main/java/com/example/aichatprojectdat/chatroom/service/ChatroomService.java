package com.example.aichatprojectdat.chatroom.service;

import com.example.aichatprojectdat.chatroom.model.Chatroom;
import com.example.aichatprojectdat.chatroom.repository.ChatroomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Primary
public class ChatroomService implements IChatroomService {

    private final ChatroomRepository chatroomRepository;

    @Override
    public Mono<Chatroom> create(Chatroom chatroom) {
        return chatroomRepository.save(chatroom);
    }

    @Override
    public Mono<Chatroom> findById(long id) {
        return chatroomRepository.findById(id);
    }
}
