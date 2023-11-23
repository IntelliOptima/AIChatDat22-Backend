package com.example.aichatprojectdat.chatroom.model;

import com.example.aichatprojectdat.message.model.Message;
import com.example.aichatprojectdat.user.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "chatroom")
public class Chatroom {

    @Id
    @Builder.Default
    private String id = UUID.randomUUID().toString(); //Important that this gets instantiated as an UUID()

    private Long chatroomUserCreatorId;

    private String chatroomName;

    private String color;

    @Transient
    private List<User> users;

    @Transient
    private List<Message> messages;

    @CreatedDate
    private Instant createdDate;

    @LastModifiedDate
    private Instant lastModifiedDate;

    @Version
    private Long version;
}