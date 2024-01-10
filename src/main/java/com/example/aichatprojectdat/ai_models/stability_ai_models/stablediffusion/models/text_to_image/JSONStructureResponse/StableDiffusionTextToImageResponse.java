package com.example.aichatprojectdat.ai_models.stability_ai_models.stablediffusion.models.text_to_image.JSONStructureResponse;

import com.example.aichatprojectdat.ai_models.stability_ai_models.stablediffusion.models.text_to_image.JSONStructureResponse.Artifact;

import java.util.List;

public record StableDiffusionTextToImageResponse(
        List<List<Artifact>> artifacts
) {}
