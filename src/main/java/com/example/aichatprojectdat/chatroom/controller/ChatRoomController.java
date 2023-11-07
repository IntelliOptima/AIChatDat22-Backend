package com.example.aichatprojectdat.chatroom.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Controller;

import com.example.aichatprojectdat.chatroom.service.IChatRoomUsersRelationService;
import com.example.aichatprojectdat.message.model.Message;
import com.example.aichatprojectdat.message.service.IMessageService;
import com.example.aichatprojectdat.user.model.User;

import lombok.RequiredArgsConstructor;

import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class ChatRoomController {

    private final IMessageService messageService;
    private final IChatRoomUsersRelationService chatRoomUsersRelationService;
    private final Map<Long, List<RSocketRequester>> subscribers = new ConcurrentHashMap<>();

    @MessageMapping("chat.{chatroomId}.subscribe")
    public Mono<ResponseEntity<String>> subscribe(@DestinationVariable Long chatroomId, User user, RSocketRequester requester) {
        return chatRoomUsersRelationService.isUserPartOfChatroom(user.id(), chatroomId)
                .map(userRelationExist -> {
                    if (userRelationExist) {
                        subscribers.computeIfAbsent(chatroomId, id -> new ArrayList<>()).add(requester);
                        return ResponseEntity.ok().body("Subscribed to chatroom " + chatroomId);
                    } else {
                        return ResponseEntity.badRequest().body("User is not part of chatroom " + chatroomId);
                    }
                });
    }



    @MessageMapping("chat.{chatroomId}")
    public Mono<Void> getChat(Message message, @DestinationVariable Long chatroomId) {
        return messageService.create(message)
                .doOnNext(createdMessage -> broadcastMessage(chatroomId, createdMessage))
                .then();
    }

    private void broadcastMessage(Long chatroomId, Message message) {
        List<RSocketRequester> chatSubscribers = subscribers.get(chatroomId);
        if (chatSubscribers != null) {
            chatSubscribers.forEach(requester -> requester.route("chat.message").data(message).send());
        }
    }

    


}
