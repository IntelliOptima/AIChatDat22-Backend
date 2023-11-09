package com.example.aichatprojectdat.user.repository;

import com.example.aichatprojectdat.user.model.User;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public interface UserRepository extends R2dbcRepository<User, Long> {

    Mono<User> findUserByEmail(String email);
    Flux<User> findUsersByIdIn(Iterable<Long> ids);
}
