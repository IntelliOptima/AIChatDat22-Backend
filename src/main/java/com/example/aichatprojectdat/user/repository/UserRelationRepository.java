package com.example.aichatprojectdat.user.repository;

import com.example.aichatprojectdat.user.model.UserRelation;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface UserRelationRepository extends R2dbcRepository<UserRelation, Long> {
}
