package com.example.aichatprojectdat.user.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class UserRelationRequestDTO {

    private User userRequester;
    private String emailRequest;
}
