package com.example.aichatprojectdat.ai_models.stability_ai_models.stablediffusion.models;

import com.example.aichatprojectdat.ai_models.stability_ai_models.stablediffusion.models.text_to_image.StableDiffusionTextToImageEngineList;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import reactor.core.publisher.Mono;

@HttpExchange("/v1/engines/list")
public interface EngineListInterface {

    @GetExchange
    Mono<StableDiffusionTextToImageEngineList> getList();
}
