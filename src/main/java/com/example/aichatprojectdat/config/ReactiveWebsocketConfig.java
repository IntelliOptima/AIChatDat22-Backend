package com.example.aichatprojectdat.config;

import com.example.aichatprojectdat.chatroom.controller.ReactiveWebsocketHandler;
import io.netty.handler.codec.http.HttpMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsWebFilter;
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

    @Bean
    public CorsWebFilter corsWebFilter() {
        return new CorsWebFilter(corsConfigurationSource());
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration defaultCorsConfig = new CorsConfiguration();
        defaultCorsConfig.addAllowedOrigin("http://localhost:3000");
        defaultCorsConfig.addAllowedOrigin("https://ai-team-talk.vercel.app");
        defaultCorsConfig.addAllowedMethod("*");
        defaultCorsConfig.addAllowedHeader("*");
        defaultCorsConfig.setAllowCredentials(true);

        Map<String, CorsConfiguration> corsConfigurationMap = new HashMap<>();
        corsConfigurationMap.put("/subscribe/**", defaultCorsConfig);
        // Add more specific configurations if needed

        return exchange -> {
            String path = exchange.getRequest().getURI().getPath();
            // Find the first matching path in the map
            for (String key : corsConfigurationMap.keySet()) {
                if (path.matches(key.replace("/**", "(/|$)(.*)"))) {
                    return corsConfigurationMap.get(key);
                }
            }
            // Fallback to default configuration if no specific match is found
            return defaultCorsConfig;
        };
    }
}
