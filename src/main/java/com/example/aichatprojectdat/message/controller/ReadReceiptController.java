package com.example.aichatprojectdat.message.controller;

import com.example.aichatprojectdat.message.model.ReadReceipt;
import com.example.aichatprojectdat.message.service.IReadReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/readReceipt")
@CrossOrigin
@RequiredArgsConstructor
public class ReadReceiptController {

    private final IReadReceiptService readReceiptService;


    @PutMapping
    public Mono<ResponseEntity<Mono<ReadReceipt>>> updateReadReceiptForMessage(@RequestBody ReadReceipt receipt) {
        return Mono.just(ResponseEntity.ok().body(readReceiptService.create(receipt)));
    }
}
