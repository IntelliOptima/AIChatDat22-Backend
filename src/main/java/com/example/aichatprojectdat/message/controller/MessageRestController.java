package com.example.aichatprojectdat.message.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.aichatprojectdat.message.model.Message;
import com.example.aichatprojectdat.message.service.IMessageService;
import com.example.aichatprojectdat.message.exception.FetchingElementMessageException;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/message")
@RequiredArgsConstructor
public class MessageRestController {
    
private final IMessageService messageService;

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Message>> getMessages() {
        return messageService.getMessages()
                .map(message -> ServerSentEvent.<Message>builder()
                        .comment("Message received")
                        .event("message-event")
                        .data(message)
                        .build())
                .onErrorResume(FetchingElementMessageException.class, error -> {
                    return Flux.error(error);
                });
    }


    @GetMapping("/findByChatroomId={chatroomId}")
    public Mono<ResponseEntity<Flux<Message>>> getMessagesByChatroomId(@PathVariable Long chatroomId) {
        return Mono.just(ResponseEntity.ok().body(messageService.getMessagesByChatroomId(chatroomId)));
    }

}
