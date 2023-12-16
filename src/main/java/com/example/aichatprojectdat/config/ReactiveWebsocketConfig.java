package com.example.aichatprojectdat.config;

import com.example.aichatprojectdat.chatroom.controller.ReactiveWebsocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.WebSocketService;
import org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import org.springframework.web.reactive.socket.server.upgrade.ReactorNettyRequestUpgradeStrategy;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class ReactiveWebsocketConfig {

    private final ReactiveWebsocketHandler reactiveWebSocketHandler;

    public ReactiveWebsocketConfig(@Qualifier("ReactiveWebSocketHandler") ReactiveWebsocketHandler reactiveWebSocketHandler) {
        this.reactiveWebSocketHandler = reactiveWebSocketHandler;
    }


    @Bean
    public HandlerMapping reactiveWebSocketHandlerMapping() {
        Map<String, WebSocketHandler> map = new HashMap<>();
        //map.put("/event-emitter/${chatroomId}", reactiveWebSocketHandler);
        map.put("/subscribe/{chatroomId}", reactiveWebSocketHandler);

        SimpleUrlHandlerMapping handlerMapping = new SimpleUrlHandlerMapping();
        handlerMapping.setOrder(1);
        handlerMapping.setUrlMap(map);
        return handlerMapping;
    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter(webSocketService());
    }

    @Bean
    public WebSocketService webSocketService() {
        ReactorNettyRequestUpgradeStrategy nettyRequestUpgradeStrategy = new ReactorNettyRequestUpgradeStrategy();
        //nettyRequestUpgradeStrategy. .setMaxSessionIdleTimeout(10000L);
        //nettyRequestUpgradeStrategy.setAsyncSendTimeout(10000L);
        return new HandshakeWebSocketService(nettyRequestUpgradeStrategy);
    }

    /* Not sure but guess this is only needed for servlet websocket connection - controller
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        ServerEndpointExporter serverEndpointExporter = new ServerEndpointExporter();

        *//**
         * Add one or more classes annotated with `@ServerEndpoint`.
         *//*
        serverEndpointExporter.setAnnotatedEndpointClasses(WebSocketController.class);

        return serverEndpointExporter;
    }
    */
}
