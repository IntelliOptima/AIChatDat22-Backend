package com.example.aichatprojectdat.ai_models.stability_ai_models.stablediffusion.service.interfaces;


import com.example.aichatprojectdat.ai_models.stability_ai_models.stablediffusion.models.text_to_image.JSONStructureRequest.StableDiffusionTextToImageRequest;
import com.example.aichatprojectdat.ai_models.stability_ai_models.stablediffusion.models.text_to_image.StableDiffusionTextToImageEngineList;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IStableDiffusionService {

    Flux<String> generateTextToImages(String engineModel, StableDiffusionTextToImageRequest request);
    Mono<List<StableDiffusionTextToImageEngineList>> getList();
}
