package com.example.aichatprojectdat.user.service;

import com.example.aichatprojectdat.user.model.User;
import com.example.aichatprojectdat.user.model.UserRelation;
import com.example.aichatprojectdat.user.model.UserRelationRequestDTO;
import com.example.aichatprojectdat.user.repository.PendingRelationRequestRepository;
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
public class UserRelationServiceImpl implements IUserRelationService {

    private final UserRelationRepository userRelationRepository;
    private final UserRepository userRepository;
    private final PendingRelationRequestRepository pendingRelationRequestRepository;

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
    public Mono<User> createUserRelation(UserRelationRequestDTO userRelationRequestDTO) {
        return userRepository.findUserByEmail(userRelationRequestDTO.getEmailRequest())
                .flatMap(user -> {
                    if (!user.id().equals(userRelationRequestDTO.getUserRequester().id())) {
                        return pendingRelationRequestRepository.deleteByRequesterIdAndReceiverId(
                                        user.id(), userRelationRequestDTO.getUserRequester().id())
                                .then(userRelationRepository.save(UserRelation.builder()
                                        .userId(userRelationRequestDTO.getUserRequester().id())
                                        .friendId(user.id())
                                        .build()))
                                .thenReturn(user);
                    } else {
                        return Mono.error(new RuntimeException("Cannot create a relation with oneself"));
                    }
                });
    }

    @Override
    public Mono<User> deleteUserRelation(UserRelationRequestDTO userRelationRequestDTO) {
        Long userId = userRelationRequestDTO.getUserRequester().id();
        String friendEmail = userRelationRequestDTO.getEmailRequest();

        return userRepository.findUserByEmail(friendEmail)
                .flatMap(friend -> {
                    Long friendId = friend.id();

                    if (!userId.equals(friendId)) {
                        return userRelationRepository.deleteByFriendIdAndAndUserId(friendId, userId)
                                .thenReturn(friend);
                    } else {
                        return Mono.error(new RuntimeException("Cannot delete a relation with oneself"));
                    }
                });
    }


}
