package com.example.aichatprojectdat.chatroom.repository;

import com.example.aichatprojectdat.chatroom.model.Chatroom;
import com.example.aichatprojectdat.chatroom.model.ChatroomUsersRelation;
import com.example.aichatprojectdat.chatroom.service.IChatRoomUsersRelationService;
import com.example.aichatprojectdat.chatroom.service.IChatroomService;
import com.example.aichatprojectdat.integration.AbstractIntegrationTest;
import com.example.aichatprojectdat.user.model.User;
import com.example.aichatprojectdat.user.service.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ChatroomRepositoryTests extends AbstractIntegrationTest {


    List<User> users = new ArrayList<>();

    @Autowired
    private IUserService userService;

    @Autowired
    private IChatroomService chatroomService;

    @Autowired
    private IChatRoomUsersRelationService chatRoomUsersRelationService;

    @BeforeEach
    void createUserForChatroom() {
       users.clear();
       users.add(userService.create(User.of("Alexander@hotmail.com", "Alexander Bø")).block());
       users.add(userService.create(User.of("Alex@hotmail.com", "Holmberg")).block());
       users.add(userService.create(User.of("Mikkel@hotmail.com", "Mikkel Fun")).block());
       users.add(userService.create(User.of("Oliver@hotmail.com", "Oliver")).block());
    }


    @Test
    void whenChatroomCreated_ChatroomSavedInDB_AndUserRelationCreated_ReturnChatroomAndRelation() {
        User user = users.get(0);
        // Create the chatroom and then create the ChatroomUsersRelation using flatMap
        Mono<Tuple2<Chatroom, ChatroomUsersRelation>> resultMono = chatroomService.create(Chatroom.builder()
                        .chatroomUserCreatorId(user.id()).build())

                .flatMap(chatroom ->
                        chatRoomUsersRelationService.create(ChatroomUsersRelation.of(user.id(), chatroom.getId()))
                                .map(relation -> Tuples.of(chatroom, relation)) // Combine the chatroom and relation into a Tuple
                );

        // Use StepVerifier to test the composed mono
        StepVerifier.create(resultMono)
                .consumeNextWith(tuple -> {
                    Chatroom chatroom = tuple.getT1();
                    ChatroomUsersRelation relation = tuple.getT2();

                    // Assertions for Chatroom
                    assertTrue(chatroom.getCreatedDate().isBefore(Instant.now()));
                    assertEquals(chatroom.getChatroomUserCreatorId(), user.id());

                    // Assertions for ChatroomUsersRelation
                    assertNotNull(relation);
                    assertEquals(user.id(), relation.userId());
                    assertEquals(chatroom.getId(), relation.chatroomId());
                })
                .verifyComplete();
    }

    @Test
    void whenChatroomFetchedFromDb_AddUsersToChatroomAndStoreInDB_ReturnChatroomUsersRelation() {
        // Create a new chatroom for this specific test to avoid ID conflicts
        Chatroom chatroom = chatroomService.create(Chatroom.builder()
                .chatroomUserCreatorId(users.get(0).id()).build()).block();
        assertNotNull(chatroom); // Ensure the chatroom was created

        // Here we assume chatroom IDs are generated correctly by the service/database
        long chatroomId = chatroom.getId();

        Mono<Tuple2<Chatroom, List<ChatroomUsersRelation>>> resultMono = chatroomService.findById(chatroomId)
                .flatMap(existingChatroom ->
                        Flux.fromIterable(users)
                                .flatMap(user -> chatRoomUsersRelationService.create(ChatroomUsersRelation
                                        .of(existingChatroom.getId(), user.id())))
                                .collectList() // Collect relations after creation
                                .map(chatroomUsersRelations -> Tuples.of(existingChatroom, chatroomUsersRelations))
                );

        StepVerifier.create(resultMono)
                .consumeNextWith(tuple -> {
                    Chatroom foundChatroom = tuple.getT1();
                    List<ChatroomUsersRelation> chatroomUsersRelations = tuple.getT2();

                    // Assert the chatroom is the one we found by ID
                    assertEquals(foundChatroom.getId(), chatroomId);

                    // Now we verify the size of the relations list
                    assertEquals(users.size(), chatroomUsersRelations.size());

                    // And that each user ID is present in the chatroom relations
                    users.forEach(user -> assertTrue(
                            chatroomUsersRelations.stream()
                                    .anyMatch(relation -> relation.userId().equals(user.id()))
                    ));
                })
                .verifyComplete();
    }
}