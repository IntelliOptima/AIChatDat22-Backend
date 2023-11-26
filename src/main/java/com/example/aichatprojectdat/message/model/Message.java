package com.example.aichatprojectdat.message.model;


import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Table;


@Table(name = "message")
public record Message(
        @Id
        String id,

        Long userId,

        String textMessage,

        String chatroomId,

        @Transient
        Map<Long, Boolean> readReceipt,

        @CreatedDate
        Instant createdDate,

        @LastModifiedDate
        Instant lastModifiedDate,

        @Version
        Long version
) {
        public static Message of(Long userId, String textMessage, String chatroomId){
                return new Message(UUID.randomUUID().toString(), userId, textMessage, chatroomId, null, null, null, null);
        }

        public static Message readReceiptUpdate(String id, Long userId, String textMessage, String chatroomId, Map<Long, Boolean> readReceipt, Instant createdDate, Instant lastModifiedDate, Long version) {
                return new Message(id, userId, textMessage, chatroomId, readReceipt, createdDate, lastModifiedDate, version);
        }

        public static Message ofGPTStream(String messageId, Long userId, String textMessage, String chatroomId, Instant createdDate ) {
                        return new Message(messageId, userId, textMessage, chatroomId, Collections.emptyMap(), createdDate, null, null);
        }

        public static Message emptyButWithIdForTest(String uuid, Long userId, String textMessage, String chatroomId) {
                return new Message(uuid, userId, textMessage, chatroomId, null,null, null, null);
        }
}
