package com.example.aichatprojectdat.ai_models.anthropic_ai_models.claude2.model;

import com.example.aichatprojectdat.ai_models.anthropic_ai_models.claude2.model.completion.ClaudeChatCompletionRequest;
import com.example.aichatprojectdat.ai_models.anthropic_ai_models.claude2.model.completion.ClaudeChatCompletionResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/v1/complete")
public interface ClaudeInterface {
    @PostExchange
    ClaudeChatCompletionResponse getCompletion(@RequestBody ClaudeChatCompletionRequest request);
}
