package com.example.aichatprojectdat.chatroom.service;

import com.example.aichatprojectdat.chatroom.exception.NotFoundException;
import com.example.aichatprojectdat.chatroom.model.Chatroom;
import com.example.aichatprojectdat.chatroom.model.ChatroomUsersRelation;
import com.example.aichatprojectdat.chatroom.repository.ChatroomRepository;
import com.example.aichatprojectdat.chatroom.repository.ChatroomUsersRelationRepository;
import com.example.aichatprojectdat.message.model.Message;
import com.example.aichatprojectdat.message.model.ReadReceipt;
import com.example.aichatprojectdat.message.repository.MessageRepository;
import com.example.aichatprojectdat.message.service.ReadReceiptServiceImpl;
import com.example.aichatprojectdat.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Primary
@Slf4j
public class ChatroomServiceImpl implements IChatroomService {

    private final ChatroomRepository chatroomRepository;
    private final ChatroomUsersRelationRepository chatroomUsersRelationRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final ReadReceiptServiceImpl readReceiptService;


    @Transactional
    public Mono<Chatroom> create(Chatroom newChatroom) {
        return chatroomRepository.save(Chatroom.builder()
                        .id(UUID.randomUUID().toString())
                        .chatroomName(newChatroom.getChatroomName())
                        .chatroomUserCreatorId(newChatroom.getChatroomUserCreatorId())
                        .color(newChatroom.getColor())
                        .build())
                .flatMap(chatroom -> {
                    Mono<ChatroomUsersRelation> chatroomRelation = chatroomUsersRelationRepository
                            .save(ChatroomUsersRelation.of(chatroom.getId(), newChatroom.getChatroomUserCreatorId()));

                    Mono<ChatroomUsersRelation> GPTRelation = chatroomUsersRelationRepository.save(
                            ChatroomUsersRelation.of(chatroom.getId(), 1L));

                    Mono<ChatroomUsersRelation> DallERelation = chatroomUsersRelationRepository.save(
                            ChatroomUsersRelation.of(chatroom.getId(), 2L)
                    );

                    return Mono.when(chatroomRelation, GPTRelation, DallERelation)
                            .then(Mono.just(chatroom));
                        }
                );
    }


    public Flux<Chatroom> findAll() {
        return chatroomRepository.findAll();
    }

    public Mono<Chatroom> findById(String chatroomId) {
        return chatroomRepository.findById(chatroomId)
                .switchIfEmpty(Mono.error(new NotFoundException("Chatroom does not exist!")))
                .flatMap(chatroom -> {
                    // Fetch and set the users
                    return chatroomUsersRelationRepository.findAllByChatroomId(chatroom.getId())
                            .flatMap(relation -> {
                                log.info("Relation found for chatroom-user: " + relation);
                                return userRepository.findById(relation.userId())
                                        .doOnSuccess(user -> log.info("User found: " + user))
                                        .doOnError(error -> log.info("Error fetching user: " + error.getMessage()));
                            })
                            .collectList()
                            .doOnNext(users -> {
                                log.info("Users collected for chatroom: " + users);
                                chatroom.setUsers(users);
                            })
                            .then(Mono.just(chatroom))
                            .doOnNext(c -> log.info("Chatroom with users set: " + c))
                            // Proceed with fetching messages only after users have been set
                            .then(messageRepository.findAllByChatroomIdOrderByCreatedDateAsc(chatroom.getId())
                                    .flatMap(message ->
                                            readReceiptService.findReadReceiptsByMessageId(message.getId())
                                                    .map(readReceipts ->
                                                            Message.builder()
                                                                    .id(message.getId())
                                                                    .userId(message.getUserId())
                                                                    .textMessage(message.getTextMessage())
                                                                    .chatroomId(message.getChatroomId())
                                                                    .readReceipt(readReceipts)
                                                                    .createdDate(message.getCreatedDate())
                                                                    .lastModifiedDate(message.getLastModifiedDate())
                                                                    .version(message.getVersion())
                                                                    .build()
                                                    )
                                    )
                                    .collectList()
                                    .doOnNext(messages -> {
                                        log.info("Messages collected for chatroom: " + messages);
                                        chatroom.setMessages(messages);
                                    }))
                            .thenReturn(chatroom);
                })
                .doOnSuccess(chatroom -> log.info("Final chatroom: " + chatroom))
                .doOnError(error -> log.info("Error in findById: " + error.getMessage()));
    }


    public Flux<Chatroom> findAllParticipatingChatrooms(Long userId) {
        return chatroomUsersRelationRepository.findAllByUserId(userId)
                .switchIfEmpty(Mono.error(new NotFoundException("No Participants found!")))
                .flatMap(chatroomUsersRelation -> findById(chatroomUsersRelation.chatroomId()));
    }


    public Flux<Chatroom> findAllByCreatorId(Long creatorId) {
        return chatroomRepository.findAllByChatroomUserCreatorId(creatorId);
    }


    public Mono<Chatroom> delete(String chatroomId) {
        return chatroomUsersRelationRepository.findAllByChatroomId(chatroomId)
                .flatMap(chatroomUsersRelation -> chatroomUsersRelationRepository.delete(chatroomUsersRelation)
                        .then(Mono.empty()))
                .switchIfEmpty(Mono.empty())
                .then(chatroomRepository.findById(chatroomId)
                        .flatMap(chatroomRepository::delete)
                        .then(Mono.empty())
                );
    }
}
