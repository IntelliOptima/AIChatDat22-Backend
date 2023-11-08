package com.example.aichatprojectdat.message.service;

import com.example.aichatprojectdat.message.model.Message;
import com.example.aichatprojectdat.message.repository.MessageRepository;
import lombok.RequiredArgsConstructor;

import java.time.Duration;

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
    public Mono<Message> getMessageById(Long messageId) {
        return messageRepository.findById(messageId);
    }

    @Override
    public Mono<Message> create(Message message) {
        return messageRepository.save(message);
    }

    @Override
    public Flux<Message> getMessagesByChatroomId(String chatroomId) {
        return messageRepository.findAllByChatroomId(chatroomId);
    }

    @Override
    public Mono<Void> deleteById(long messageId) {
        return messageRepository.deleteById(messageId);
    }

    @Override
    public Flux<Message> getAllMessagesByUserId(long userId) {
        return messageRepository.findAllByUserId(userId);
    }

    @Override
    public Flux<Message> getMessages() {
        return messageRepository.findAll().delayElements(Duration.ofMillis(100)).map(message -> {
            if (message.id() > 20 && message.id() < 30) throw new RuntimeException();
            return message;
        });
    }

    
}
