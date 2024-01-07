package com.example.aichatprojectdat.ai_models.google_ai_models.gemini.spring.service;

import com.example.aichatprojectdat.ai_models.google_ai_models.gemini.model.completion.chat.GeminiChatCompletionResponse;
import com.example.aichatprojectdat.ai_models.google_ai_models.gemini.model.completion.chat.JSONStructureRequest.Content;
import com.example.aichatprojectdat.ai_models.google_ai_models.gemini.model.completion.chat.JSONStructureRequest.InlineData;
import com.example.aichatprojectdat.ai_models.google_ai_models.gemini.model.completion.chat.JSONStructureRequest.InlineDataPart;
import com.example.aichatprojectdat.ai_models.google_ai_models.gemini.model.completion.chat.JSONStructureRequest.TextPart;
import com.example.aichatprojectdat.ai_models.google_ai_models.gemini.model.GeminiInterface;
import com.example.aichatprojectdat.ai_models.google_ai_models.gemini.model.completion.chat.GeminiChatCompletionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GeminiService {
    public static final String GEMINI_PRO = "gemini_pro";
    public static final String GEMINI_PRO_VISION = "gemini-pro-vision";

    private final GeminiInterface geminiInterface;

    public Flux<GeminiChatCompletionResponse> getCompletion(GeminiChatCompletionRequest request) {
        return geminiInterface.getCompletion(GEMINI_PRO, request);
    }

    public Flux<GeminiChatCompletionResponse> getCompletionWithImage(GeminiChatCompletionRequest request) {
        return geminiInterface.getCompletion(GEMINI_PRO_VISION, request);
    }

    public Flux<String> getCompletion(List<Content> context) {
        // Create a mutable list from the context
        List<Content> combinedContents = new ArrayList<>(context);

        // Create and send the request reactively
        return geminiInterface.getCompletion(GEMINI_PRO, new GeminiChatCompletionRequest(combinedContents))
                .flatMapIterable(GeminiChatCompletionResponse::candidates) // Flatten if expecting multiple candidates
                .map(candidate -> candidate.content().parts().get(0).text()); // Assuming you want the first part's text of each candidate
    }




}
