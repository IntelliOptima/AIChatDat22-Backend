package com.example.aichatprojectdat.message.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "message")
public class Message2 {

    @Id
    @Builder.Default
    String id = UUID.randomUUID().toString();

    Long userId;

    String textMessage;

    String chatroomId;

    @Transient
    Map<Long, Boolean> readReceipt;

    @CreatedDate
    Instant createdDate;

    @LastModifiedDate
    Instant lastModifiedDate;

    @Version
    Long version;
}
