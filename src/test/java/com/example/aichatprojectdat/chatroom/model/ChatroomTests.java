package com.example.aichatprojectdat.chatroom.model;


import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ChatroomTests {

    @Test
    void whenCreatingNewChatroom_ReturnChatroomWithCreatorId() {
        long chatroomUserCreatorId = 2L;

        Chatroom chatroom = Chatroom.builder()
                .id(UUID.randomUUID().toString())
                .chatroomUserCreatorId(chatroomUserCreatorId)
                .build();
        assertFalse(chatroom.getId().isEmpty(), "Id should be set beforestored in DB, and always be instantiated on creation");
        assertNull(chatroom.getCreatedDate(), "Id should be handled by Database, and always be null on creation");
        assertEquals(chatroom.getChatroomUserCreatorId(), chatroomUserCreatorId, "ChatroomUserCreatorId should be equal to the user that creates id");
    }
}
