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

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "chatroom")
public class Chatroom {

    @Id
    private Long id;

    private Long chatroomUserCreatorId;

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