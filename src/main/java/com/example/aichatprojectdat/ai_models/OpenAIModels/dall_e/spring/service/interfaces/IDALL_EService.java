package com.example.aichatprojectdat.ai_models.OpenAIModels.dall_e.spring.service.interfaces;


import com.example.aichatprojectdat.ai_models.OpenAIModels.dall_e.model.generation.ImageGenerationRequest;
import com.example.aichatprojectdat.ai_models.OpenAIModels.dall_e.model.generation.ImageGenerationResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IDALL_EService {

    Flux<ImageGenerationResponse> generateImage(ImageGenerationRequest question);
    Mono<ImageGenerationResponse> editGenerateImage(ImageGenerationRequest question);
    Mono<ImageGenerationResponse> variationGenerateImage(ImageGenerationRequest question);

}