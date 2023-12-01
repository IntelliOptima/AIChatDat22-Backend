package com.example.aichatprojectdat.message.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class MessageTests {

    @Test
    void whenInvokingStaticFactoryMethod_ReturnMessage() {
        long userId = 2L;
        String mockMessage = "A beautiful weather";
        String chatroomId = UUID.randomUUID().toString();

        Message message = Message.builder()
                .userId(userId)
                .textMessage(mockMessage)
                .chatroomId(chatroomId)
                .build();

        assertNotNull(message.getId(), "Id should be handled by Database, and always be null on creation");
        assertNull(message.getCreatedDate(), "Id should be handled by Database, and always be null on creation");
        assertEquals(message.getUserId(), userId);
        assertEquals(message.getTextMessage(), mockMessage);
        assertEquals(message.getChatroomId(), chatroomId);
    }


}
