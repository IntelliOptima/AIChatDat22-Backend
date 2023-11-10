package com.example.aichatprojectdat.user.service;

import com.example.aichatprojectdat.user.exception.CustomDuplicateUserException;
import com.example.aichatprojectdat.user.model.User;
import com.example.aichatprojectdat.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Primary
@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final UserRepository userRepository;

    @Override
    public Mono<User> findUserByEmail(String email) {
        return userRepository.findUserByEmail(email);
    }

    @Override
    public Mono<User> create(User user) {
        return findUserByEmail(user.email())
                .switchIfEmpty(Mono.defer(() -> userRepository.save(user)));
    }
}
