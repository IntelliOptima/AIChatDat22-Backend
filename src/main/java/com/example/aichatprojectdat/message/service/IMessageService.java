package com.example.aichatprojectdat.message.service;



import com.example.aichatprojectdat.message.model.Message;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IMessageService {

    Mono<Message> create(Message of);

    Flux<Message> findAllByUserId(long userId);

    Mono<Message> findById(long messageId);

    Mono<Void> deleteById(long messageId);

    Flux<Message> findMessagesByChatroomId(long chatroomId);
}
