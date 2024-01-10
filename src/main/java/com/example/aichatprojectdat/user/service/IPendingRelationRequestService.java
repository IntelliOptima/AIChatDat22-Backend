package com.example.aichatprojectdat.user.service;

import com.example.aichatprojectdat.user.model.PendingRelationRequestDTO;
import com.example.aichatprojectdat.user.model.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IPendingRelationRequestService {

    Flux<User> getPendingRequestsFromUserId(Long receiverId);

    Flux<User> getOutgoingRequestsFromUserId(Long requesterId);

    Mono<User> createPendingRequest(PendingRelationRequestDTO pendingRelationRequestDTO);

    Mono<User> deletePendingRequest(PendingRelationRequestDTO pendingRelationRequestDTO);
}
