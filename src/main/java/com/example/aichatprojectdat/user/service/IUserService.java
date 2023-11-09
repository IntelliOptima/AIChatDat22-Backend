package com.example.aichatprojectdat.user.service;

import com.example.aichatprojectdat.user.model.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IUserService {
    Mono<User> createOrReturnExistingUser(User user);

    Flux<User> findUsersByIdIn(Iterable<Long> userIds);
}
