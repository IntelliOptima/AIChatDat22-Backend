package com.example.aichatprojectdat.chatroom.controller;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import com.example.aichatprojectdat.ChatGpt.service.IChatGPTService;
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

import javax.swing.plaf.synth.SynthOptionPaneUI;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatroomController {
    private final IMessageService messageService;
    private final IChatGPTService chatGPTService;

    private final IChatRoomUsersRelationService chatRoomUsersRelationService;
    private final Map<String, List<RSocketRequester>> subscribers = new ConcurrentHashMap<>();
    private final Map<String, Sinks.Many<Message>> chatroomSinks = new ConcurrentHashMap<>();

    //_______________________ RSOCKET -> CHANNEL _______________________________

    // Method for clients to connect and stream chatroom messages
    @MessageMapping("chat.stream.{chatroomId}")
    public Flux<Message> streamMessages(
            @DestinationVariable String chatroomId,
            Mono<String> requestMessage
    ) {

        return requestMessage.doOnNext(s -> System.out.println("Received message text: " + s))
                .thenMany(chatroomSinks.computeIfAbsent(chatroomId, id -> Sinks.many().replay().latest()).asFlux()
                        .doOnCancel(() -> {
                            // Handle cancellation such as a user leaving a chatroom, if necessary
                        }));
    }

    // Method for clients to send messages to a chatroom
    @MessageMapping("chat.send.{chatroomId}")
    public void receiveMessage(@DestinationVariable String chatroomId, Message chatMessage, RSocketRequester requester) {

        System.out.println(chatMessage.textMessage());
        // Retrieve or create a new sink for the chatroom
        Sinks.Many<Message> sink = chatroomSinks.computeIfAbsent(chatroomId, id -> Sinks.many().replay().latest());

        // Emit the message to the sink
        sink.emitNext(chatMessage, Sinks.EmitFailureHandler.FAIL_FAST);

        // Check if the message starts with "@gpt" and handle accordingly
        if (chatMessage.textMessage().toLowerCase().startsWith("@gpt")) {
            String question = chatMessage.textMessage().split("@gpt", 2)[1];
            chatGPTService.getAnswerFromGPT(question)
                    .subscribe(choices -> {
                        Message gptMessage = Message.of(999L, choices.get(0).getGptMessage().getContent(), chatroomId);
                        sink.emitNext(gptMessage, Sinks.EmitFailureHandler.FAIL_FAST);
                    });
        }
    }


    // Optionally, you might want to clean up sinks when they are no longer being used
    // For example, when a chatroom becomes empty, you could remove its sink from the map
    private void removeSinkIfNoSubscribers(String chatroomId) {
        Sinks.Many<Message> sink = chatroomSinks.get(chatroomId);
        if (sink != null && sink.currentSubscriberCount() == 0) {
            chatroomSinks.remove(chatroomId);
        }
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