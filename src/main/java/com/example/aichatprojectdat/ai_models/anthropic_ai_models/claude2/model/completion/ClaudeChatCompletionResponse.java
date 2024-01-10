package com.example.aichatprojectdat.ai_models.anthropic_ai_models.claude2.model.completion;

public record ClaudeChatCompletionResponse(
        String completion,
        String stopReason,
        String model
) {}
