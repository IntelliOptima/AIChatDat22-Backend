package com.example.aichatprojectdat.chatroom.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table
public record ChatroomUsersRelation(
        @Id
        Long id,
        String chatroomId,
        Long userId
) {

    public static ChatroomUsersRelation of(String chatroomId, Long userId) {
        return new ChatroomUsersRelation(null, chatroomId, userId);
    }
}
