package com.example.aichatprojectdat.ai_models.OpenAIModels.dall_e.spring.service;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "openai")
public record DALL_EProperties(Api api) {
    public record Api(String key, String url) {
    }
}
