package com.example.aichatprojectdat.config.socketio.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SocketModule {
//
//    private final SocketIOServer server;
//    private final SocketService socketService;
//
//    public SocketModule(SocketIOServer server, SocketService socketService) {
//        this.server = server;
//        this.socketService = socketService;
//        server.addEventListener("send_message", ChunkData.class, onChatReceived());
//
//    }
//
//
//    private DataListener<ChunkData> onChatReceived() {
//        return (senderClient, data, ackSender) -> {
//            log.info(data.toString());
//            socketService.saveMessage(senderClient, data.getChunk());
//        };
//    }
}
