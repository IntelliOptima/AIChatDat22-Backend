package com.example.aichatprojectdat.chatroom.service;

import com.example.aichatprojectdat.chatroom.model.Chatroom;
import com.example.aichatprojectdat.chatroom.model.ChatroomUsersRelation;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface IChatroomService {

    Mono<Chatroom> create(Long chatroomUserCreatorId);

    Flux<Chatroom> findAll();

    Mono<Chatroom> findById(String chatroomId);

    Flux<Chatroom> findAllParticipatingChatrooms(Long userid);

    Flux<Chatroom> findAllByCreatorId(Long creatorId);
}
