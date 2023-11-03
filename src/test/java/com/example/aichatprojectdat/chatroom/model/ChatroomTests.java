package com.example.aichatprojectdat.chatroom.model;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ChatroomTests {

    @Test
    void whenCreatingNewChatroom_ReturnChatroomWithCreatorId() {
        long chatroomUserCreatorId = 1L;

        Chatroom chatroom = Chatroom.of(chatroomUserCreatorId);
        assertNull(chatroom.id(), "Id should be handled by Database, and always be null on creation");
        assertNull(chatroom.createdDate(), "Id should be handled by Database, and always be null on creation");
        assertEquals(chatroom.chatroomUserCreatorId(), chatroomUserCreatorId, "ChatroomUserCreatorId should be equal to the user that creates id");
    }
}
