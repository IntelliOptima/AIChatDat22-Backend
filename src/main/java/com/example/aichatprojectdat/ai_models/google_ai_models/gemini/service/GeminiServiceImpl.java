package com.example.aichatprojectdat.ai_models.google_ai_models.gemini.service;

import com.example.aichatprojectdat.ai_models.google_ai_models.gemini.model.completion.chat.JSONStructureRequest.Content;
import com.example.aichatprojectdat.ai_models.google_ai_models.gemini.model.completion.chat.JSONStructureRequest.TextPart;
import com.example.aichatprojectdat.ai_models.google_ai_models.gemini.service.interfaces.IGeminiService;
import com.example.aichatprojectdat.ai_models.google_ai_models.gemini.spring.service.GeminiService;
import com.example.aichatprojectdat.message.model.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class GeminiServiceImpl implements IGeminiService {

    private final GeminiService geminiService;
    private static final long USER_ID_GPT = 1L;
    private static final int USER_ID_DALLE = 2;
    private static final long USER_ID_GEMINI = 3L;

    @Override
    public Flux<String> streamChatContext(List<Message> messages) {
        List<Content> contents = createListOfContentWithContext(messages);
        return geminiService.getCompletion(contents);
    }

    private List<Content> createListOfContentWithContext(List<Message> messages) {
        return messages.stream()
                .filter(message -> message.getUserId() != USER_ID_DALLE)
                .map(this::messageToContent)
                .toList();
    }

    private Content messageToContent(Message message) {
        if (message.getTextMessage().toLowerCase().startsWith("@gemini")) {
            String question = message.getTextMessage().replace("@gemini ", "");
            return new Content("user", List.of(new TextPart(question)));
        } else if (message.getUserId() == USER_ID_GPT) {
            return new Content("user", List.of(new TextPart("GPT Answer: " + message.getTextMessage())));
        } else if (message.getUserId() == USER_ID_GEMINI) {
            return new Content("model", List.of(new TextPart(message.getTextMessage())));
        } else {
            // Handle default or unknown case, or throw an exception if appropriate
            log.warn("Unhandled message type: {}", message);
            return null; // or some default Content
        }
    }
}
