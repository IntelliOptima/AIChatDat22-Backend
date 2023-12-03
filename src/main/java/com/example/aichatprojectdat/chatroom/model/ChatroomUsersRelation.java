package com.example.aichatprojectdat.chatroom.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table
public record ChatroomUsersRelation(
        @Id
        Long id,
        String chatroomId,
        Long userId,

        @CreatedDate
        Instant createdDate,

        @LastModifiedDate
        Instant lastModifiedDate,

        @Version
        Long version
) {

    public static ChatroomUsersRelation of(String chatroomId, Long userId) {
        return new ChatroomUsersRelation(null, chatroomId, userId, null, null, null);
    }
}
