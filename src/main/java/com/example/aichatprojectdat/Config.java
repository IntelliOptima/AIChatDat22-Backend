package com.example.aichatprojectdat;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;

@Configuration
class RSocketConfig {

    @Bean
    RSocketMessageHandler messageHandler() {
        return new RSocketMessageHandler();
    }
}