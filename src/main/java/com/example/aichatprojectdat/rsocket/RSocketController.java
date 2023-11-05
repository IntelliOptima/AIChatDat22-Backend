package com.example.aichatprojectdat.rsocket;

import com.example.aichatprojectdat.message.model.Message;
import com.example.aichatprojectdat.message.service.IMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

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

    //THIS COMES FROM CHATTEN -> show me a controller
    // for receiving a message to a Rsocket which returns message to all its subscribers?

    /*private final Sinks.Many<String> messageSink;

    public RSocketMessageController() {
        this.messageSink = Sinks.many().multicast().onBackpressureBuffer();
    }

    @MessageMapping("send-message")
    public Mono<Void> sendMessage(String message) {
        messageSink.tryEmitNext(message); // Emit the message to all subscribers
        return Mono.empty();
    }

    // This method handles the subscription from clients
    @MessageMapping("messages")
    public Flux<String> subscribeToMessages() {
        return messageSink.asFlux();
    }
*/

}
