package com.example.aichatprojectdat.ai_models.google_ai_models.gemini.model.completion.chat.JSONStructureResponse;

import java.util.List;

public record Candidate(
        Content content,
        String finishReason,
        int index,
        List<SafetyRating> safetyRatingList
) {}
