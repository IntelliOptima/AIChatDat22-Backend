package com.example.aichatprojectdat.chatroom.controller;

import com.example.aichatprojectdat.chatroom.model.Chatroom;
import com.example.aichatprojectdat.chatroom.service.IChatroomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@CrossOrigin
@RequestMapping("/api/v1/chatroom")
public class ChatroomRestController {
    private final IChatroomService chatroomService;

    @PostMapping("{chatroomUserCreatorId}")
    Mono<Chatroom> create(@PathVariable Long chatroomUserCreatorId) {
        return chatroomService.create(chatroomUserCreatorId);
    }

    @GetMapping("/participatingChatrooms/{userId}")
    Flux<Chatroom> getParticipatingChatrooms(@PathVariable Long userId) {
        return chatroomService.findAllParticipatingChatrooms(userId);
    }

    @GetMapping("/{id}")
    Mono<Chatroom> getChatroom(@PathVariable String id) {
        return chatroomService.findById(id);
    }

    @GetMapping("/{creatorId}")
    public Flux<Chatroom> getChatroomByCreatorId(@PathVariable Long creatorId) {
        return chatroomService.findAllByCreatorId(creatorId);
    }

}
