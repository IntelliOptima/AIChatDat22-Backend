package com.example.aichatprojectdat.chatroom.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
public class RSocketController {

    @MessageMapping("fire-and-forget")
    public void fireAndForget(String name){
        log.info("Received: {}",name);
    }

    @MessageMapping("request-response")
    public String requestResponse(String name){
        String greet = "Welcome";
        log.info("Received: {} And Returning {}", name, greet + " ", name);
        return greet + " " + name;
    }
}
