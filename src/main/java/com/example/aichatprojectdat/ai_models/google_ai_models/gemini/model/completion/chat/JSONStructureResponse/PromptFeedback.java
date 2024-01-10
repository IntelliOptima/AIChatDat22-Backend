package com.example.aichatprojectdat.ai_models.google_ai_models.gemini.model.completion.chat.JSONStructureResponse;

import java.util.List;

public record PromptFeedback(
        List<SafetyRating> safetyRatings
) {
}
