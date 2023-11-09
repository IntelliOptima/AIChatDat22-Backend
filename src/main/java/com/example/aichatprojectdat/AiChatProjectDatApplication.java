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
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

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

            userService.createOrReturnExistingUser(User.chatGPTUser()).subscribe();

            // Create Users and Chatrooms in sequence
            List<String> chatroomIds = Stream.of(1, 4)
                    .map(i -> UUID.randomUUID().toString())
                    .toList();

            chatroomIds.forEach(chatroomId ->
                    userService.createOrReturnExistingUser(User.of("test1@mail.com", "Alex1", null))
                            .then(chatroomService.create(Chatroom.builder()
                                    .id(chatroomId)
                                    .chatroomUserCreatorId(2L) // Assuming the user ID of the created user is 1L
                                    .build()))
                            .thenMany(Flux.fromStream(LongStream.range(1, 4).boxed())
                                    .flatMap(aLong -> userService.createOrReturnExistingUser(User.of("test" + aLong + "@mail.com", "Alex" + aLong, null))
                                            .flatMap(user -> chatRoomUsersRelationService.create(ChatroomUsersRelation.of(chatroomId, user.id())))
                                    )
                            )
                            .thenMany(Flux.fromStream(LongStream.range(1, 4).boxed())
                                    .flatMap(userId -> Flux.fromStream(LongStream.range(1, 10).boxed())
                                            .flatMap(messageIndex -> messageService.create(Message.of(userId, "TESTER" + messageIndex, chatroomId)))
                                    )
                            )
                            .then(chatroomService.findById(chatroomId))
                            .doOnNext(chatroom -> {
                                System.out.println("Number of messages: " + chatroom.getMessages().size());
                                System.out.println("Number of users: " + chatroom.getUsers().size());
                            })
                            .onErrorContinue((throwable, o) -> {
                                System.err.println("An error occurred: " + throwable.getMessage());
                            })
                            .subscribe()
            );
        };
    }


}
