package com.example.aichatprojectdat.message.model;


import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

import com.example.aichatprojectdat.config.aspects.AdviceAnnotations.ValidateParams;

import jakarta.validation.constraints.NotNull;

@Table
public record Message(
        @Id
        Long id,

        Long userId,

        String message,

        Long chatroomId,

        @CreatedDate
        Instant createdDate,

        @LastModifiedDate
        Instant lastModifiedDate,

        @Version
        Long version
) {
        public static Message of(Long userId, String message, Long chatroomId ){
                return new Message(null, userId, message, chatroomId, null, null, null);
        }

        public static Message empty() {
                return new Message(null, null, null, null, null, null, null);
        }
}
