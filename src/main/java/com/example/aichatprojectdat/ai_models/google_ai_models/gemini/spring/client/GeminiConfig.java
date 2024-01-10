package com.example.aichatprojectdat.ai_models.google_ai_models.gemini.spring.client;

import com.example.aichatprojectdat.ai_models.google_ai_models.gemini.model.GeminiInterface;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class GeminiConfig {

    // Bean configuration
    /*@Bean
    public WebClient geminiRestClient(@Value("${gemini.baseurl}") String baseUrl,
                                      @Value("${googleai.api.key}") String apiKey) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("x-goog-api-key", apiKey)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .build();
    }*/

    @Bean
    public WebClient geminiRestClient(@Value("${gemini.baseurl}") String baseUrl) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .build();
    }

    @Bean
    public GeminiInterface geminiInterface(@Qualifier("geminiRestClient") WebClient client) {
        WebClientAdapter adapter = WebClientAdapter.forClient(client);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder(adapter).build();
        return factory.createClient(GeminiInterface.class);
    }
}
