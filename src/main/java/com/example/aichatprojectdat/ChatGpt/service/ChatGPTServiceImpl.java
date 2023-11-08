package com.example.aichatprojectdat.ChatGpt.service;

import com.example.aichatprojectdat.ChatGpt.model.Choice;
import com.example.aichatprojectdat.ChatGpt.model.GptMessage;
import com.example.aichatprojectdat.ChatGpt.model.GptRequest;
import com.example.aichatprojectdat.ChatGpt.model.GptResponse;

import org.mvnsearch.chatgpt.model.completion.chat.ChatCompletionRequest;
import org.mvnsearch.chatgpt.model.completion.chat.ChatCompletionResponse;
import org.mvnsearch.chatgpt.spring.service.ChatGPTService;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;


@Primary
@Service
public class ChatGPTServiceImpl implements IChatGPTService {

    private final ChatGPTService chatGPTService;

    private final WebClient webClient;

    public ChatGPTServiceImpl(WebClient.Builder webClientBuilder, ChatGPTService chatGPTService) {
        this.webClient = webClientBuilder.baseUrl("https://api.openai.com/v1/chat/completions").build();
        this.chatGPTService = chatGPTService;
    }

    @Override
    public Mono<List<Choice>> getAnswerFromGPT(String question) {
        GptRequest gptRequest = new GptRequest();
        gptRequest.setModel("gpt-3.5-turbo"); //Cheapest Model
        List<GptMessage> listMessages = new ArrayList<>();
        listMessages.add(new GptMessage("system", "You are a helpful assistant."));
        listMessages.add(new GptMessage("user", question));
        gptRequest.setGptMessages(listMessages);
        gptRequest.setN(1); //Number of answers from GPT
        gptRequest.setTemperature(1); //0-2 How Creative is GPT
        gptRequest.setMaxTokens(1000); //Answer Length
        gptRequest.setStream(false);
        gptRequest.setPresencePenalty(1);

        System.out.println(gptRequest);

        return webClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .headers(h -> h.setBearerAuth(""))
                .bodyValue(gptRequest)
                .retrieve()
                .bodyToMono(GptResponse.class)
                .map(GptResponse::getChoices)
                .doOnNext(choices -> {
                    if (!choices.isEmpty()) {
                        String answer = choices.get(0).toString(); // Assuming there's at least one choice
                        System.out.println("Answer from ChatGPT: " + answer);
                    }
                })
                .onErrorResume(throwable -> Mono.just(new ArrayList<>()));
    }

    public Flux<String> streamChat(String question) {
        return chatGPTService.stream(ChatCompletionRequest.of(question))
                .map(ChatCompletionResponse::getReplyText);
    }

}
