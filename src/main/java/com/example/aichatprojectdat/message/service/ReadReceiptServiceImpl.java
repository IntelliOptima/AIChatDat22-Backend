package com.example.aichatprojectdat.message.service;

import com.example.aichatprojectdat.message.model.ReadReceipt;
import com.example.aichatprojectdat.message.repository.ReadReceiptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Service
@Primary
@RequiredArgsConstructor
public class ReadReceiptServiceImpl implements IReadReceiptService {

    private final ReadReceiptRepository repository;

    @Override
    public Mono<ReadReceipt> create(ReadReceipt readReceipt) {
        return repository.save(readReceipt);
    }

    @Override
    public Flux<ReadReceipt> findAllByMessageId(String messageId) {
        return repository.findAllByMessageId(messageId);
    }
}
