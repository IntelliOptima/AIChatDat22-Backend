package com.example.aichatprojectdat.ai_models.OpenAIModels.dall_e.model;

import com.example.aichatprojectdat.ai_models.OpenAIModels.dall_e.model.generation.ImageGenerationRequest;

import java.io.File;

public class ImageRequestBuilder {

    private final ImageGenerationRequest request = new ImageGenerationRequest();

    public static ImageRequestBuilder of(String userPrompt) {
        return (new ImageRequestBuilder()).prompt(userPrompt);
    }

    public static ImageRequestBuilder of(File image, String userPrompt) {
        return (new ImageRequestBuilder()).image(image).prompt(userPrompt);
    }

    private ImageRequestBuilder image(File image) {
        this.request.setImage(image);
        return this;
    }

    public ImageRequestBuilder prompt(String userPrompt) {
        this.request.setPrompt(userPrompt);
        return this;
    }

    public ImageRequestBuilder model(String dallEModel) {
        this.request.setModel(dallEModel);
        return this;
    }

    public ImageRequestBuilder n(int numberOfImages) {
        this.request.setN(numberOfImages);
        return this;
    }

    public ImageRequestBuilder quality(String imageQuality) {
        this.request.setQuality(imageQuality);
        return this;
    }

    public ImageRequestBuilder responseFormat(String imageFormat) {
        this.request.setResponseFormat(imageFormat);
        return this;
    }

    public ImageRequestBuilder size(String imageSize) {
        this.request.setSize(imageSize);
        return this;
    }

    public ImageRequestBuilder style(String imageStyle) {
        this.request.setStyle(imageStyle);
        return this;
    }

    public ImageGenerationRequest build() {
        return this.request;
    }
}
