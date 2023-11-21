package com.example.aichatprojectdat.OpenAIModels.dall_e.service;

import com.example.aichatprojectdat.OpenAIModels.custom_interface.DALLE_Exchange;

import com.example.aichatprojectdat.OpenAIModels.dall_e.model.generation.ImageGenerationResponse;
import reactor.core.publisher.Mono;

import java.io.File;

@DALLE_Exchange
public interface IDALL_E3ServiceStandard {
    Mono<ImageGenerationResponse> generateImage(String request);
    Mono<ImageGenerationResponse> editGenerateImage(String request, File image);
    Mono<ImageGenerationResponse> variationGenerateImage(String request, File image);
}
