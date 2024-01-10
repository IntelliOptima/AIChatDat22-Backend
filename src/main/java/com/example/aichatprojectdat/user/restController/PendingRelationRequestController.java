package com.example.aichatprojectdat.user.restController;

import com.example.aichatprojectdat.user.model.PendingRelationRequest;
import com.example.aichatprojectdat.user.model.PendingRelationRequestDTO;
import com.example.aichatprojectdat.user.model.User;
import com.example.aichatprojectdat.user.model.UserRelationRequestDTO;
import com.example.aichatprojectdat.user.service.PendingRelationRequestServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/pending-relation-request")
@CrossOrigin
public class PendingRelationRequestController {

    private final PendingRelationRequestServiceImpl pendingRelationRequestService;

    @GetMapping("/{receiverId}")
    public Flux<User> getPendingRequestsFromUserId(@PathVariable Long receiverId){
        return pendingRelationRequestService.getPendingRequestsFromUserId(receiverId);
    }

    @GetMapping("/outgoing/{requesterId}")
    public Flux<User> getOutgoingRequestsFromUserId(@PathVariable Long requesterId){
        return pendingRelationRequestService.getOutgoingRequestsFromUserId(requesterId);
    }

    @PostMapping
    Mono<User> createPendingRequest (@RequestBody PendingRelationRequestDTO pendingRelationRequestDTO) {
        return pendingRelationRequestService.createPendingRequest(pendingRelationRequestDTO);
    }

    @DeleteMapping("/delete")
    Mono<User> deletePendingRequest (@RequestBody PendingRelationRequestDTO pendingRelationRequestDTO) {
        return pendingRelationRequestService.deletePendingRequest(pendingRelationRequestDTO);
    }
}
