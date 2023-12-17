package com.example.aichatprojectdat.user.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "user_relation")
public class UserRelation {

    @Id
    private Long id;

    private Long userId;

    private Long friendId;

}
