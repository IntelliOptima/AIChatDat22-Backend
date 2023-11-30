package com.example.aichatprojectdat.OpenAIModels.gpt.service;

import com.example.aichatprojectdat.OpenAIModels.gpt.service.interfaces.IGPT3Service;
import lombok.RequiredArgsConstructor;
import org.mvnsearch.chatgpt.model.completion.chat.ChatCompletionRequest;
import org.mvnsearch.chatgpt.model.completion.chat.ChatCompletionResponse;
import org.mvnsearch.chatgpt.spring.service.ChatGPTService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Primary
@Service
public class GPT3ServiceImpl implements IGPT3Service {

    private final ChatGPTService chatGPTService;

    @Override
    public Mono<String> chat(String content) {
        return chatGPTService.chat(ChatCompletionRequest.of(content))
                .map(ChatCompletionResponse::getReplyText);
    }

    @Override
    public Flux<String> streamChat(String question) {
        return chatGPTService.stream(ChatCompletionRequest.of(question))
                .map(ChatCompletionResponse::getReplyText);
    }
}