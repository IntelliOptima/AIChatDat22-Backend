package com.example.aichatprojectdat.message.repository;

import com.example.aichatprojectdat.message.model.ReadReceipt;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ReadReceiptRepository extends R2dbcRepository<ReadReceipt, String> {

    Flux<ReadReceipt> findAllByMessageId(String messageId);

}
