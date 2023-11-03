package com.example.aichatprojectdat.chatroom.service;

import com.example.aichatprojectdat.chatroom.model.Chatroom;
import reactor.core.publisher.Mono;


public interface IChatroomService {

    Mono<Chatroom> create(Chatroom of);

    Mono<Chatroom> findById(long id);
}
