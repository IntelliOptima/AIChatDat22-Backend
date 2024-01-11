package com.example.aichatprojectdat.ai_models.stability_ai_models.stablediffusion.spring.service;

import com.example.aichatprojectdat.ai_models.stability_ai_models.stablediffusion.models.EngineListInterface;
import com.example.aichatprojectdat.ai_models.stability_ai_models.stablediffusion.models.StableDiffusionInterface;
import com.example.aichatprojectdat.ai_models.stability_ai_models.stablediffusion.models.text_to_image.JSONStructureResponse.Artifact;
import com.example.aichatprojectdat.ai_models.stability_ai_models.stablediffusion.models.text_to_image.JSONStructureRequest.StableDiffusionTextToImageRequest;
import com.example.aichatprojectdat.ai_models.stability_ai_models.stablediffusion.models.text_to_image.JSONStructureResponse.StableDiffusionTextToImageResponse;
import com.example.aichatprojectdat.ai_models.stability_ai_models.stablediffusion.models.text_to_image.StableDiffusionTextToImageEngineList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class StableDiffusionTextToImageService {

    private final StableDiffusionInterface stableDiffusionInterface;
    private final EngineListInterface engineListInterface;

    public Flux<String> getArtifacts(String engine, StableDiffusionTextToImageRequest request) {
        return stableDiffusionInterface.getImages(engine, request)
                .flatMapIterable(StableDiffusionTextToImageResponse::artifacts) // Extract the list of lists of artifacts
                .flatMap(Flux::fromIterable) // Flatten the lists of artifacts into a Flux<Artifact>
                .map(Artifact::base64); // Extract the base64 string from each artifact
    }

    public Mono<List<StableDiffusionTextToImageEngineList>> getList() {
        return engineListInterface.getList();
    }

}
