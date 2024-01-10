package com.example.aichatprojectdat.ai_models.google_ai_models.gemini.model.completion.chat;

import com.example.aichatprojectdat.ai_models.google_ai_models.gemini.model.completion.chat.JSONStructureRequest.Content;

import java.util.List;

public record GeminiChatCompletionRequest(
        List<Content> contents
) {}
