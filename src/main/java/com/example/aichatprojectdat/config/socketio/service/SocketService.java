package com.example.aichatprojectdat.config.socketio.service;


import com.corundumstudio.socketio.SocketIOClient;
import com.example.aichatprojectdat.message.model.Message;
import com.example.aichatprojectdat.message.service.IMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SocketService {

    private final IMessageService messageService;

//
//    public void sendSocketMessage(SocketIOClient senderClient, Message message, String room) {
//        for (
//                SocketIOClient client : senderClient.getNamespace().getRoomOperations(room).getClients()) {
//            if (!client.getSessionId().equals(senderClient.getSessionId())) {
//                client.sendEvent("read_message",
//                        message);
//            }
//        }
//    }
//
//    public void saveMessage(SocketIOClient senderClient, Message message) {
//        Message storedMessage = messageService.create(message).block();
//        sendSocketMessage(senderClient, storedMessage, message.getChatroomId());
//    }
}
