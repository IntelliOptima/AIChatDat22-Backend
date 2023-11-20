package com.example.aichatprojectdat.chatroom.service;

import com.example.aichatprojectdat.chatroom.exception.NotFoundException;
import com.example.aichatprojectdat.chatroom.model.ChatroomUsersRelation;
import com.example.aichatprojectdat.chatroom.repository.ChatroomUsersRelationRepository;
import com.example.aichatprojectdat.user.model.User;
import com.example.aichatprojectdat.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Primary
public class ChatroomUsersRelationServiceImpl implements IChatRoomUsersRelationService {

    private final ChatroomUsersRelationRepository chatroomUsersRelationRepository;
    private final UserRepository userRepository;

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
    public Mono<Boolean> isUserPartOfChatroom(Long userId, String chatroomId) {
        return chatroomUsersRelationRepository.findByUserIdAndChatroomId(userId, chatroomId)
                .map(chatroomUserRelation -> true)
                .defaultIfEmpty(false);
    }
}
