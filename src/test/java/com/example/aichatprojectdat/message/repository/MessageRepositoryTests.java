package com.example.aichatprojectdat.message.repository;

import com.example.aichatprojectdat.chatroom.model.Chatroom;
import com.example.aichatprojectdat.chatroom.service.IChatroomService;
import com.example.aichatprojectdat.integration.AbstractIntegrationTest;
import com.example.aichatprojectdat.message.model.Message;
import com.example.aichatprojectdat.message.service.IMessageService;
import com.example.aichatprojectdat.user.model.User;
import com.example.aichatprojectdat.user.service.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
public class MessageRepositoryTests extends AbstractIntegrationTest {

    @Autowired
    private IMessageService messageService;

    @Autowired
    private IUserService userService;

    @Autowired
    private IChatroomService chatroomService;


    private User testUser;
    private Chatroom testChatroom;
    private final String[] UUIDS_FOR_MESSAGES = IntStream.range(0, 10)
            .mapToObj(i -> UUID.randomUUID().toString())
            .toArray(String[]::new);

    @BeforeEach
    void createChatRoom() {
        testUser = userService.create(User.of("test@email.com", "HASHAN", null)).block();
        testChatroom = chatroomService.create(
                Chatroom.builder()
                        .id(UUID.randomUUID().toString())
                        .chatroomUserCreatorId(testUser.id())
                        .chatroomName("Test")
                        .build())
                .block();
    }


    void addMessagesToDbForTest() {
        IntStream.range(0, 10).forEach(i -> {
            messageService.create(
                    Message.emptyButWithIdForTest
                            (UUIDS_FOR_MESSAGES[i], testUser.id(), "Test", testChatroom.getId())).block();
        });
    }

    @Test
    void createMessageWithService_ReturnsNewCreatedMessage() {
        long userId = testUser.id();
        String mockMessage = "Test message";
        String chatroomId = testChatroom.getId();

        Mono<Message> messageMono = messageService.create(Message.of(userId, mockMessage, chatroomId));

        StepVerifier.create(messageMono)
                .consumeNextWith(message -> {
                    assertFalse(message.id().isEmpty());
                    assertEquals(message.textMessage(), mockMessage);
                    assertEquals(message.userId(), userId);
                    assertEquals(message.chatroomId(), chatroomId);
                })
                .verifyComplete();
    }

    @Test
    void fetchingMessagesFromDB_ByUserId_ReturnAllUserIdsMessages() {
        long userId = testUser.id();
        String mockMessage = "Test message";
        String chatroomId = testChatroom.getId();

        Mono<Message> saveOperation = messageService.create(Message.of(userId, mockMessage, chatroomId));

        // Ensure findAllByUserId is chained after the save operation completes
        Mono<List<Message>> messagesListMono = saveOperation
                .thenMany(messageService.getAllMessagesByUserId(userId))
                .collectList();

        StepVerifier.create(messagesListMono)
                .assertNext(messagesList -> {
                    assertFalse(messagesList.isEmpty(), "The messages list should not be empty");
                    messagesList.forEach(message -> {
                        assertEquals(userId, message.userId(), "User ID should match");
                        assertTrue(message.createdDate().isBefore(Instant.now()), "Created date should be in the past");
                        assertFalse(message.chatroomId().isEmpty(), "Chatroom ID shold not be empty -  UUID");
                    });
                })
                .expectComplete()
                .verify();
    }

    @Test
    void fetchingMessagesFromDb_ByChatroomId() {
        addMessagesToDbForTest();
        String chatroomId = testChatroom.getId();

        Mono<List<Message>> chatroomMessages = messageService.getMessagesByChatroomId(chatroomId).collectList();

        StepVerifier.create(chatroomMessages)
                .assertNext(messages -> {
                    assertFalse(messages.isEmpty(), "The list should not be empty");
                    messages.forEach(message -> {
                        assertEquals(message.chatroomId(), chatroomId);
                    });
                })
                .verifyComplete();
    }

    @Test
    void testDeleteMessageByIdSuccess() {
        addMessagesToDbForTest();
        long userId = testUser.id();
        String messageId = UUIDS_FOR_MESSAGES[0];

        // First ensure the message is there
        StepVerifier.create(messageService.getMessageById(messageId))
                .expectNextMatches(message -> message.id().equals(messageId))
                .verifyComplete();

        // Delete the message
        StepVerifier.create(messageService.deleteById(messageId))
                .verifyComplete();

        // Fetch all messages for the user and ensure the deleted message is not among them
        Mono<List<Message>> messagesListMinusDeleted = messageService.getAllMessagesByUserId(userId)
                .filter(message -> !message.id().equals(messageId)) // This ensures we don't get the deleted message
                .collectList();

        StepVerifier.create(messagesListMinusDeleted)
                .assertNext(messages -> assertFalse(messages.stream()
                                .anyMatch(message -> message.id().equals(messageId)),
                        "Deleted message should not be in the list"))
                .verifyComplete();
    }
}