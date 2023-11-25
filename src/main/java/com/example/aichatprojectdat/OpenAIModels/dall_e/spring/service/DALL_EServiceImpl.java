package com.example.aichatprojectdat.OpenAIModels.dall_e.spring.service;

import com.example.aichatprojectdat.OpenAIModels.custom_interface.CustomOpenAI_DALL_E_API;
import com.example.aichatprojectdat.OpenAIModels.dall_e.model.generation.ImageGenerationRequest;
import com.example.aichatprojectdat.OpenAIModels.dall_e.model.generation.ImageGenerationResponse;
import com.example.aichatprojectdat.OpenAIModels.dall_e.spring.service.interfaces.IDALL_EService;


import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Primary
public class DALL_EServiceImpl implements IDALL_EService {

    private final CustomOpenAI_DALL_E_API openAIChatAPI;
    private String model = "dall-e-3";

    DALL_EServiceImpl(CustomOpenAI_DALL_E_API openAIChatAPI) {
        this.openAIChatAPI = openAIChatAPI;
    }


    @Override
    public Mono<ImageGenerationResponse> generateImage(ImageGenerationRequest request) {
        if (request.getModel() == null) {
            request.setModel(this.model);
        }
        return openAIChatAPI.imageGenerate(request);
    }

    @Override
    public Mono<ImageGenerationResponse> editGenerateImage(ImageGenerationRequest request) {
        return null;
    }

    @Override
    public Mono<ImageGenerationResponse> variationGenerateImage(ImageGenerationRequest request) {
        return null;
    }

}
