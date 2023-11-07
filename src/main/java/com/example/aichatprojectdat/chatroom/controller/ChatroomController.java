package com.example.aichatprojectdat.chatroom.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Controller;

import com.example.aichatprojectdat.chatroom.service.IChatRoomUsersRelationService;
import com.example.aichatprojectdat.message.model.Message;
import com.example.aichatprojectdat.message.service.IMessageService;
import com.example.aichatprojectdat.user.model.User;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.function.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatroomController {
    private final IMessageService messageService;

    private final IChatRoomUsersRelationService chatRoomUsersRelationService;
    private final Map<String, List<RSocketRequester>> subscribers = new ConcurrentHashMap<>();

    //_______________________ RSOCKET -> CHANNEL _______________________________


    @MessageMapping("chat.{chatroomId}")
    public Flux<String> chatroom (
            @DestinationVariable String chatroomId,
            Flux<String> messages,
            RSocketRequester requester
    ) {
        // For testing, we are simply logging the message and returning it in the response
        // without any user validation or processing.
        return messages.map(message -> {
            log.info("Received message: {}", message);
            return "Server: " + message;
        });

//        return messages.flatMap(message -> {
//            Long userId = message.userId();
//            return chatRoomUsersRelationService.isUserPartOfChatroom(userId, chatroomId)
//                    .flatMapMany(userRelationExist -> {
//                        if (userRelationExist) {
//                            subscribers.computeIfAbsent(chatroomId, id -> new ArrayList<>()).add(requester);
//                            return Flux.just(ResponseEntity.ok(message));
//                        } else {
//                            return Flux.just(ResponseEntity.badRequest().body(Message.empty()));
//                        }
//                    });
//        });
    }



    /*
    FIRST TRY OF RSOCKET CHATROOM CHANNEL
    @MessageMapping("chat.{chatroomId}")
    public Mono<ResponseEntity<String>> subscribe(@DestinationVariable Long chatroomId, User user) {
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
*/
    @MessageMapping("request-response")
    public Mono<String> requestResponse(Mono<String> name){
        return name.map(inputName -> "Welcome " + inputName)
                .doOnNext(greet -> log.info("Received: {} And Returning {}", name, greet));
    }

}