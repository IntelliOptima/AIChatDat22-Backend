package com.example.aichatprojectdat.message.service;



import com.example.aichatprojectdat.message.model.Message;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IMessageService {

    Mono<Message> create(Message message);

    Mono<Message> getMessageById(Long messageId);

    Flux<Message> getMessages();

    Flux<Message> getMessagesByChatroomId(String chatroomId);

    Mono<Void> deleteById(long messageId);

    Flux<Message> getAllMessagesByUserId(long userId);
}
