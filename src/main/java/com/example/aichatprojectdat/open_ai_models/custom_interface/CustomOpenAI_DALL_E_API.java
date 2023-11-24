package com.example.aichatprojectdat.open_ai_models.custom_interface;

import com.example.aichatprojectdat.open_ai_models.dall_e.model.generation.ImageGenerationRequest;
import com.example.aichatprojectdat.open_ai_models.dall_e.model.generation.ImageGenerationResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;


public interface CustomOpenAI_DALL_E_API {

    @PostExchange("/images/generations")
    Mono<ImageGenerationResponse> imageGenerate(@RequestBody ImageGenerationRequest request);

    @PostExchange("/images/edit")
    Mono<ImageGenerationResponse> editImageGeneration(@RequestBody ImageGenerationRequest request);

    @PostExchange("/images/variations")
    Mono<ImageGenerationResponse> imageVariationGenerate(@RequestBody ImageGenerationRequest request);
}

