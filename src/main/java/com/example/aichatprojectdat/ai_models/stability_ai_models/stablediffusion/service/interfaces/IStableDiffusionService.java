package com.example.aichatprojectdat.ai_models.stability_ai_models.stablediffusion.service.interfaces;


import com.example.aichatprojectdat.ai_models.stability_ai_models.stablediffusion.models.text_to_image.JSONStructureRequest.StableDiffusionTextToImageRequest;
import reactor.core.publisher.Flux;

public interface IStableDiffusionService {

    Flux<String> generateTextToImages(String engineModel, StableDiffusionTextToImageRequest request);
}
