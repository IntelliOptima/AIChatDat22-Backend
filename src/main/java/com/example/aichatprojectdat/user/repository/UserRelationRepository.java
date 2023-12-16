package com.example.aichatprojectdat.user.repository;

import com.example.aichatprojectdat.user.model.User;
import com.example.aichatprojectdat.user.model.UserRelation;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;


public interface UserRelationRepository extends R2dbcRepository<UserRelation, Long> {

    Flux<UserRelation> findAllByUserId(Long userId);
}
