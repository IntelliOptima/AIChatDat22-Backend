package com.example.aichatprojectdat.open_ai_models.gpt.service.interfaces;

import org.mvnsearch.chatgpt.model.GPTExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@GPTExchange(model = "gpt-3.5-turbo-1106", maxTokens = 1000)
public interface IGPT3Service extends IGPT_General {

    @Override
    Flux<String> streamChat(String question);

    @Override
    Mono<String> chat(String question);



}
