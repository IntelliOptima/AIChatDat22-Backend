package com.example.aichatprojectdat.OpenAIModels.gpt.service;

import com.example.aichatprojectdat.OpenAIModels.gpt.service.interfaces.IGPT3Service;
import com.example.aichatprojectdat.message.model.Message;
import lombok.RequiredArgsConstructor;
import org.mvnsearch.chatgpt.model.completion.chat.ChatCompletionRequest;
import org.mvnsearch.chatgpt.model.completion.chat.ChatCompletionResponse;
import org.mvnsearch.chatgpt.model.completion.chat.ChatMessage;
import org.mvnsearch.chatgpt.spring.service.ChatGPTService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Primary
@Service
public class GPT3ServiceImpl implements IGPT3Service {

    private final ChatGPTService chatGPTService;

    @Override
    public Mono<String> chat(String content) {
        return chatGPTService.chat(ChatCompletionRequest.of(content))
                .map(ChatCompletionResponse::getReplyText);
    }

    @Override
    public Flux<String> streamChat(String question) {
        var chatCompletionRequest = ChatCompletionRequest.of(question);
        chatCompletionRequest.setMaxTokens(1000);
        chatCompletionRequest.setTemperature(1.0);
        chatCompletionRequest.setModel("gpt-3.5-turbo-1106");
        return chatGPTService.stream(chatCompletionRequest)
                .map(ChatCompletionResponse::getReplyText);
    }


    @Override
    public Flux<String> streamChatContext(List<Message> messages) {
        var chatCompletionRequest = new ChatCompletionRequest();
        chatCompletionRequest.addMessage(ChatMessage.systemMessage(gptInstruction()));

        // Sort messages from newest to oldest
        messages.sort(Comparator.comparing(Message::getCreatedDate).reversed());

        int tokenCount = 0;
        int maxTokens = 32_000; // Set your max token limit

        for (Message message : messages) {
            String context = getMessageContext(message);
            // Approximate token count by character count
            tokenCount += context.length();

            if (tokenCount <= maxTokens) {
                chatCompletionRequest.addMessage(ChatMessage.userMessage(context));
            } else {
                break;
            }
        }

        chatCompletionRequest.setMaxTokens(4096);
        chatCompletionRequest.setTemperature(1.0);
        chatCompletionRequest.setModel("gpt-3.5-turbo-1106");
        return chatGPTService.stream(chatCompletionRequest)
                .map(ChatCompletionResponse::getReplyText);
    }


    public String getMessageContext(Message message) {
        return message.getUserId() + " said: " + message.getTextMessage();
    }

    private String gptInstruction() {
        return """
            As an intelligent assistant in a multi-user chatroom, your role is to interact with users by responding
            to their messages. Each message in this chatroom is formatted with a user ID followed by 'said:',
            and then the message content, like '12345 said: Can you give me recipe ideas?'. Treat each user ID as
            the name of the person speaking.
            Prioritize responding to the newest messages first, as these are the most
            immediate and relevant to the conversation. Address the user by their ID when
            replying, and offer helpful and informed responses to their questions or comments.
            Your aim is to engage with users by providing timely and contextually appropriate assistance,
            treating each user ID as a unique individual in the chat. Focus on the most recent queries to
            ensure the conversation is current and responsive to the latest inputs.
            """;
    }




}
