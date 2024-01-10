package com.example.aichatprojectdat.user.service;

import com.example.aichatprojectdat.user.model.PendingRelationRequest;
import com.example.aichatprojectdat.user.model.PendingRelationRequestDTO;
import com.example.aichatprojectdat.user.model.User;
import com.example.aichatprojectdat.user.repository.PendingRelationRequestRepository;
import com.example.aichatprojectdat.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Primary
@RequiredArgsConstructor
public class PendingRelationRequestServiceImpl implements IPendingRelationRequestService{

    private final PendingRelationRequestRepository pendingRelationRequestRepository;
    private final UserRepository userRepository;

    @Override
    public Flux<User> getPendingRequestsFromUserId(Long receiverId) {
        return pendingRelationRequestRepository.findAllByReceiverId(receiverId)
                .flatMap(pendingRelation -> userRepository.findById(pendingRelation.getRequesterId()));
    }

    @Override
    public Flux<User> getOutgoingRequestsFromUserId(Long requesterId) {
        return pendingRelationRequestRepository.findAllByRequesterId(requesterId)
                .flatMap(pendingRelation -> userRepository.findById(pendingRelation.getReceiverId()));
    }

    @Override
    public Mono<User> createPendingRequest(PendingRelationRequestDTO pendingRelationRequestDTO) {
        return userRepository.findById(pendingRelationRequestDTO.getReceiverId())
                .flatMap(receiver -> {
                    if (!receiver.id().equals(pendingRelationRequestDTO.getRequesterId())) {
                        PendingRelationRequest newPendingRelationRequest = new PendingRelationRequest();
                        newPendingRelationRequest.setRequesterId(pendingRelationRequestDTO.getRequesterId());
                        newPendingRelationRequest.setReceiverId(pendingRelationRequestDTO.getReceiverId());

                        return pendingRelationRequestRepository.save(newPendingRelationRequest)
                                .thenReturn(receiver);
                    } else {
                        return Mono.error(new RuntimeException("Cannot send a request to oneself"));
                    }
                });
    }

    @Override
    public Mono<User> deletePendingRequest(PendingRelationRequestDTO pendingRelationRequestDTO) {
        return userRepository.findById(pendingRelationRequestDTO.getReceiverId())
                .flatMap(receiver -> {
                    if (!receiver.id().equals(pendingRelationRequestDTO.getRequesterId())) {
                        return pendingRelationRequestRepository.deleteByRequesterIdAndReceiverId(pendingRelationRequestDTO.getRequesterId(), receiver.id())
                                .thenReturn(receiver);
                    } else {
                        return Mono.error(new RuntimeException("Cannot send a request to oneself"));
                    }
                });
    }



}
