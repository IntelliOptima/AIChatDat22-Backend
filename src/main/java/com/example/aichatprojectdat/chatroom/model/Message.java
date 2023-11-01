package com.example.aichatprojectdat.chatroom.model;


import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;

public record Message(
        @Id
        Long id,
        String senderName,
        String message,

        @CreatedDate
        Instant date,

        @LastModifiedDate
        Instant lastModifiedDate
) {
        public static Message of(String senderName, String message) {
                return new Message(null, senderName, message, null, null);
        }
}
