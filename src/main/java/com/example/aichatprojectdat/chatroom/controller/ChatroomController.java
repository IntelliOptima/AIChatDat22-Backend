package com.example.aichatprojectdat.chatroom.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

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
    private final Map<String, Sinks.Many<Message>> chatroomSinks = new ConcurrentHashMap<>();

    //_______________________ RSOCKET -> CHANNEL _______________________________


    @MessageMapping("chat.{chatroomId}")
    public Flux<Message> handleChatMessage(
            @DestinationVariable String chatroomId,
            Flux<Message> incomingMessages,
            RSocketRequester requester
    ) {
        log.info("Connected to chatroom: {}", chatroomId);

        Sinks.Many<Message> sink = chatroomSinks.computeIfAbsent(
                chatroomId, id -> Sinks.many().multicast().directAllOrNothing());

        // Handle incoming messages and then return the live flux of messages for this chatroom
        return incomingMessages
                .flatMap(message -> messageService.create(message) // Save message reactively
                        .doOnNext(sink::tryEmitNext) // On successful save, emit the message to the sink
                        .onErrorResume(e -> {
                            log.error("Error processing message: {}", e.getMessage());
                            return Mono.empty();
                        })
                )
                .thenMany(sink.asFlux()); // After processing the incoming messages, return the flux of broadcasted messages
    }


//    @MessageMapping("chat.{chatroomId}")
//    public Flux<Message> chatroom (
//            @DestinationVariable String chatroomId,
//            Flux<Message> messages,
//            RSocketRequester requester
//    ) {
//
////        return messages.flatMap(message -> {
////            Long userId = message.userId();
////            return chatRoomUsersRelationService.isUserPartOfChatroom(userId, chatroomId)
////                    .flatMapMany(userRelationExist -> {
////                        if (userRelationExist) {
////                            subscribers.computeIfAbsent(chatroomId, id -> new ArrayList<>()).add(requester);
////                            return Flux.just(ResponseEntity.ok(message));
////                        } else {
////                            return Flux.just(ResponseEntity.badRequest().body(Message.empty()));
////                        }
////                    });
////        });
//    }



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