package com.example.aichatprojectdat.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UtilityConfig {

    @Bean("jsonObjectMapperUtil")
    public ObjectMapper jsonObjectMapper() {
        return new ObjectMapper();
    }
}
