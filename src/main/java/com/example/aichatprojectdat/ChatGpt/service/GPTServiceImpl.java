package com.example.aichatprojectdat.ChatGpt.service;

import com.example.aichatprojectdat.ChatGpt.model.Choice;
import lombok.RequiredArgsConstructor;
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
    private final ChatCompletionRequest chatCompletionRequest = createGPTRequester();

    public Mono<String> chat(String content) {
        return chatGPTService.chat(ChatCompletionRequest.of(content))
                .map(chatgptReply -> "CHATGPT: " + chatgptReply.getReplyText());
    }

    @Override
    public Flux<String> streamChat(String question ){
        this.chatCompletionRequest.addMessage(ChatMessage.userMessage(question));
        return chatGPTService.stream(chatCompletionRequest)
                .map(chatgptReply -> "CHATGPT: " + chatgptReply.getReplyText());
    }


    private ChatCompletionRequest createGPTRequester() {
        ChatCompletionRequest newRequester = new ChatCompletionRequest();
        newRequester.setStream(true);
        newRequester.setMaxTokens(1000);
        newRequester.setTemperature(1.0);
        return newRequester;
    }
}
