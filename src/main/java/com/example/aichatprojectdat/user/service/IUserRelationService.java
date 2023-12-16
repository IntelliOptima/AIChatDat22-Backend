package com.example.aichatprojectdat.user.service;

import com.example.aichatprojectdat.user.model.User;
import com.example.aichatprojectdat.user.model.UserRelation;
import com.example.aichatprojectdat.user.model.UserRelationRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IUserRelationService {

    Flux<UserRelation> getAllUserRelationsFromUserId(Long userId);

    Flux<User> getAllUserFriendsFromUserId(Long userId);

    Mono<User> createUserRelation(UserRelationRequest userRelationRequest);
}
