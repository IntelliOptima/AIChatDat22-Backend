package com.example.aichatprojectdat.chatroom.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table
public record Chatroom(
        @Id
        Long id,

        Long chatroomUserCreatorId,

        @CreatedDate
        Instant createdDate
) {
    public static Chatroom of(Long chatroomUserCreatorId) {
        return new Chatroom(null, chatroomUserCreatorId, null);
    }
}