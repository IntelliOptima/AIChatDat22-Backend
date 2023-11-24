package com.example.aichatprojectdat.open_ai_models.dall_e.spring.service;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "openai")
public record DALL_EProperties(Api api) {
    public record Api(String key, String url) {
    }
}
