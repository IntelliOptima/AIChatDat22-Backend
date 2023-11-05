package com.example.aichatprojectdat.chatroom.service;

import com.example.aichatprojectdat.chatroom.model.ChatroomUsersRelation;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface IChatRoomUsersRelationService {
    Mono<ChatroomUsersRelation> create(ChatroomUsersRelation chatroomUsersRelation);

    Flux<ChatroomUsersRelation> findAllByChatroomId(Long id);
}
