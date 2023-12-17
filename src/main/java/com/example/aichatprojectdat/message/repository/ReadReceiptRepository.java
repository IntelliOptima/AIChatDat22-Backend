package com.example.aichatprojectdat.message.repository;

import com.example.aichatprojectdat.message.model.ReadReceipt;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ReadReceiptRepository extends R2dbcRepository<ReadReceipt, String> {

    Mono<Void> deleteByMessageId(String messageId);
}
