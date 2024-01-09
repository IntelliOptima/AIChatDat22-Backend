package com.example.aichatprojectdat.ai_models.stability_ai_models.stablediffusion.models.text_to_image;


import com.example.aichatprojectdat.ai_models.stability_ai_models.stablediffusion.models.text_to_image.JSONStructureRequest.StableDiffusionTextToImageRequest;
import com.example.aichatprojectdat.ai_models.stability_ai_models.stablediffusion.models.text_to_image.JSONStructureRequest.TextPrompt;


public class StableDiffusionTextToImageGenerationRequestBuilder {

    private final StableDiffusionTextToImageRequest request = new StableDiffusionTextToImageRequest();

    public static StableDiffusionTextToImageGenerationRequestBuilder of(String userPrompt, float weight) {
        return (new StableDiffusionTextToImageGenerationRequestBuilder()).prompt(TextPrompt.of(userPrompt, weight));
    }

    private StableDiffusionTextToImageGenerationRequestBuilder height(String height) {
        this.request.setHeight(height);
        return this;
    }

    private StableDiffusionTextToImageGenerationRequestBuilder width (String width) {
        this.request.setWidth(width);
        return this;
    }

    private StableDiffusionTextToImageGenerationRequestBuilder cfgScale(int n) {
        this.request.setCfgScale(n);
        return this;
    }

    private StableDiffusionTextToImageGenerationRequestBuilder clipGuidancePreset(String clipGuidancePresetValue) {
        this.request.setClipGuidancePreset(clipGuidancePresetValue);
        return this;
    }

    public StableDiffusionTextToImageGenerationRequestBuilder sampler(String samplerValue) {
        this.request.setSampler(samplerValue);
        return this;
    }

    public StableDiffusionTextToImageGenerationRequestBuilder seed(float seedValue) {
        this.request.setSeed(seedValue);
        return this;
    }

    public StableDiffusionTextToImageGenerationRequestBuilder stylePreset(String stylePreset) {
        this.request.setStylePreset(stylePreset);
        return this;
    }

    public StableDiffusionTextToImageGenerationRequestBuilder prompt(TextPrompt textPrompt) {
        this.request.addTextPrompt(textPrompt);
        return this;
    }

    public StableDiffusionTextToImageRequest build() {
        return this.request;
    }

}
