package com.example.aichatprojectdat.message.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class MessageTests {

    @Test
    void whenInvokingStaticFactoryMethod_ReturnMessage() {
        long userId = 3L;
        String mockMessage = "A beautiful weather";
        String chatroomId = UUID.randomUUID().toString();

        Message message = Message.of(userId, mockMessage, chatroomId);

        assertNull(message.id(), "Id should be handled by Database, and always be null on creation");
        assertNull(message.createdDate(), "Id should be handled by Database, and always be null on creation");
        assertEquals(message.userId(), userId);
        assertEquals(message.textMessage(), mockMessage);
        assertEquals(message.chatroomId(), chatroomId);
    }


}
