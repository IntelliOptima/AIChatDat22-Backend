package com.example.aichatprojectdat.message.service;

import com.example.aichatprojectdat.message.model.ReadReceipt;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;


public interface IReadReceiptService {

    Mono<ReadReceipt> createReadReceipt(ReadReceipt readReceipt);

    Mono<Map<Long, Boolean>> findReadReceiptsByMessageId(String messageId);
}
