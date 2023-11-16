package com.example.aichatprojectdat.chatroom.controller;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.example.aichatprojectdat.ChatGpt.service.GPTServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Controller;

import com.example.aichatprojectdat.chatroom.service.IChatRoomUsersRelationService;
import com.example.aichatprojectdat.message.model.Message;
import com.example.aichatprojectdat.message.service.IMessageService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.CrossOrigin;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Slf4j
@Controller
@RequiredArgsConstructor
@CrossOrigin
public class ChatroomController {
    private final IMessageService messageService;

    private final GPTServiceImpl gptService;

    private final IChatRoomUsersRelationService chatRoomUsersRelationService;
    private final Map<String, List<RSocketRequester>> subscribers = new ConcurrentHashMap<>();
    private final Map<String, Sinks.Many<Message>> chatroomSinks = new ConcurrentHashMap<>();
    private final Map<String, Sinks.Many<String>> gptAnswerSinks = new ConcurrentHashMap<>();

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

        messageService.create(chatMessage)
                .doOnError(e -> System.out.println("Error creating message: " + e.getMessage()))
                .subscribe();

        Sinks.Many<Message> sink = chatroomSinks.computeIfAbsent(chatroomId, id -> Sinks.many().replay().latest());
        sink.emitNext(chatMessage, Sinks.EmitFailureHandler.FAIL_FAST);

        if (chatMessage.textMessage().toLowerCase().startsWith("@gpt")) {
            String question = chatMessage.textMessage().substring(4);
            StringBuilder gptResponseBuilder = new StringBuilder();

            gptService.chat(question)
                    .doOnError(e -> System.out.println("Error getting GPT answer: " + e.getMessage()))
                    .subscribe(gptChunk -> {
                        gptResponseBuilder.append(gptChunk);
                        // Emit each chunk as it arrives for real-time streaming
                        Sinks.Many<String> answerSink = gptAnswerSinks.computeIfAbsent(chatroomId, id -> Sinks.many().replay().latest());
                        answerSink.emitNext(gptChunk, Sinks.EmitFailureHandler.FAIL_FAST);
                    }, err -> {}, () -> {
                        // Once all chunks are received, create and store the complete GPT message
                        Message gptCompleteMessage = Message.of(1L, gptResponseBuilder.toString(), chatroomId);
                        messageService.create(gptCompleteMessage).subscribe();
                        sink.emitNext(gptCompleteMessage, Sinks.EmitFailureHandler.FAIL_FAST);
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
*/

}