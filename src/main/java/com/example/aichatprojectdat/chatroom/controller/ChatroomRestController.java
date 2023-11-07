package com.example.aichatprojectdat.chatroom.controller;

import com.example.aichatprojectdat.chatroom.model.Chatroom;
import com.example.aichatprojectdat.chatroom.service.IChatroomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chatroom")
public class ChatroomRestController {
    private final IChatroomService service;

    @PostMapping
    Mono<Chatroom> create(@RequestBody Chatroom chatroom) {
        return service.create(chatroom);
    }

    @GetMapping
    Flux<Chatroom> getAllChatrooms() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    Mono<Chatroom> getChatroom(@PathVariable Long id) {
        return service.findById(id);
    }


    @GetMapping("/{creatorId}")
    public Flux<Chatroom> getChatroomByCreatorId(@PathVariable Long creatorId) {
        return service.findAllByCreatorId(creatorId);
    }
}
