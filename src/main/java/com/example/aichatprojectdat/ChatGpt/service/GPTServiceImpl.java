package com.example.aichatprojectdat.ChatGpt.service;

import com.example.aichatprojectdat.ChatGpt.model.Choice;
import lombok.RequiredArgsConstructor;
import org.mvnsearch.chatgpt.model.ChatCompletion;
import org.mvnsearch.chatgpt.model.completion.chat.ChatCompletionRequest;
import org.mvnsearch.chatgpt.model.completion.chat.ChatCompletionResponse;
import org.mvnsearch.chatgpt.model.completion.chat.ChatMessage;
import org.mvnsearch.chatgpt.spring.service.ChatGPTService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
@Primary
@Service
public class GPTServiceImpl implements IGPTService {

    private final ChatGPTService chatGPTService;

    public Mono<String> chat(String content) {
        return chatGPTService.chat(ChatCompletionRequest.of(content))
                .map(ChatCompletionResponse::getReplyText);
    }

    @Override
    public Flux<String> streamChat(String question) {
        // Create a new ChatCompletionRequest instance
        var chatCompletionRequest = ChatCompletionRequest.of(question);
        chatCompletionRequest.setStream(true);
        chatCompletionRequest.setMaxTokens(1000);
        chatCompletionRequest.setTemperature(1.0);
        chatCompletionRequest.setModel("gpt-4-1106-preview");
        // Use chatCompletionRequest in the stream method
        return chatGPTService.stream(chatCompletionRequest)
                .map(ChatCompletionResponse::getReplyText);
    }
}
