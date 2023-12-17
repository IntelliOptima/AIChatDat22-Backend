package com.example.aichatprojectdat.user.restController;

import com.example.aichatprojectdat.user.model.User;
import com.example.aichatprojectdat.user.model.UserRelation;
import com.example.aichatprojectdat.user.model.UserRelationRequest;
import com.example.aichatprojectdat.user.service.UserRelationServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/user-relation")
@CrossOrigin
public class UserRelationController {

    private final UserRelationServiceImpl userRelationService;

    @GetMapping("/{userId}")
    Flux<User> getAllUserRelationFromUserId (@PathVariable Long userId){
        return userRelationService.getAllUserFriendsFromUserId(userId);
    }

    @PostMapping
    Mono<User> createUserRelation (@RequestBody UserRelationRequest userRelationRequest) {
        return userRelationService.createUserRelation(userRelationRequest);
    }
}
