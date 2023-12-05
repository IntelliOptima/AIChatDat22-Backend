package com.example.aichatprojectdat.OpenAIModels.gpt.service;

import com.example.aichatprojectdat.OpenAIModels.gpt.service.interfaces.IGPT3Service;
import com.example.aichatprojectdat.message.model.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mvnsearch.chatgpt.model.completion.chat.ChatCompletionRequest;
import org.mvnsearch.chatgpt.model.completion.chat.ChatCompletionResponse;
import org.mvnsearch.chatgpt.model.completion.chat.ChatMessage;
import org.mvnsearch.chatgpt.spring.service.ChatGPTService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Primary
@Service
@Slf4j
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
        // Process messages to build the chat completion request
        var chatCompletionRequest = buildChatCompletionRequest(messages);

        // Use the built request in the Flux stream
        return chatGPTService.stream(chatCompletionRequest)
                .map(ChatCompletionResponse::getReplyText);
    }

    private ChatCompletionRequest buildChatCompletionRequest(List<Message> messages) {
        var chatCompletionRequest = new ChatCompletionRequest();
        chatCompletionRequest.addMessage(ChatMessage.systemMessage(gptInstruction()));
        System.out.println(messages);

        messages.get(messages.size() - 1).setCreatedDate(Instant.now());

        int tokenCount = 0;
        int maxTokens = 32_000;
        Set<String> addedMessageIds = new HashSet<>();

        for (Message message : messages) {
            if (!addedMessageIds.contains(message.getId())) {
                String context = getMessageContext(message);
                tokenCount += context.length();

                if (tokenCount <= maxTokens) {
                    if (message.getUserId() == 2L) continue;
                    if (message.getTextMessage().toLowerCase().contains("@gpt")) {
                        String gptQuestion = message.getTextMessage().replace("@gpt", " ");
                        chatCompletionRequest.addMessage(ChatMessage.userMessage(gptQuestion));
                    } else if (message.getUserId() == 1L) {
                        chatCompletionRequest.addMessage(ChatMessage.assistantMessage(context));
                    } else {
                        chatCompletionRequest.addMessage(ChatMessage.userMessage(context));
                    }
                    log.info("Adding message: " + message);
                    addedMessageIds.add(message.getId());
                } else {
                    break;
                }
            }
        }

        chatCompletionRequest.setMaxTokens(4096);
        chatCompletionRequest.setTemperature(1.0);
        chatCompletionRequest.setModel("gpt-3.5-turbo-1106");
        return chatCompletionRequest;
    }

    public String getMessageContext(Message message) {
        if (message.getUserId() != 1) {
            return message.getUserId() + " said: " + message.getTextMessage();
        }
        return message.getTextMessage();
    }


    private String gptInstruction() {
        return """
                As the intelligent assistant in this multi-user chatroom, be aware that your assigned user ID is 1.
                This means that when you are mentioned or addressed in the chat, it will be under the user ID 1.
                In this environment, you will see messages formatted with a user ID followed by 'said:', and then
                the message content, like this: '12345 said: Can you give me recipe ideas?'. Treat each user ID as
                the respective individual's name, and remember that your responses will be coming from user ID 1.
                Your primary task is to address the most recent messages first, as they are the most immediate in the
                conversation. When replying, refer to the users by their respective IDs and provide informed and thoughtful
                responses to their questions or comments. Your aim is to engage actively and supportively in the chatroom,
                treating each user ID as a unique individual, and responding from your designated user ID, which is 1.
                """;
    }




}
