package com.example.aichatprojectdat.config;

import io.rsocket.frame.decoder.PayloadDecoder;
import org.springframework.boot.rsocket.server.RSocketServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.cbor.Jackson2CborDecoder;
import org.springframework.http.codec.cbor.Jackson2CborEncoder;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.util.pattern.PathPatternRouteMatcher;

import java.util.Map;

@Configuration
public class RSocketConfig {

    @Bean
    public RSocketStrategies rSocketStrategies() {
        return RSocketStrategies.builder()
                .metadataExtractorRegistry(registry -> {
                    registry.metadataToExtract(MimeTypeUtils.APPLICATION_JSON, Map.class, "headers");
                })
                .decoder(new Jackson2JsonDecoder())
                .encoder(new Jackson2JsonEncoder())
                .build();
    }


    @Bean
    public RSocketMessageHandler messageHandler(RSocketStrategies socketStrategies) {
        RSocketMessageHandler handler = new RSocketMessageHandler();
        handler.setRSocketStrategies(socketStrategies);
        handler.setRouteMatcher(new PathPatternRouteMatcher());
        return handler;
    }

    @Bean
    public RSocketServerCustomizer rSocketServerCustomizer() {
        return rSocketServer -> rSocketServer.payloadDecoder(PayloadDecoder.ZERO_COPY);
    }
}