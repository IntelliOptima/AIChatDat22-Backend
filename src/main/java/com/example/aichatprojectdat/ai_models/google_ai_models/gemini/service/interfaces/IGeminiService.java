package com.example.aichatprojectdat.ai_models.google_ai_models.gemini.service.interfaces;

import com.example.aichatprojectdat.message.model.Message;
import reactor.core.publisher.Flux;

import java.util.List;

public interface IGeminiService {

    Flux<String> streamChatContext(List<Message> messages);
}
