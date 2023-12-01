package com.example.aichatprojectdat.message.service;

import com.example.aichatprojectdat.message.model.ReadReceipt;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.Map;
import java.util.Objects;


@Service
@Primary
@RequiredArgsConstructor
public class ReadReceiptServiceImpl implements IReadReceiptService{

    private final R2dbcEntityTemplate entityTemplate;

    public Mono<ReadReceipt> createReadReceipt(ReadReceipt readReceipt) {
        String sql = "INSERT INTO read_receipt (message_id, user_id, has_read) VALUES (:messageId, :userId, :hasRead)";
        return entityTemplate.getDatabaseClient()
                .sql(sql)
                .bind("messageId", readReceipt.messageId())
                .bind("userId", readReceipt.userId())
                .bind("hasRead", readReceipt.hasRead())
                .fetch().rowsUpdated()
                .thenReturn(readReceipt);
    }

    public Mono<Map<Long, Boolean>> findReadReceiptsByMessageId(String messageId) {
        String sql = "SELECT user_id, has_read FROM read_receipt WHERE message_id = :messageId";
        return entityTemplate.getDatabaseClient()
                .sql(sql)
                .bind("messageId", messageId)
                .map((row, metadata) -> Tuples.of(Objects.requireNonNull(row.get("user_id", Long.class)), Objects.requireNonNull(row.get("has_read", Boolean.class))))
                .all()
                .collectMap(Tuple2::getT1, Tuple2::getT2);
    }

}
