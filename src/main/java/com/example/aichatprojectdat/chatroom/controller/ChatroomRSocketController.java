package com.example.aichatprojectdat.chatroom.controller;


import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.example.aichatprojectdat.OpenAIModels.dall_e.model.generation.ImageGenerationRequest;
import com.example.aichatprojectdat.OpenAIModels.dall_e.service.IDALL_E3ServiceStandard;
import com.example.aichatprojectdat.OpenAIModels.gpt.service.interfaces.IGPT3Service;
import com.example.aichatprojectdat.OpenAIModels.gpt.service.interfaces.IGPT4Service;
import com.example.aichatprojectdat.config.ChunkData;
import com.example.aichatprojectdat.message.model.Message;
import com.example.aichatprojectdat.message.model.ReadReceipt;
import com.example.aichatprojectdat.message.service.IReadReceiptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;

import com.example.aichatprojectdat.message.service.IMessageService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

@Slf4j
@RestController
@RequiredArgsConstructor
@CrossOrigin
public class ChatroomRSocketController {

    private final IMessageService messageService;
    private final IReadReceiptService readReceiptService;
    private final IGPT3Service gpt3Service;
    private final IGPT4Service gpt4Service;
    private final IDALL_E3ServiceStandard iDallE3ServiceStandard;

    private final Map<String, Sinks.Many<Message>> chatroomSinks = new ConcurrentHashMap<>();


    // Map to keep track of connected users in each chatroom
    private final Map<String, Set<Long>> onlineUsers = new ConcurrentHashMap<>();

    // Map to hold and multicast updates about online users
    private final Map<String, Sinks.Many<Long>> userSinks = new ConcurrentHashMap<>();

    //_______________________ RSOCKET -> CHANNEL _______________________________

    // Method for clients to connect and stream chatroom messages
    @MessageMapping("chat.stream.{chatroomId}")
    public Flux<Message> streamMessages(
            @DestinationVariable String chatroomId,
            Mono<String> requestMessage
    ) {
        return requestMessage.doOnNext(s -> log.info("Received message text: " + s))
                .thenMany(chatroomSinks.computeIfAbsent(chatroomId, id -> Sinks.many().replay().latest()).asFlux().onBackpressureBuffer())
                .doFinally(signal -> handleClientDisconnection(chatroomId));

    }

    private void handleClientDisconnection(String chatroomId) {

        log.info("Client disconnected from chatroom: " + chatroomId);
    }

    @MessageMapping("chat.online.{chatroomId}")
    public Flux<Message> streamOnlineUsers(
            @DestinationVariable String chatroomId,
            Mono<String> userConnected) {
        return userConnected
                .map(Long::parseLong)
                .doOnNext(userId -> {
                    log.info("User: " + userId + " has connected");
                    onlineUsers.computeIfAbsent(chatroomId, k -> new HashSet<>()).add(userId);
                })
                .thenMany(Flux.defer(() ->
                        Flux.fromIterable(onlineUsers.get(chatroomId))
                                .map(userId -> Message.builder()
                                                .userId(userId)
                                                .textMessage("User: " + userId + " has just connected")
                                                .chatroomId(chatroomId)
                                                .build())
                ));
    }


    // Method for clients to send messages to a chatroom
    @MessageMapping("chat.send.{chatroomId}")
    public Mono<Void> receiveMessage(@DestinationVariable String chatroomId, Flux<ChunkData> chunkData) {
        return chunkData
                .groupBy(ChunkData::getChunkIdentifier)
                .flatMap(group -> processGroupedChunks(chatroomId, group))
                .then();
    }

    private Mono<Void> processGroupedChunks(String chatroomId, GroupedFlux<String, ChunkData> groupedChunks) {
        return groupedChunks
                .collectSortedList(Comparator.comparing(ChunkData::getStartIndex))
                .filter(list -> list.stream().anyMatch(ChunkData::isLastChunk))
                .flatMap(sortedChunks -> {
                    List<Message> messages = sortedChunks.stream()
                            .map(ChunkData::getChunk)
                            .collect(Collectors.toList());
                    return processMessages(chatroomId, messages);
                });
    }


    private Mono<Void> processMessages(String chatroomId, List<Message> messages) {
        if (messages.isEmpty()) {
            return Mono.empty(); // Return an empty Mono if the message list is empty
        }

        Message lastMessage = messages.get(messages.size() - 1);

        if (lastMessage.getTextMessage().toLowerCase().startsWith("@gpt")) {
            return handleGptContextMessage(messages, chatroomSinks.get(chatroomId)).then();
        } else if (lastMessage.getTextMessage().toLowerCase().startsWith("@dalle")) {
            return handleDallEMessage(lastMessage, chatroomSinks.get(chatroomId)).then();
        } else {
            return processNormalMessages(messages, chatroomSinks.get(chatroomId));
        }
    }


    // Example method for processing normal messages
    private Mono<Void> processNormalMessages(List<Message> messages, Sinks.Many<Message> sink) {
        // Emitting messages, handling other logic
        messages.forEach(message -> emitReceivedMessage(message, sink));
        return Mono.empty();
    }

