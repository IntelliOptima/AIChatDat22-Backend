package com.example.aichatprojectdat.OpenAIModels.gpt.service.interfaces;


import org.mvnsearch.chatgpt.model.GPTExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@GPTExchange(model = "gpt-4-1106-preview", maxTokens = 1000)
public interface IGPT4Service extends IGPT_General {

    @Override
    Flux<String> streamChat(String question);

    @Override
    Mono<String> chat(String question);
}
