package com.example.aichatprojectdat.ai_models.google_ai_models.gemini.model.completion.chat.JSONStructureResponse;

import com.example.aichatprojectdat.ai_models.google_ai_models.gemini.model.completion.chat.JSONStructureRequest.TextPart;

import java.util.List;

public record Content(
        List<TextPart> parts,
        String role
) {}