    public void emitReceivedMessage(Message chatMessage, Sinks.Many<Message> sink) {

        Message receivedMessage = Message.builder()
                .id(chatMessage.getId())
                .userId(chatMessage.getUserId())
                .textMessage(chatMessage.getTextMessage())
                .chatroomId(chatMessage.getChatroomId())
                .build();

        messageService.create(receivedMessage)
                .flatMap(savedMessage -> {
                    ReadReceipt newMessageReceipt = ReadReceipt.of(savedMessage.getId(), savedMessage.getUserId(), true);
                    return readReceiptService.createReadReceipt(newMessageReceipt)
                            .thenReturn(savedMessage); // Chain read receipt creation in the reactive flow
                })
                .subscribe(savedMessage -> {
                    log.info("Read receipt created for message {}", savedMessage);

                    Message messageWithReceipt = Message.builder()
                            .id(savedMessage.getId())
                            .userId(savedMessage.getUserId())
                            .textMessage(savedMessage.getTextMessage())
                            .chatroomId(savedMessage.getChatroomId())
                            .readReceipt(Map.of(savedMessage.getUserId(), true))
                            .createdDate(savedMessage.getCreatedDate())
                            .lastModifiedDate(savedMessage.getLastModifiedDate())
                            .version(savedMessage.getVersion())
                            .build();

                    sink.emitNext(messageWithReceipt, Sinks.EmitFailureHandler.FAIL_FAST);
                }, error -> {
                    log.error("Error creating read receipt", error);
                });

        log.info("Emitting message {}", chatMessage);
    }


    public void handleGptMessage(Message chatMessage, Sinks.Many<Message> sink) {
        StringBuilder gptAnswer = new StringBuilder();
        
        String gptMessageId = UUID.randomUUID().toString();
        Instant createdDate = Instant.now();
        
        gpt3Service.streamChat(chatMessage.getTextMessage().split("@gpt ")[1])
                .doOnNext(chunk -> {
                    String updatedContent = gptAnswer.append(chunk).toString();
                    sink.emitNext(Message.builder()
                                    .id(gptMessageId)
                                    .userId(1L)
                                    .textMessage(updatedContent)
                                    .chatroomId(chatMessage.getChatroomId())
                                    .createdDate(createdDate)
                                    .build(), Sinks.EmitFailureHandler.FAIL_FAST);
                })
                .doOnError(e -> log.error("Error in GPT streaming: {}", e.getMessage()))
                .publishOn(Schedulers.boundedElastic())
                .doFinally(signalType -> {
                    sink.emitNext(Message.builder()
                                    .id(gptMessageId)
                                    .userId(1L)
                                    .textMessage("Gpt Finished message")
                                    .chatroomId(chatMessage.getChatroomId())
                                    .createdDate(createdDate)
                                    .build(), Sinks.EmitFailureHandler.FAIL_FAST);


                    Message gptCompleteMessage = Message.builder()
                            .id(gptMessageId)
                            .userId(1L)
                            .textMessage(gptAnswer.toString())
                            .chatroomId(chatMessage.getChatroomId())
                            .createdDate(createdDate)
                            .build();

                    messageService.create(gptCompleteMessage).subscribe();
                    gptAnswer.setLength(0);
                }).subscribe();
    }


    public Mono<Void> handleGptContextMessage(List<Message> messages, Sinks.Many<Message> sink) {
        log.info("IM RUNNING CONTEXT!");
        StringBuilder gptAnswer = new StringBuilder();

        String gptMessageId = UUID.randomUUID().toString();
        Instant createdDate = Instant.now();

        return gpt3Service.streamChatContext(messages)
                .doOnNext(chunk -> {
                    String updatedContent = gptAnswer.append(chunk).toString();
                    sink.emitNext(Message.builder()
                            .id(gptMessageId)
                            .userId(1L)
                            .textMessage(updatedContent)
                            .chatroomId(messages.get(messages.size() -1).getChatroomId())
                            .createdDate(createdDate)
                            .build(), Sinks.EmitFailureHandler.FAIL_FAST);
                })
                .doOnError(e -> log.error("Error in GPT streaming: {}", e.getMessage()))
                .publishOn(Schedulers.boundedElastic())
                .doFinally(signalType -> {
                    sink.emitNext(Message.builder()
                            .id(gptMessageId)
                            .userId(1L)
                            .textMessage("Gpt Finished message")
                            .chatroomId(messages.get(messages.size() -1).getChatroomId())
                            .createdDate(createdDate)
                            .build(), Sinks.EmitFailureHandler.FAIL_FAST);


                    Message gptCompleteMessage = Message.builder()
                            .id(gptMessageId)
                            .userId(1L)
                            .textMessage(gptAnswer.toString())
                            .chatroomId(messages.get(messages.size() -1).getChatroomId())
                            .createdDate(createdDate)
                            .build();

                    messageService.create(gptCompleteMessage).subscribe();
                    gptAnswer.setLength(0);
                }).then();
    }


    public Mono<Void> handleDallEMessage(Message chatMessage, Sinks.Many<Message> sink) {

        AtomicReference<Message> dalleMessageRef = new AtomicReference<>(); // AtomicReference to hold the dalleMessage

        return iDallE3ServiceStandard.generateImage(chatMessage.getTextMessage().split("@dalle ")[1])
                .doFirst(() -> System.out.println(ImageGenerationRequest.of(chatMessage.getTextMessage()).toString()))
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(response -> {
                    Instant createdTime = Instant.now();
                    Message dalleMessage = Message.builder()
                            .userId(2L)
                            .textMessage(response.getImageList().get(0).getUrl())
                            .chatroomId(chatMessage.getChatroomId())
                            .createdDate(createdTime)
                            .lastModifiedDate(createdTime)
                            .build();

                    dalleMessageRef.set(dalleMessage); // Set the dalleMessage in the AtomicReference

                    sink.emitNext(dalleMessage, Sinks.EmitFailureHandler.FAIL_FAST);
                })
                .doFinally(signalType -> {
                    Message messageToSave = dalleMessageRef.get(); // Retrieve the dalleMessage
                    if (messageToSave != null) {
                        messageService.create(messageToSave).subscribe(); // Save the message
                    }
                }).then();
    }



}