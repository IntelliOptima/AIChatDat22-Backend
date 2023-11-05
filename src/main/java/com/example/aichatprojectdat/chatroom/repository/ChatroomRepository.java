package com.example.aichatprojectdat.chatroom.repository;

import com.example.aichatprojectdat.chatroom.model.Chatroom;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ChatroomRepository extends R2dbcRepository<Chatroom, Long> {
    Flux<Chatroom> findAllByChatroomUserCreatorId(Long creatorId);
}
