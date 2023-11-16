package com.example.aichatprojectdat.ChatGpt.service;

import org.mvnsearch.chatgpt.model.ChatCompletion;
import org.mvnsearch.chatgpt.model.GPTExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@GPTExchange
public interface IGPTService {

    @ChatCompletion("You are a helpful assistant.")
    Flux<String> streamChat(String question);

    @ChatCompletion("You are a helpful assistant.")
    Mono<String> chat(String question);
}
