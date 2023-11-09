package com.example.aichatprojectdat.user.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.time.LocalDate;

@Table
public record User (
      @Id
      Long id,

      @Email
      @NotEmpty
      String email,

      @NotEmpty
      @NotNull
      String fullName,

      @CreatedDate
      Instant createdDate,

      @LastModifiedDate
      Instant lastModifiedDate,

      @Version
      Long version
) {
    public static User of(String email, String fullName) {
        return new User(null, email, fullName, null, null, null);
    }


    public static User chatGPTUser() {
        return new User(1L, "chatgpt@chatgpt.com", "CHATGPT", null, null, null);
    }
}
