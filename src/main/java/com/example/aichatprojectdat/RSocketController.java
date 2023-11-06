package com.example.aichatprojectdat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;

import static java.net.Authenticator.RequestorType.SERVER;

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

    @MessageMapping("chanel/{channelId}")
    public Flux<Message> channel(@DestinationVariable String channelId, Message message) {


        return Flux.just(message);
    }


    @MessageMapping("request-response")
    public Mono<String> requestResponse(Mono<String> name) {
        return name.map(inputName -> "Welcome " + inputName)
                .doOnNext(greet -> log.info("Received: {} And Returning {}", name, greet));
    }

    @MessageMapping("chat.messages")
    public Flux<Message> chatMessages() {
        return chatMessages;
    }

    public void sendMessage(Message message) {
        chatMessagesSink.tryEmitNext(message);
    }
}
