package com.example.aichatprojectdat.ai_models.stability_ai_models.stablediffusion.models;

import com.example.aichatprojectdat.ai_models.stability_ai_models.stablediffusion.models.text_to_image.JSONStructureRequest.StableDiffusionTextToImageRequest;
import com.example.aichatprojectdat.ai_models.stability_ai_models.stablediffusion.models.text_to_image.JSONStructureResponse.StableDiffusionTextToImageResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

@HttpExchange("/v1/generation/")
public interface StableDiffusionInterface {

    @PostExchange("{engineId}/text-to-image")
    Mono<StableDiffusionTextToImageResponse> getImages(
            @PathVariable String engineId,
            @RequestBody StableDiffusionTextToImageRequest request
    );
}
