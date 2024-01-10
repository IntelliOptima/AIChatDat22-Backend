package com.example.aichatprojectdat.ai_models.stability_ai_models.stablediffusion.models.text_to_image.JSONStructureResponse;

public record Artifact(
        String base64,
        FinishReason finishReason,
        long seed
) {}
