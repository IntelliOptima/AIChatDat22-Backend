package com.example.aichatprojectdat.user.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class PendingRelationRequestDTO {

    private Long requesterId;

    private Long receiverId;
}
