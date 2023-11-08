package com.example.aichatprojectdat.ChatGpt.service;

import com.example.aichatprojectdat.ChatGpt.model.Choice;
import lombok.RequiredArgsConstructor;
import org.mvnsearch.chatgpt.model.completion.chat.ChatCompletionRequest;
import org.mvnsearch.chatgpt.model.completion.chat.ChatCompletionResponse;
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



    @Override
    public Flux<String> streamChat(String question) {
        return chatGPTService.stream(ChatCompletionRequest.of(question))
                .map(ChatCompletionResponse::getReplyText);
    }
}
