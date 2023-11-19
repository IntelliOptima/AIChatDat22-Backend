package com.example.aichatprojectdat.chatroom.controller;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.example.aichatprojectdat.ChatGpt.service.GPTServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;

import org.springframework.stereotype.Controller;

import com.example.aichatprojectdat.message.model.Message;
import com.example.aichatprojectdat.message.service.IMessageService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.CrossOrigin;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Controller
@RequiredArgsConstructor
@CrossOrigin
public class ChatroomController {

    private final IMessageService messageService;
    private final GPTServiceImpl gptService;

    private final Map<String, Sinks.Many<Message>> chatroomSinks = new ConcurrentHashMap<>();

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
            // Emit an empty placeholder message for the GPT response
            log.info("Emitting GPT Response Start message");
            Message placeholderMessage = Message.of(1L, "", chatMessage.chatroomId());
            sink.emitNext(placeholderMessage, Sinks.EmitFailureHandler.FAIL_FAST);

            handleGptMessage(chatMessage, sink);
        }
    }

    public void emitReceivedMessage(Message chatMessage, Sinks.Many<Message> sink) {
        messageService.create(chatMessage).subscribe();

        log.info("Emitting message {}", chatMessage);
        sink.emitNext(chatMessage, Sinks.EmitFailureHandler.FAIL_FAST);
    }

    public void handleGptMessage(Message chatMessage, Sinks.Many<Message> sink) {
        StringBuilder gptAnswer = new StringBuilder();

        gptService.streamChat(chatMessage.textMessage())
                .doOnNext(chunk -> {
                    String updatedContent = gptAnswer.append(chunk).toString();
                    sink.emitNext(Message.of(1L, updatedContent, chatMessage.chatroomId()), Sinks.EmitFailureHandler.FAIL_FAST);
                })
                .doOnError(e -> log.error("Error in GPT streaming: {}", e.getMessage()))
                .publishOn(Schedulers.boundedElastic())
                .doFinally(signalType -> {
                    sink.emitNext(Message.of(1L, "Gpt Finished message", chatMessage.chatroomId()), Sinks.EmitFailureHandler.FAIL_FAST);
                    Message gptCompleteMessage = Message.of(1L, gptAnswer.toString(), chatMessage.chatroomId());
                    messageService.create(gptCompleteMessage).subscribe();
                    gptAnswer.setLength(0);
                }).subscribe();
    }


}