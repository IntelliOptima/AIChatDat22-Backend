package com.example.aichatprojectdat.chatroom.repository;

import com.example.aichatprojectdat.chatroom.model.ChatroomUsersRelation;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

@Repository
public interface ChatroomUsersRelationRepository extends R2dbcRepository<ChatroomUsersRelation, Long> {

    Flux<ChatroomUsersRelation> findAllByChatroomId(String chatroomId);

    Flux<ChatroomUsersRelation> findAllByUserId(Long id);

    Mono<ChatroomUsersRelation> findByUserIdAndChatroomId(Long userId, String chatroomId);

    Mono<Void> deleteByUserIdInAndChatroomId(Collection<Long> userIds, String chatroomId);
}
