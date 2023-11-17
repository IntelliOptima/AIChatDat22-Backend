package com.example.aichatprojectdat.chatroom.service;

import com.example.aichatprojectdat.chatroom.exception.NotFoundException;
import com.example.aichatprojectdat.chatroom.model.Chatroom;
import com.example.aichatprojectdat.chatroom.model.ChatroomUsersRelation;
import com.example.aichatprojectdat.chatroom.repository.ChatroomRepository;
import com.example.aichatprojectdat.chatroom.repository.ChatroomUsersRelationRepository;
import com.example.aichatprojectdat.message.model.Message;
import com.example.aichatprojectdat.message.repository.MessageRepository;
import com.example.aichatprojectdat.user.model.User;
import com.example.aichatprojectdat.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Primary
public class ChatroomServiceImpl implements IChatroomService {

    private final ChatroomRepository chatroomRepository;
    private final ChatroomUsersRelationRepository chatroomUsersRelationRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

    public Mono<Chatroom> create(Long chatroomUserCreatorId, String chatroomName) {
        return chatroomRepository.save(Chatroom.builder()
                        .id(UUID.randomUUID().toString())
                        .chatroomName(chatroomName)
                        .chatroomUserCreatorId(chatroomUserCreatorId)
                        .build())
                .flatMap(chatroom ->
                        chatroomUsersRelationRepository.save(ChatroomUsersRelation.of(chatroom.getId(), chatroomUserCreatorId))
                                .then(findById(chatroom.getId()))
                );
    }


    public Flux<Chatroom> findAll() {
        return chatroomRepository.findAll();
    }

    public Mono<Chatroom> findById(String chatroomId) {
        return chatroomRepository.findById(chatroomId)
                .switchIfEmpty(Mono.error(new NotFoundException()))
                .flatMap(chatroom -> {
                    // Fetch and set the users
                    return chatroomUsersRelationRepository.findAllByChatroomId(chatroom.getId())
                            .flatMap(relation -> {
                                System.out.println("Relation found for chatroom-user: " + relation);
                                return userRepository.findById(relation.userId())
                                        .doOnSuccess(user -> System.out.println("User found: " + user))
                                        .doOnError(error -> System.out.println("Error fetching user: " + error.getMessage()));
                            })
                            .collectList()
                            .doOnNext(users -> {
                                System.out.println("Users collected for chatroom: " + users);
                                chatroom.setUsers(users);
                            })
                            .then(Mono.just(chatroom))
                            .doOnNext(c -> System.out.println("Chatroom with users set: " + c))
                            // Proceed with fetching messages only after users have been set
                            .then(messageRepository.findAllByChatroomId(chatroom.getId())
                                    .collectList()
                                    .doOnNext(messages -> {
                                        System.out.println("Messages collected for chatroom: " + messages);
                                        chatroom.setMessages(messages);
                                    }))
                            .thenReturn(chatroom);
                })
                .doOnSuccess(chatroom -> System.out.println("Final chatroom: " + chatroom))
                .doOnError(error -> System.out.println("Error in findById: " + error.getMessage()));
    }


    public Flux<Chatroom> findAllParticipatingChatrooms(Long userId) {
        return chatroomUsersRelationRepository.findAllByUserId(userId)
                .switchIfEmpty(Mono.error(new NotFoundException()))
                .flatMap(chatroomUsersRelation -> findById(chatroomUsersRelation.chatroomId()));
    }


    public Flux<Chatroom> findAllByCreatorId(Long creatorId) {
        return chatroomRepository.findAllByChatroomUserCreatorId(creatorId);
    }
}
