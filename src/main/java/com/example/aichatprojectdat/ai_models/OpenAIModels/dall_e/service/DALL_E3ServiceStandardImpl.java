package com.example.aichatprojectdat.ai_models.OpenAIModels.dall_e.service;

import com.example.aichatprojectdat.ai_models.OpenAIModels.dall_e.model.ImageRequestBuilder;
import com.example.aichatprojectdat.ai_models.OpenAIModels.dall_e.model.generation.ImageGenerationRequest;
import com.example.aichatprojectdat.ai_models.OpenAIModels.dall_e.model.generation.ImageGenerationResponse;
import com.example.aichatprojectdat.ai_models.OpenAIModels.dall_e.spring.client.DALLEExchangeMethodInterceptor;
import com.example.aichatprojectdat.ai_models.OpenAIModels.dall_e.spring.service.interfaces.IDALL_EService;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;

@Service
@Primary
public class DALL_E3ServiceStandardImpl implements IDALL_E3ServiceStandard {

    private final IDALL_EService dallEService;

    @Autowired
    DALL_E3ServiceStandardImpl(IDALL_EService dallEService) {
        this.dallEService = dallEService;
    }

    @Override
    public Flux<ImageGenerationResponse> generateImage(String request) {
        return dallEService.generateImage(ImageRequestBuilder.of(request)
                        .n(1)
                        .size("1024x1024")
                        .style("vivid")
                        .quality("hd")
                        .responseFormat("url")
                        .model("dall-e-3")
                .build());
    }

    @Override
    public Mono<ImageGenerationResponse> editGenerateImage(String request, File image) {
        return dallEService.editGenerateImage(ImageGenerationRequest.of(request, image));
    }

    @Override
    public Mono<ImageGenerationResponse> variationGenerateImage(String request, File image) {
        return dallEService.variationGenerateImage(ImageGenerationRequest.of(request, image));
    }

    private <T> T createProxy(Class<T> clazz) {
        return ProxyFactory.getProxy(clazz, new DALLEExchangeMethodInterceptor(this.dallEService));
    }
}
