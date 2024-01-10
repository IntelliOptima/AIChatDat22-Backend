package com.example.aichatprojectdat.ai_models.stability_ai_models.stablediffusion.spring.client;

import com.example.aichatprojectdat.ai_models.stability_ai_models.stablediffusion.models.StableDiffusionInterface;
import com.example.aichatprojectdat.ai_models.stability_ai_models.stablediffusion.models.EngineListInterface;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class StableDiffusionConfig {

    @Value("${stabilityai.api.key}")
    private String apiKey;

    @Value("${stablediffusion.baseurl}")
    private String baseUrl;


    @Bean
    public WebClient stableDiffusionRestClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "image/png")
                .build();
    }

    @Bean
    public StableDiffusionInterface stableDiffusionInterface(@Qualifier("stableDiffusionRestClient") WebClient client) {
        WebClientAdapter adapter = WebClientAdapter.forClient(client);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder(adapter).build();
        return factory.createClient(StableDiffusionInterface.class);
    }

    @Bean
    public EngineListInterface engineListInterface(@Qualifier("stableDiffusionRestClient") WebClient client) {
        WebClientAdapter adapter = WebClientAdapter.forClient(client);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder(adapter).build();
        return factory.createClient(EngineListInterface.class);
    }
}
