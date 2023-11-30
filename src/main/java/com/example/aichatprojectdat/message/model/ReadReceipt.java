package com.example.aichatprojectdat.message.model;

import org.springframework.data.relational.core.mapping.Table;

@Table(name = "read_receipt")
public record ReadReceipt (
        String messageId,
        Long userId,
        Boolean hasRead
) {
    public static ReadReceipt of(String messageId, Long userId, Boolean hasRead) {
        return new ReadReceipt(messageId, userId, hasRead);
    }
}