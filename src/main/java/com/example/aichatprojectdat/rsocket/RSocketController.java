package com.example.aichatprojectdat.rsocket;

import com.example.aichatprojectdat.message.model.Message;
import com.example.aichatprojectdat.message.service.IMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class RSocketController {

    private final IMessageService messageService;

    @MessageMapping("send.message")
    public Mono<Void> handleMessage(Message message) {
        return messageService.create(message).then();
    }


    @MessageMapping("stream.messages")
    public Flux<Message> streamMessages(Long chatroomId) {
        return messageService.findMessagesByChatroomId(chatroomId);
    }

}
