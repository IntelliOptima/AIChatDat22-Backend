package com.example.aichatprojectdat.chatroom.controller;

import com.example.aichatprojectdat.message.model.Message;
import com.example.aichatprojectdat.chatroom.model.OutputMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
public class ChatroomWebsocketController {

    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    public OutputMessage send(Message message) {
        System.out.println(message.toString());
        String time = new SimpleDateFormat("HH:mm").format(new Date());
        return new OutputMessage((String.valueOf(message.userId())), message.message(), time);
    }
}
