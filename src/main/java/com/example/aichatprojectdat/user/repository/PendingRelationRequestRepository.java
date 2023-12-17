package com.example.aichatprojectdat.user.repository;

import com.example.aichatprojectdat.user.model.PendingRelationRequest;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface PendingRelationRequestRepository extends R2dbcRepository<PendingRelationRequest, Long> {

}
