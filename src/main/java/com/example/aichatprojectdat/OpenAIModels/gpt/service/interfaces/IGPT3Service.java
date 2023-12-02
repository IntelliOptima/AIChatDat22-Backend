package com.example.aichatprojectdat.OpenAIModels.gpt.service.interfaces;

import org.mvnsearch.chatgpt.model.GPTExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@GPTExchange
public interface IGPT3Service extends IGPT_General {

    @Override
    Flux<String> streamChat(String question);

    @Override
    Mono<String> chat(String question);



}
