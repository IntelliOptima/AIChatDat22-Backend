package com.example.aichatprojectdat.ai_models.stability_ai_models.stablediffusion.models.text_to_image.JSONStructureRequest;

public record TextPrompt(
        String text,
        float weight
) {
    public static TextPrompt of(String text, float weight) {
        return new TextPrompt(text, weight);
    }
}
