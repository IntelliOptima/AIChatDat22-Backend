package com.example.aichatprojectdat.chatroom.controller;

import com.example.aichatprojectdat.chatroom.model.Chatroom;
import com.example.aichatprojectdat.chatroom.service.IChatroomService;
import com.example.aichatprojectdat.message.model.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    Mono<Chatroom> create(@PathVariable Long chatroomUserCreatorId, @RequestBody String chatroomName) {
        return chatroomService.create(chatroomUserCreatorId, chatroomName);
    }

    @GetMapping("/participatingChatrooms/{userId}")
    Mono<ResponseEntity<Flux<Chatroom>>> getParticipatingChatrooms(@PathVariable Long userId) {
        return Mono.just(ResponseEntity.ok().body(chatroomService.findAllParticipatingChatrooms(userId)));
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
