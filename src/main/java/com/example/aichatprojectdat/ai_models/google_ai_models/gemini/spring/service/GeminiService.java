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

    public GeminiChatCompletionResponse getCompletion(GeminiChatCompletionRequest request) {
        return geminiInterface.getCompletion(GEMINI_PRO, request);
    }

    public GeminiChatCompletionResponse getCompletionWithImage(GeminiChatCompletionRequest request) {
        return geminiInterface.getCompletion(GEMINI_PRO_VISION, request);
    }

    public Flux<String> getCompletion(List<Content> context) {
        // Add the user's input as the last piece of content
        List<Content> combinedContents = new ArrayList<>(context); // Create a mutable list from the context
        // Create and send the request
        GeminiChatCompletionResponse response = getCompletion(new GeminiChatCompletionRequest(combinedContents));

        // Assuming you want the first candidate's first part's text as the response
        return Flux.just(response.candidates().get(0).content().parts().get(0).text());
    }

    public Flux<String> getCompletionWithImage(List<Content> context, String text, String imageFileName) {
        return Mono.fromCallable(() -> {
                    // Read the image file and encode it
                    byte[] bytes = Files.readAllBytes(Path.of("src/main/resources/", imageFileName));
                    String encodedImage = Base64.getEncoder().encodeToString(bytes);

                    // Create a new Content object for the user's text and image
                    Content userInputContent = new Content("user", List.of(
                            new TextPart(text),
                            new InlineDataPart(new InlineData("image/png", encodedImage))
                    ));

                    // Add the user's input as the last piece of content
                    List<Content> combinedContents = new ArrayList<>(context); // Copy the context into a new list
                    combinedContents.add(userInputContent); // Add the new user input

                    // Create and send the request with the combined contents
                    GeminiChatCompletionResponse response = getCompletionWithImage(
                            new GeminiChatCompletionRequest(combinedContents));

                    // Log the response
                    System.out.println(response);

                    // Return the desired part of the response
                    return response.candidates().get(0).content().parts().get(0).text();
                })
                .subscribeOn(Schedulers.boundedElastic()) // Execute the blocking file read in a separate thread pool
                .flux(); // Convert this Mono to Flux if needed
    }


}
