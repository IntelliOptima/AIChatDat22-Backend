package com.example.aichatprojectdat.message.model;


import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;

import com.example.aichatprojectdat.config.aspects.AdviceAnnotations.ValidateParams;

import jakarta.validation.constraints.NotNull;

@Table
public record Message(
        @Id
        Long id,
        
        long userId,

        @NotNull
        String message,

        long chatroomId,

        @CreatedDate
        Instant createdDate,

        @LastModifiedDate
        Instant lastModifiedDate
) {
        @ValidateParams
        public static Message of(long userId, String message, long chatroomId) {
                return new Message(null, userId, message, chatroomId, null, null);
        }
}
