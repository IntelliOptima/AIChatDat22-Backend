package com.example.aichatprojectdat.ai_models.google_ai_models.gemini.model;

import com.example.aichatprojectdat.ai_models.google_ai_models.gemini.model.completion.chat.GeminiChatCompletionResponse;
import com.example.aichatprojectdat.ai_models.google_ai_models.gemini.model.completion.chat.GeminiChatCompletionRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@HttpExchange("/v1beta/models/")
public interface GeminiInterface {

    @PostExchange("{model}:streamGenerateContent")
    Flux<GeminiChatCompletionResponse> getCompletion(
            @PathVariable String model,
            @RequestBody GeminiChatCompletionRequest request
    );
}
