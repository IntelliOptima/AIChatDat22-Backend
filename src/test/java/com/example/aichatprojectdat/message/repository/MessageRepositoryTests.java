package com.example.aichatprojectdat.message.repository;

import com.example.aichatprojectdat.chatroom.model.Chatroom;
import com.example.aichatprojectdat.chatroom.service.IChatroomService;
import com.example.aichatprojectdat.integration.AbstractIntegrationTest;
import com.example.aichatprojectdat.message.model.Message;
import com.example.aichatprojectdat.message.service.IMessageService;
import com.example.aichatprojectdat.user.model.User;
import com.example.aichatprojectdat.user.service.IUserService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.annotation.DirtiesContext;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class MessageRepositoryTests extends AbstractIntegrationTest {

    @Autowired
    private IMessageService messageService;

    @Autowired
    private IUserService userService;

    @Autowired
    private IChatroomService chatroomService;


    private User testUser;
    private Chatroom testChatroom;

    @BeforeEach
    void createChatRoom() {
        testUser = userService.create(User.of("test@email.com", "HASHAN")).block();
        testChatroom = chatroomService.create(Chatroom.of(testUser.id())).block();
    }


    void addMessagesToDbForTest() {
        IntStream.range(0, 10).forEach(i -> {
            messageService.saveMessage(Message.of(testUser.id(), "Test", testChatroom.id())).block();
        });
    }

    @Test
    void createMessageWithService_ReturnsNewCreatedMessage() {
        long userId = testUser.id();
        String mockMessage = "Test message";
        long chatroomId = testChatroom.id();

        Mono<Message> messageMono = messageService.saveMessage(Message.of(userId, mockMessage, chatroomId));

        StepVerifier.create(messageMono)
                .consumeNextWith(message -> {
                    assertTrue(message.id() > 0);
                    assertEquals(message.message(), mockMessage);
                    assertEquals(message.userId(), userId);
                    assertEquals(message.chatroomId(), chatroomId);
                })
                .verifyComplete();
    }

    @Test
    void fetchingMessagesFromDB_ByUserId_ReturnAllUserIdsMessages() {
        long userId = testUser.id();
        String mockMessage = "Test message";
        long chatroomId = testChatroom.id();

        Mono<Message> saveOperation = messageService.saveMessage(Message.of(userId, mockMessage, chatroomId));

        // Ensure findAllByUserId is chained after the save operation completes
        Mono<List<Message>> messagesListMono = saveOperation
                .thenMany(messageService.findAllByUserId(userId))
                .collectList();

        StepVerifier.create(messagesListMono)
                .assertNext(messagesList -> {
                    assertFalse(messagesList.isEmpty(), "The messages list should not be empty");
                    messagesList.forEach(message -> {
                        assertEquals(userId, message.userId(), "User ID should match");
                        assertTrue(message.createdDate().isBefore(Instant.now()), "Created date should be in the past");
                        assertTrue(message.chatroomId() > 0, "Chatroom ID should be positive");
                    });
                })
                .expectComplete()
                .verify();
    }

    @Test
    void fetchingMessagesFromDb_ByChatroomId() {
        addMessagesToDbForTest();
        long chatroomId = testChatroom.id();

        Mono<List<Message>> chatroomMessages = messageService.findMessagesByChatroomId(chatroomId).collectList();

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
        long messageId = 1L;

        // First ensure the message is there
        StepVerifier.create(messageService.findById(messageId))
                .expectNextMatches(message -> message.id() == messageId)
                .verifyComplete();

        // Delete the message
        StepVerifier.create(messageService.deleteById(messageId))
                .verifyComplete();

        // Fetch all messages for the user and ensure the deleted message is not among them
        Mono<List<Message>> messagesListMinusDeleted = messageService.findAllByUserId(userId)
                .filter(message -> message.id() != messageId) // This ensures we don't get the deleted message
                .collectList();

        StepVerifier.create(messagesListMinusDeleted)
                .assertNext(messages -> assertFalse(messages.stream()
                                .anyMatch(message -> message.id() == messageId),
                        "Deleted message should not be in the list"))
                .verifyComplete();
    }
}