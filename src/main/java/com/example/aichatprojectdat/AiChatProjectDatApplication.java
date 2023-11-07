package com.example.aichatprojectdat;

import com.example.aichatprojectdat.chatroom.model.Chatroom;
import com.example.aichatprojectdat.chatroom.model.ChatroomUsersRelation;
import com.example.aichatprojectdat.chatroom.service.ChatroomUsersRelationService;
import com.example.aichatprojectdat.chatroom.service.IChatRoomUsersRelationService;
import com.example.aichatprojectdat.chatroom.service.IChatroomService;
import com.example.aichatprojectdat.message.model.Message;
import com.example.aichatprojectdat.message.service.IMessageService;
import com.example.aichatprojectdat.user.model.User;
import com.example.aichatprojectdat.user.service.IUserService;
import com.sun.jdi.LongValue;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

@SpringBootApplication
@EnableR2dbcAuditing
@EnableAspectJAutoProxy
public class AiChatProjectDatApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiChatProjectDatApplication.class, args);
    }

    @Bean
    CommandLineRunner commandLineRunner(
            IChatroomService chatroomService,
            IMessageService messageService,
            IUserService userService,
            IChatRoomUsersRelationService chatRoomUsersRelationService
    ) {
        return args -> {
//            // Create Users and Chatrooms in sequence
//            Flux.fromStream(LongStream.range(1, 4).boxed())
//                    .flatMap(i -> userService.create(User.of("test" + i + "@test.dk", "Alex" + i))
//                            .then(chatroomService.create(Chatroom.builder()
//                                    .chatroomUserCreatorId(i)
//                                    .build()))
//                            .then(chatRoomUsersRelationService.create(ChatroomUsersRelation.of(String.valueOf(i), i))))
//
//                    .thenMany(
//                            // Create Messages after Users and Chatrooms are created
//                            Flux.fromStream(LongStream.range(1, 4).boxed())
//                                    .flatMap(i -> Flux.fromStream(LongStream.range(1, 10).boxed())
//                                            .flatMap(j -> messageService.create(Message.of(i, "TESTER" + j,
//                                                    chatroomService.findAll().then(chatroom -> chatroom.)))
//                                            )
//                                    )
//                    )
//                    .then(chatroomService.findById(chatroomIds.get(0)))
//                    .doOnNext(chatroom -> {
//                        System.out.println(chatroom.getMessages().size());
//                        System.out.println(chatroom.getUsers().size());
//                    })
//                    .onErrorContinue((throwable, o) -> {
//                        // Handle errors here
//                        System.err.println("An error occurred: " + throwable.getMessage());
//                    })
//                    .subscribe();
//
//

            /*
            Mono<Message> messageMono = messageService.getMessageById(1L)
                            .flatMap(message -> {
                                System.out.println(message.toString());
                                Message updatedMessage = new Message(
                                        message.id(),
                                        message.userId(),
                                        "ITS OVERWRITTEN AND MANIPULATED!",
                                        message.chatroomId(),
                                        message.createdDate(),
                                        message.lastModifiedDate(),
                                        message.version()
                                );
                                return messageService.create(updatedMessage);
                            });
            messageMono.subscribe(System.out::println);
             */

        };
    }

}
