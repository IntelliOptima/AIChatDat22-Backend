package com.example.aichatprojectdat.chatroom.repository;

import com.example.aichatprojectdat.chatroom.model.ChatroomUsersRelation;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ChatroomUsersRelationRepository extends R2dbcRepository<ChatroomUsersRelation, Long> {

    Flux<ChatroomUsersRelation> findAllById(Long id);
}
