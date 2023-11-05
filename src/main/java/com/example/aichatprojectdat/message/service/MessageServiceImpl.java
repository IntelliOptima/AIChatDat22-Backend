package com.example.aichatprojectdat.message.service;

import com.example.aichatprojectdat.message.model.Message;
import com.example.aichatprojectdat.message.repository.MessageRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Primary
@RequiredArgsConstructor
public class MessageServiceImpl implements IMessageService {

    private final MessageRepository messageRepository;
    
    @Override
    public Mono<Message> create(Message of) {
        return messageRepository.save(of);
    }

    @Override
    public Flux<Message> findAllByUserId(long userId) {
        return messageRepository.findAllByUserId(userId);
    }

    @Override
    public Mono<Message> findById(long messageId) {
        return messageRepository.findById(messageId);
    }

    @Override
    public Mono<Void> deleteById(long messageId) {
        return messageRepository.deleteById(messageId);
    }

    @Override
    public Flux<Message> findMessagesByChatroomId(long chatroomId) {
        return messageRepository.findAllByChatroomId(chatroomId);
    }

    
}
