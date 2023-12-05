package com.example.aichatprojectdat.config;

import com.example.aichatprojectdat.message.model.MessageDecoder;
import com.example.aichatprojectdat.message.model.MessageEncoder;
import com.example.aichatprojectdat.user.model.UserDecoder;
import com.example.aichatprojectdat.user.model.UserEncoder;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.rsocket.frame.decoder.PayloadDecoder;
import org.springframework.boot.rsocket.server.RSocketServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                // Register any necessary modules
                .registerModule(new JavaTimeModule())
                // Configure as necessary
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Bean
    public RSocketStrategies rSocketStrategies(ObjectMapper objectMapper) {
        return RSocketStrategies.builder()
                .metadataExtractorRegistry(registry -> {
                    registry.metadataToExtract(MimeTypeUtils.APPLICATION_OCTET_STREAM, Map.class, "headers");
                })
                .decoder(new Jackson2JsonDecoder())
                .encoder(new Jackson2JsonEncoder())
                // Add custom decoder here for APPLICATION_OCTET_STREAM
                .decoder(new MessageDecoder(objectMapper))
                .encoder(new MessageEncoder(objectMapper))
                .decoder(new UserDecoder(objectMapper))
                .encoder(new UserEncoder(objectMapper))
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

//    @Bean
//    RSocketMessageHandler messageHandler() {
//        return new RSocketMessageHandler();
//    }
}
