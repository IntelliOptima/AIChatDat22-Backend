package com.example.aichatprojectdat.ai_models.stability_ai_models.stablediffusion.service;

import com.example.aichatprojectdat.ai_models.stability_ai_models.stablediffusion.models.text_to_image.JSONStructureRequest.StableDiffusionTextToImageRequest;
import com.example.aichatprojectdat.ai_models.stability_ai_models.stablediffusion.service.interfaces.IStableDiffusionService;
import com.example.aichatprojectdat.ai_models.stability_ai_models.stablediffusion.spring.service.StableDiffusionTextToImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@Slf4j@RequiredArgsConstructor

public class StableDiffusionServiceImpl implements IStableDiffusionService {

    private final StableDiffusionTextToImageService stableDiffusionService;

    @Override
    public Flux<String> generateTextToImages(String engineModel, StableDiffusionTextToImageRequest request) {
        return stableDiffusionService.getArtifacts(engineModel, request);
    }
}
