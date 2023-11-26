package com.example.aichatprojectdat.message.service;

import com.example.aichatprojectdat.message.model.ReadReceipt;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface IReadReceiptService {

    Mono<ReadReceipt> create(ReadReceipt readReceipt);

    Flux<ReadReceipt> findAllByMessageId(String messageId);
}
