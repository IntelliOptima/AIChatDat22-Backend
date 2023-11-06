package com.example.aichatprojectdat.chatroom.service;

import com.example.aichatprojectdat.chatroom.model.ChatroomUsersRelation;
import com.example.aichatprojectdat.chatroom.repository.ChatroomUsersRelationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Primary
public class ChatroomUsersRelationService implements IChatRoomUsersRelationService {

    private final ChatroomUsersRelationRepository chatroomUsersRelationRepository;

    @Override
    public Mono<ChatroomUsersRelation> create(ChatroomUsersRelation chatroomUsersRelation) {
        return chatroomUsersRelationRepository.save(chatroomUsersRelation);
    }

    @Override
    public Flux<ChatroomUsersRelation> findAllByChatroomId(Long chatroomId) {
        return chatroomUsersRelationRepository.findAllByChatroomId(chatroomId);
    }

    @Override
    public Mono<Boolean> isUserPartOfChatroom(Long userId, Long chatroomId) {
        return chatroomUsersRelationRepository.findByUserIdAndChatroomId(userId, chatroomId)
                .map(chatroomUserRelation -> true)
                .defaultIfEmpty(false);
    }
}
