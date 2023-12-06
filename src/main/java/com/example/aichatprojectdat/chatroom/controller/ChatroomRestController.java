package com.example.aichatprojectdat.chatroom.controller;

import com.example.aichatprojectdat.chatroom.model.Chatroom;
import com.example.aichatprojectdat.chatroom.model.ChatroomUsersRelation;
import com.example.aichatprojectdat.chatroom.service.IChatRoomUsersRelationService;
import com.example.aichatprojectdat.chatroom.service.IChatroomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@CrossOrigin
@Slf4j
@RequestMapping("/api/v1/chatroom")
public class ChatroomRestController {
    private final IChatroomService chatroomService;
    private final IChatRoomUsersRelationService chatRoomUsersRelationService;

    @PostMapping
    Mono<Chatroom> create(@RequestBody Chatroom newChatroom) {
        log.info("Creating new chatroom: " + newChatroom);
        return chatroomService.create(newChatroom);
    }

    @PostMapping("/addUser/{chatroomId}")
    Mono<ChatroomUsersRelation> addUserToChatroom(@PathVariable String chatroomId, @RequestBody String email) {
        log.info("Adding user" +  email  + " to chatroomId: " + chatroomId);
        return chatRoomUsersRelationService.addUserToChatroom(chatroomId, email);
    }

    @GetMapping("/participatingChatrooms/{userId}")
    Mono<ResponseEntity<Flux<Chatroom>>> getParticipatingChatrooms(@PathVariable Long userId) {
        return Mono.just(ResponseEntity.ok().body(chatroomService.findAllParticipatingChatrooms(userId)));
    }

    @GetMapping("/room/{id}")
    Mono<Chatroom> getChatroom(@PathVariable String id) {
        return chatroomService.findById(id);
    }

    @GetMapping("/{creatorId}")
    public Flux<Chatroom> getChatroomByCreatorId(@PathVariable Long creatorId) {
        return chatroomService.findAllByCreatorId(creatorId);
    }

    @PostMapping("/delete/{chatroomId}")
    public Mono<ResponseEntity<Mono<Chatroom>>> deleteChatroom(@PathVariable String chatroomId) {
        return Mono.just(ResponseEntity.ok().body(chatroomService.delete(chatroomId)));
    }

    @PostMapping("/leave/{chatroomId}/{userId}")
    public Mono<ResponseEntity<Mono<Void>>> leaveChatroom(@PathVariable String chatroomId, @PathVariable Long userId) {
        return Mono.just(ResponseEntity.ok().body(chatRoomUsersRelationService.leaveChatroom(chatroomId, userId)));
    }




}
