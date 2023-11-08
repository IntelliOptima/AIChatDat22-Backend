package com.example.aichatprojectdat.ChatGpt.service;

import com.example.aichatprojectdat.ChatGpt.model.Choice;
import com.example.aichatprojectdat.ChatGpt.model.GptMessage;
import com.example.aichatprojectdat.ChatGpt.model.GptRequest;
import com.example.aichatprojectdat.ChatGpt.model.GptResponse;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Primary

@Service
public class ChatGPTServiceImpl implements IChatGPTService{

    private final WebClient webClient;

    public ChatGPTServiceImpl(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.openai.com/v1/chat/completions").build();
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
        gptRequest.setMaxTokens(2); //Answer Length
        gptRequest.setStream(false);
        gptRequest.setPresencePenalty(1);

        System.out.println(gptRequest);

        return webClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .headers(h -> h.setBearerAuth("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"))
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
}
