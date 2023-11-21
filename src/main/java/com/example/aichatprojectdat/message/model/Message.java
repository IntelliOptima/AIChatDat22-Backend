package com.example.aichatprojectdat.message.model;


import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

@Table
public record Message(
        @Id
        String id,

        Long userId,

        String textMessage,

        String chatroomId,

        @CreatedDate
        Instant createdDate,

        @LastModifiedDate
        Instant lastModifiedDate,

        @Version
        Long version
) {
        public static Message of(Long userId, String textMessage, String chatroomId ){
                return new Message(UUID.randomUUID().toString(), userId, textMessage, chatroomId, null, null, null);
        }

        public static Message ofGPTStream(String messageId, Long userId, String textMessage, String chatroomId, Instant createdDate ) {
                        return new Message(messageId, userId, textMessage, chatroomId, createdDate, null, null);
        }

        public static Message emptyButWithIdForTest(String uuid, Long userId, String textMessage, String chatroomId) {
                return new Message(uuid, userId, textMessage, chatroomId, null, null, null);
        }
}
