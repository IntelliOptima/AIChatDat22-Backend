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

      String profileImage,

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
    public static User of(String email, String fullName, String profileImage) {
        return new User(null, email, profileImage ,fullName, null, null, null);
    }


    public static User chatGPTUser() {
        return new User(1L, "chatgpt@chatgpt.com", "CHATGPT", "https://th.bing.com/th/id/OIF.pu6JgOrk5eTBtCEFGtCR7Q?pid=ImgDet&rs=1", null, null, null);
    }
}
