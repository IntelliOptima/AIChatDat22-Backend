package com.example.aichatprojectdat.user.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
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

      String profileImage,

      @CreatedDate
      Instant createdDate,

      @LastModifiedDate
      Instant lastModifiedDate,

      @Version
      Long version
) {
    public static User of(String email, String fullName, String profileImage) {
        return new User(null, email, fullName, profileImage, null, null, null);
    }


    public static User chatGPTUser() {
        return new User(1L, "chatgpt@chatgpt.com", "ChatGPT", "https://th.bing.com/th/id/OIF.pu6JgOrk5eTBtCEFGtCR7Q?pid=ImgDet&rs=1", null, null, null);
    }

    public static User DallE() {
        return new User(2L, "dalle@chatgpt.com", "Dall-E", "https://www.google.com/url?sa=i&url=https%3A%2F%2Fwww.magnolia-cms.com%2Fmarketplace%2Fdetail%2Fdall-e.html&psig=AOvVaw2g7wAwzEuIi7zeJ7Q7glAO&ust=1700613408374000&source=images&cd=vfe&ved=0CBEQjRxqFwoTCOiFveLs04IDFQAAAAAdAAAAABAE", null, null, null);
    }
}
