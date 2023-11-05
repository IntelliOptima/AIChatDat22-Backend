package com.example.aichatprojectdat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Slf4j
@Controller
public class RSocketController {

    private final Sinks.Many<Message> chatMessagesSink;
    private final Flux<Message> chatMessages;

    public RSocketController() {
        this.chatMessagesSink = Sinks.many().multicast().onBackpressureBuffer();
        this.chatMessages = chatMessagesSink.asFlux();
    }

    @MessageMapping("fire-and-forget")
    public void fireAndForget(String name) {
        log.info("Received: {}", name);
    }

    @MessageMapping("request-response")
    public String requestResponse(String name) {
        String greet = "Welcome";
        log.info("Received: {} And Returning {}", name, greet + " ", name);
        return greet + " " + name;
    }

    @MessageMapping("chat.messages")
    public Flux<Message> chatMessages() {
        return chatMessages;
    }

    public void sendMessage(Message message) {
        chatMessagesSink.tryEmitNext(message);
    }
}
