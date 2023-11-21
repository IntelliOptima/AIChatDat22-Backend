package com.example.aichatprojectdat;

import com.example.aichatprojectdat.user.model.User;
import com.example.aichatprojectdat.user.service.IUserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;

import java.util.Arrays;

@SpringBootApplication
@EnableR2dbcAuditing
@EnableAspectJAutoProxy
public class AiChatProjectDatApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiChatProjectDatApplication.class, args);
    }

    @Bean
    CommandLineRunner commandLineRun(IUserService userService) {
        return args -> {
            userService.create(User.chatGPTUser()).block();
            userService.create(User.DallE()).block();
        };
    }

//    @Bean
//    CommandLineRunner commandLineRunner(
//            IChatroomService chatroomService,
//            IMessageService messageService,
//            IUserService userService,
//            IChatRoomUsersRelationService chatRoomUsersRelationService
//    ) {
//        return args -> {
//            // Create Users and Chatrooms in sequence
//            List<String> chatroomIds = Stream.of(2, 11)
//                    .map(i -> UUID.randomUUID().toString())
//                    .toList();
//            userService.create(User.chatGPTUser()).block();
//
//            chatroomIds.forEach(chatroomId -> {
//                userService.createOrRetucrnExistingUser(User.of("test" + chatroomId + "@test.com", "alex", null))
//                        .flatMap(user -> chatroomService.create(Chatroom.builder()
//                                            .id(chatroomId)
//                                            .chatroomUserCreatorId(user.id())
//                                            .build())
//                                            .flatMap(chatroom -> chatRoomUsersRelationService.create(
//                                                    ChatroomUsersRelation.of(chatroom.getId(), user.id()))
//                                                    .flatMap(chatroomUsersRelation -> userService.create(
//                                                            User.of("anotherTest - "+ chatroomId + " - @test.dk", "TEQNO", null)))
//                                                    .flatMap(anotherUser -> chatRoomUsersRelationService.create(ChatroomUsersRelation.of(chatroomId, anotherUser.id())))
//
//
//                        ))
//                        .subscribe();
//
//            });
//
//        };
//        };
//    }


}
