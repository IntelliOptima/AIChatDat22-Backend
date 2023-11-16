package com.example.aichatprojectdat.message.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;

import com.example.aichatprojectdat.message.model.Message;
import com.example.aichatprojectdat.message.service.IMessageService;
import com.example.aichatprojectdat.message.exception.FetchingElementMessageException;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/message")
@RequiredArgsConstructor
@CrossOrigin
public class MessageRestController {

private final IMessageService messageService;

    @GetMapping(produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<Message> getMessages() {
        return messageService.getMessages();
    }


    @GetMapping("/findByChatroomId={chatroomId}")
    public Mono<ResponseEntity<Flux<Message>>> getMessagesByChatroomId(@PathVariable String chatroomId) {
        return Mono.just(ResponseEntity.ok().body(messageService.getMessagesByChatroomId(chatroomId)));
    }

}
