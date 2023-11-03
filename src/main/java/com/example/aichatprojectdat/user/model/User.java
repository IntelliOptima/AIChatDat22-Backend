package com.example.aichatprojectdat.user.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table
public record User (
      @Id
      Long id,

      @Email
      @NotEmpty
      String email,

      @NotEmpty
      @NotNull
      String fullName
) {
    public static User of(String email, String fullName) {
        return new User(null, email, fullName);
    }
}
