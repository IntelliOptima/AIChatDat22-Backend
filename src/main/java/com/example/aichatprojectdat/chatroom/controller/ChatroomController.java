package com.example.aichatprojectdat.chatroom.controller;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.example.aichatprojectdat.ChatGpt.service.GPTServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Controller;

import com.example.aichatprojectdat.chatroom.service.IChatRoomUsersRelationService;
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
    private final Map<String, Sinks.Many<String>> gptAnswerSinks = new ConcurrentHashMap<>();

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
                                .multicast()
                                .onBackpressureBuffer())
                        .asFlux().onBackpressureBuffer());
    }

    @MessageMapping("chat.gptstream.{chatroomId}")
    public Flux<String> streamStringGpt(
            @DestinationVariable String chatroomId,
            Mono<String> requestMessage
    ) {
        return requestMessage.doOnNext(s -> System.out.println("Received message text: " + s))
                .thenMany(gptAnswerSinks.computeIfAbsent(chatroomId, id ->
                        Sinks.many()
                                .multicast()
                                .onBackpressureBuffer())
                        .asFlux().onBackpressureBuffer());
    }

    // Method for clients to send messages to a chatroom
    @MessageMapping("chat.send.{chatroomId}")
    public void receiveMessage(@DestinationVariable String chatroomId, Message chatMessage, RSocketRequester requester) {
        log.info("Received message: {}", chatMessage.textMessage());

        messageService.create(chatMessage)
                .doOnError(e -> log.error("Error creating message: {}", e.getMessage()))
                .subscribe();

        Sinks.Many<Message> messageSink = chatroomSinks.computeIfAbsent(chatroomId, id ->
                Sinks.many()
                        .multicast()
                        .onBackpressureBuffer());

        messageSink.emitNext(chatMessage, Sinks.EmitFailureHandler.FAIL_FAST);

        if (chatMessage.textMessage().toLowerCase().startsWith("@gpt")) {
            messageSink.emitNext(Message.of(1L, "", chatroomId), Sinks.EmitFailureHandler.FAIL_FAST);
            handleGptRequest(chatroomId, chatMessage.textMessage().substring(4));
        }
    }

    private void handleGptRequest(String chatroomId, String question) {
        Sinks.Many<String> answerSink = gptAnswerSinks.computeIfAbsent(chatroomId, id -> Sinks.many().multicast().onBackpressureBuffer());
        StringBuilder completeGPTMessage = new StringBuilder();

        gptService.streamChat(question)
                .doOnNext(gptChunk -> {
                    log.info("GPT chunk received: {}", gptChunk);
                    answerSink.emitNext(gptChunk, Sinks.EmitFailureHandler.FAIL_FAST);
                    completeGPTMessage.append(gptChunk);
                })
                .doOnError(e -> log.error("Error in GPT streaming: {}", e.getMessage()))
                .publishOn(Schedulers.boundedElastic())
                .doOnComplete(() -> {
                    answerSink.emitNext("Gpt Finished message", Sinks.EmitFailureHandler.FAIL_FAST);
                    Message gptCompleteMessage = Message.of(1L, completeGPTMessage.toString(), chatroomId);
                    messageService.create(gptCompleteMessage).subscribe();
                    completeGPTMessage.setLength(0);
                })
                .subscribe();
    }

}