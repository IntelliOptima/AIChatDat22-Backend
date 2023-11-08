package com.example.aichatprojectdat.ChatGpt.service;

import com.example.aichatprojectdat.ChatGpt.model.Choice;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IChatGPTService {

    Mono<List<Choice>> getAnswerFromGPT(String question);
}
