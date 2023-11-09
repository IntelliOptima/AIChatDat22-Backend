package com.example.aichatprojectdat.chatroom.model;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ChatroomTests {

    @Test
    void whenCreatingNewChatroom_ReturnChatroomWithCreatorId() {
        long chatroomUserCreatorId = 2L;

        Chatroom chatroom = Chatroom.builder().chatroomUserCreatorId(chatroomUserCreatorId).build();
        assertNull(chatroom.getId(), "Id should be handled by Database, and always be null on creation");
        assertNull(chatroom.getCreatedDate(), "Id should be handled by Database, and always be null on creation");
        assertEquals(chatroom.getChatroomUserCreatorId(), chatroomUserCreatorId, "ChatroomUserCreatorId should be equal to the user that creates id");
    }
}
