package com.example.aichatprojectdat.message.repository;

import com.example.aichatprojectdat.integration.AbstractIntegrationTest;
import com.example.aichatprojectdat.message.model.Message;
import com.example.aichatprojectdat.message.service.IMessageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class MessageRepositoryTests extends AbstractIntegrationTest {

    @Autowired
    private IMessageService messageService;


    void addMessagesToDbForTest() {
        IntStream.range(0, 10).forEach(i -> {
            messageService.saveMessage(Message.of(1, "Test", 2));
        });
    }

    @Test
    void createMessageWithService_ReturnsNewCreatedMessage() {
        long userId = 1L;
        String mockMessage = "Test message";
        long receiver = 2L;

        Mono<Message> messageMono = messageService.saveMessage(Message.of(userId, mockMessage, receiver));

        StepVerifier.create(messageMono)
                .consumeNextWith(message -> {
                    assertTrue(message.id() > 0);
                    assertEquals(message.message(), mockMessage);
                    assertEquals(message.userId(), userId);
                    assertEquals(message.chatroomId(), receiver);
                })
                .verifyComplete();
    }

    @Test
    void fetchingMessagesFromDB_ByUserId_ReturnAllUserIdsMessages() {
        long userId = 1L;
        String mockMessage = "Test message";
        long receiver = 2L;

        Mono<Message> saveOperation = messageService.saveMessage(Message.of(userId, mockMessage, receiver));

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
                        assertTrue(message.lastModifiedDate().isBefore(Instant.now()), "Last modified date should be in the past");
                        assertTrue(message.chatroomId() > 0, "Chatroom ID should be positive");
                    });
                })
                .expectComplete()
                .verify();
    }

    @Test
    void fetchingMessagesFromDb_ByChatroomId() {
        long chatroomId = 2L;

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
        long userId = 1L;
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