package com.example.aichatprojectdat.user.service;

import com.example.aichatprojectdat.user.model.User;
import com.example.aichatprojectdat.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final UserRepository userRepository;

    @Override
    public Mono<User> create(User user) {
        return userRepository.save(user);
    }
}
