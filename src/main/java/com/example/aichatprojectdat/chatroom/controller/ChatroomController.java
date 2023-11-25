package com.example.aichatprojectdat.chatroom.controller;


import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.example.aichatprojectdat.OpenAIModels.dall_e.model.generation.ImageGenerationRequest;
import com.example.aichatprojectdat.OpenAIModels.dall_e.service.IDALL_E3ServiceStandard;
import com.example.aichatprojectdat.OpenAIModels.dall_e.spring.service.interfaces.IDALL_EService;
import com.example.aichatprojectdat.OpenAIModels.gpt.service.GPT3ServiceImpl;
import com.example.aichatprojectdat.OpenAIModels.gpt.service.GPT4ServiceImpl;
import com.example.aichatprojectdat.OpenAIModels.gpt.service.interfaces.IGPT3Service;
import com.example.aichatprojectdat.OpenAIModels.gpt.service.interfaces.IGPT4Service;
import com.example.aichatprojectdat.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;

import org.springframework.stereotype.Controller;

import com.example.aichatprojectdat.message.model.Message;
import com.example.aichatprojectdat.message.service.IMessageService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

@Slf4j
@RestController
@RequiredArgsConstructor
@CrossOrigin
public class ChatroomController {

    private final IMessageService messageService;
    private final IGPT3Service gpt3Service;
    private final IGPT4Service gpt4Service;
    private final IDALL_E3ServiceStandard iDallE3ServiceStandard;

    private final Map<String, Sinks.Many<Message>> chatroomSinks = new ConcurrentHashMap<>();


    // Map to keep track of connected users in each chatroom
    private final Map<String, Set<Long>> onlineUsers = new ConcurrentHashMap<>();

    // Map to hold and multicast updates about online users
    private final Map<String, Sinks.Many<Long>> userSinks = new ConcurrentHashMap<>();

    //_______________________ RSOCKET -> CHANNEL _______________________________

    // Method for clients to connect and stream chatroom messages
    @MessageMapping("chat.stream.{chatroomId}")
    public Flux<Message> streamMessages(
            @DestinationVariable String chatroomId,
            Mono<String> requestMessage
    ) {
        return requestMessage.doOnNext(s -> System.out.println("Received message text: " + s))
                .thenMany(chatroomSinks.computeIfAbsent(chatroomId, id ->
                                Sinks.many()
                                        .replay()
                                        .latest())
                        .asFlux().onBackpressureBuffer());

    }

    @MessageMapping("chat.online.{chatroomId}")
    public Flux<Message> streamOnlineUsers(
            @DestinationVariable String chatroomId,
            Mono<String> userConnected) {
        return userConnected
                .map(Long::parseLong)
                .doOnNext(userId -> {
                    log.info("User: " + userId + " has connected");
                    onlineUsers.computeIfAbsent(chatroomId, k -> new HashSet<>()).add(userId);
                })
                .thenMany(Flux.defer(() ->
                        Flux.fromIterable(onlineUsers.get(chatroomId))
                                .map(userId -> Message.of(userId, "User: " + userId + " has just connected", chatroomId))
                ));
    }




    // Method for clients to send messages to a chatroom
    @MessageMapping("chat.send.{chatroomId}")
    public void receiveMessage(@DestinationVariable String chatroomId, Message chatMessage) {
        log.info("Received message: {}", chatMessage.textMessage());

        Sinks.Many<Message> sink = chatroomSinks.computeIfAbsent(chatroomId, id ->
                Sinks.many()
                        .multicast()
                        .onBackpressureBuffer());

        emitReceivedMessage(chatMessage, sink);

        // Check if the message is a GPT command
        if (chatMessage.textMessage().toLowerCase().startsWith("@gpt")) {
            log.info("Emitting GPT Response");
            handleGptMessage(chatMessage, sink);
        }

        if (chatMessage.textMessage().toLowerCase().startsWith("@dalle")) {
            log.info("Emitting DallE Response");
            handleDallEMessage(chatMessage, sink);
        }
    }

    public void emitReceivedMessage(Message chatMessage, Sinks.Many<Message> sink) {
        Message receivedMessage = Message.of(chatMessage.userId(), chatMessage.textMessage(), chatMessage.chatroomId());
        messageService.create(receivedMessage).subscribe();

        log.info("Emitting message {}", chatMessage);
        sink.emitNext(chatMessage, Sinks.EmitFailureHandler.FAIL_FAST);
    }

    public void handleGptMessage(Message chatMessage, Sinks.Many<Message> sink) {
        StringBuilder gptAnswer = new StringBuilder();
        
        String gptMessageId = UUID.randomUUID().toString();
        Instant createdDate = Instant.now();
        
        gpt4Service.streamChat(chatMessage.textMessage().split("@gpt")[1])
                .doOnNext(chunk -> {
                    String updatedContent = gptAnswer.append(chunk).toString();
                    sink.emitNext(Message.ofGPTStream(gptMessageId,1L, updatedContent, chatMessage.chatroomId(), createdDate), Sinks.EmitFailureHandler.FAIL_FAST);
                })
                .doOnError(e -> log.error("Error in GPT streaming: {}", e.getMessage()))
                .publishOn(Schedulers.boundedElastic())
                .doFinally(signalType -> {
                    sink.emitNext(Message.ofGPTStream(gptMessageId,1L, "Gpt Finished message", chatMessage.chatroomId(), createdDate), Sinks.EmitFailureHandler.FAIL_FAST);
                    Message gptCompleteMessage = Message.ofGPTStream(gptMessageId,1L, gptAnswer.toString(), chatMessage.chatroomId(), createdDate);
                    messageService.create(gptCompleteMessage).subscribe();
                    gptAnswer.setLength(0);
                }).subscribe();
    }


    public void handleDallEMessage(Message chatMessage, Sinks.Many<Message> sink) {
        iDallE3ServiceStandard.generateImage(chatMessage.textMessage())
                .doFirst(() -> System.out.println(ImageGenerationRequest.of(chatMessage.textMessage()).toString()))
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(response -> {
                    Message dalleMessage = Message.of(2L, response.getImageList().get(0).getUrl(), chatMessage.chatroomId());
                    sink.emitNext(dalleMessage,Sinks.EmitFailureHandler.FAIL_FAST);
                    messageService.create(dalleMessage).subscribe();
                }).subscribe();
    }


}