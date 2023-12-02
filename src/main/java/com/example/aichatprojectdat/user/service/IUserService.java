package com.example.aichatprojectdat.user.service;

import com.example.aichatprojectdat.user.model.User;
import reactor.core.publisher.Mono;

public interface IUserService {
    Mono<User> findOrCreate(User user);
}
