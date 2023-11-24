package com.example.aichatprojectdat.open_ai_models.dall_e.spring.service.interfaces;


import com.example.aichatprojectdat.open_ai_models.dall_e.model.generation.ImageGenerationRequest;
import com.example.aichatprojectdat.open_ai_models.dall_e.model.generation.ImageGenerationResponse;

import reactor.core.publisher.Mono;

public interface IDALL_EService {

    Mono<ImageGenerationResponse> generateImage(ImageGenerationRequest question);
    Mono<ImageGenerationResponse> editGenerateImage(ImageGenerationRequest question);
    Mono<ImageGenerationResponse> variationGenerateImage(ImageGenerationRequest question);

}