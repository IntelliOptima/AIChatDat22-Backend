package com.example.aichatprojectdat.user.service;

import com.example.aichatprojectdat.user.model.User;
import com.example.aichatprojectdat.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final UserRepository userRepository;

    @Override
    public Mono<User> createOrReturnExistingUser(User user) {
        return userRepository.findUserByEmail(user.email())
                .flatMap(Mono::justOrEmpty)
                .switchIfEmpty(userRepository.save(user));
    }

    @Override
    public Flux<User> findUsersByIdIn(Iterable<Long> userIds) {
        return userRepository.findUsersByIdIn(userIds);
    }
}
