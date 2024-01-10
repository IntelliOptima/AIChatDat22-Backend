package com.example.aichatprojectdat.ai_models.anthropic_ai_models.claude2.model.completion;

public record ClaudeChatCompletionRequest(
   String model,
   String prompt,
   int maxTokensToSample,
   double temperature
) {}
