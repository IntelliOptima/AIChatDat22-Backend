package com.example.aichatprojectdat.user.service;

import com.example.aichatprojectdat.user.model.User;
import com.example.aichatprojectdat.user.model.UserRelation;
import com.example.aichatprojectdat.user.model.UserRelationRequest;
import com.example.aichatprojectdat.user.repository.UserRelationRepository;
import com.example.aichatprojectdat.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Service
@Primary
@RequiredArgsConstructor
public class UserRelationServiceImpl implements IUserRelationService{

    private final UserRelationRepository userRelationRepository;
    private final UserRepository userRepository;

    @Override
    public Flux<UserRelation> getAllUserRelationsFromUserId(Long userId) {
        return userRelationRepository.findAllByUserId(userId);
    }

    public Flux<User> getAllUserFriendsFromUserId(Long userId) {
        return userRelationRepository.findAllByUserId(userId)
                .flatMap(userRelation -> userRepository.findById(userRelation.getFriendId()))
                .distinct();
    }

    @Override
    public Mono<User> createUserRelation(UserRelationRequest userRelationRequest) {
        return userRepository.findUserByEmail(userRelationRequest.getEmailRequest())
                .flatMap(user -> {
                    if (!user.id().equals(userRelationRequest.getUserRequester().id())) {
                        UserRelation newUserRelation = UserRelation.builder()
                                .userId(userRelationRequest.getUserRequester().id())
                                .friendId(user.id())
                                .build();
                        return userRelationRepository.save(newUserRelation)
                                .thenReturn(user);
                    } else {
                        return Mono.error(new RuntimeException("Cannot create a relation with oneself"));
                    }
                });
    }


}
