package com.example.aichatprojectdat.ai_models.google_ai_models.gemini.model.completion.chat;

import com.example.aichatprojectdat.ai_models.google_ai_models.gemini.model.completion.chat.JSONStructureResponse.Candidate;
import com.example.aichatprojectdat.ai_models.google_ai_models.gemini.model.completion.chat.JSONStructureResponse.PromptFeedback;

import java.util.List;

public record GeminiChatCompletionResponse(
        List<Candidate> candidates,
        PromptFeedback promptFeedback
) {}
