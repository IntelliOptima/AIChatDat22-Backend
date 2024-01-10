package com.example.aichatprojectdat.ai_models.google_ai_models.gemini.model.completion.chat.JSONStructureRequest;

import java.util.List;

public record Content(
        String role,
        List<Part> parts
) {}
