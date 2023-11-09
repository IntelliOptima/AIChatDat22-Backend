package com.example.aichatprojectdat.user.restController;


import com.example.aichatprojectdat.message.model.Message;
import com.example.aichatprojectdat.user.model.User;
import com.example.aichatprojectdat.user.repository.UserRepository;
import com.example.aichatprojectdat.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/user")
@CrossOrigin
public class UserRestController {

    private final IUserService userService;

    @PostMapping
    public Mono<User> getUser(@RequestBody User user) {
        return userService.createOrReturnExistingUser(user);
    }
}
