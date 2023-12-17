package com.example.aichatprojectdat.user.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Table(name = "pending_relation_request")
public class PendingRelationRequest {

    @Id
    private Long id;

    private Long requesterId;

    private Long receiverId;

}
