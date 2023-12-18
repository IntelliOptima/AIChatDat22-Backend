package com.example.aichatprojectdat.user.repository;

import com.example.aichatprojectdat.user.model.PendingRelationRequest;
import com.example.aichatprojectdat.user.model.UserRelation;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PendingRelationRequestRepository extends R2dbcRepository<PendingRelationRequest, Long> {

    Flux<PendingRelationRequest> findAllByReceiverId(Long receiverId);

    Flux<PendingRelationRequest> findAllByRequesterId(Long requesterId);

    Mono<Void> deleteByRequesterIdAndReceiverId(Long requesterId, Long receiverId);


}
