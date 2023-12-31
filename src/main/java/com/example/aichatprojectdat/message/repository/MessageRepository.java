package com.example.aichatprojectdat.message.repository;

import com.example.aichatprojectdat.message.model.Message;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface MessageRepository extends R2dbcRepository<Message, String> {
    Flux<Message> findAllByUserIdOrderByCreatedDateAsc(long userId);

    Flux<Message> findAllByChatroomIdOrderByCreatedDateAsc(String chatroomId);


    Flux<Message> findAllByChatroomId(String chatroomId);
}
