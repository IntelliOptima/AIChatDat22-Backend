package com.example.aichatprojectdat.OpenAIModels.gpt.service;

import com.example.aichatprojectdat.OpenAIModels.gpt.service.interfaces.IGPT4Service;
import lombok.RequiredArgsConstructor;
import org.mvnsearch.chatgpt.model.completion.chat.ChatCompletionRequest;
import org.mvnsearch.chatgpt.model.completion.chat.ChatCompletionResponse;
import org.mvnsearch.chatgpt.spring.service.ChatGPTService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class GPT4ServiceImpl implements IGPT4Service {

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
