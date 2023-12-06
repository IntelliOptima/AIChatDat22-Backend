package com.example.aichatprojectdat.chatroom.service;

import com.example.aichatprojectdat.chatroom.exception.NotFoundException;
import com.example.aichatprojectdat.chatroom.model.ChatroomUsersRelation;
import com.example.aichatprojectdat.chatroom.repository.ChatroomRepository;
import com.example.aichatprojectdat.chatroom.repository.ChatroomUsersRelationRepository;
import com.example.aichatprojectdat.user.model.User;
import com.example.aichatprojectdat.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.NotActiveException;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
@Primary
@Slf4j
public class ChatroomUsersRelationServiceImpl implements IChatRoomUsersRelationService {

    private final ChatroomUsersRelationRepository chatroomUsersRelationRepository;
    private final UserRepository userRepository;
    private final ChatroomRepository chatroomRepository;

    @Override
    public Mono<ChatroomUsersRelation> create(ChatroomUsersRelation chatroomUsersRelation) {
        return chatroomUsersRelationRepository.save(chatroomUsersRelation);
    }

    @Override
    public Flux<ChatroomUsersRelation> findAllByChatroomId(String chatroomId) {
        return chatroomUsersRelationRepository.findAllByChatroomId(chatroomId);
    }

    @Override
    public Flux<ChatroomUsersRelation> findAllByUserId(Long userId) {
        return chatroomUsersRelationRepository.findAllByUserId(userId);
    }

    @Override
    public Mono<ChatroomUsersRelation> addUserToChatroom(String chatroomId, String userEmailToAdd) {

        return userRepository.findUserByEmail(userEmailToAdd)
                .map(User::id)
                .flatMap(userId -> chatroomUsersRelationRepository.findByUserIdAndChatroomId(userId, chatroomId)
                        .switchIfEmpty(chatroomUsersRelationRepository
                                .save(ChatroomUsersRelation.of(chatroomId, userId))))
                .switchIfEmpty(Mono.error(new NotFoundException("User not found with email: " + userEmailToAdd)));
    }


    @Override
    public Mono<Void> delete(ChatroomUsersRelation chatroomUsersRelation) {
        return chatroomUsersRelationRepository.delete(chatroomUsersRelation);
    }

    @Override
    public Mono<Void> leaveChatroom(String chatroomId, Long userId) {
        Mono<Void> deleteRelation = chatroomUsersRelationRepository
                .findByUserIdAndChatroomId(userId, chatroomId)
                .flatMap(chatroomUsersRelationRepository::delete);

        Mono<Boolean> checkForId2And1 = chatroomUsersRelationRepository
                .findAllByChatroomId(chatroomId)
                .map(ChatroomUsersRelation::userId)
                .collectList()
                .map(userIds -> userIds.stream().allMatch(id -> id.equals(1L) || id.equals(2L)));

        Mono<Void> removeIds1And2 = checkForId2And1
                .flatMap(shouldDelete -> shouldDelete ?
                        chatroomUsersRelationRepository
                                .deleteByUserIdInAndChatroomId(Arrays.asList(1L, 2L), chatroomId)
                                .then() :
                        Mono.empty());

        return deleteRelation.then(checkForId2And1)
                .flatMap(shouldDelete -> shouldDelete ?
                        removeIds1And2.then(chatroomRepository.deleteById(chatroomId)) :
                        Mono.empty());
    }


    @Override
    public Mono<Boolean> isUserPartOfChatroom(Long userId, String chatroomId) {
        return chatroomUsersRelationRepository.findByUserIdAndChatroomId(userId, chatroomId)
                .map(chatroomUserRelation -> true)
                .defaultIfEmpty(false);
    }
}
