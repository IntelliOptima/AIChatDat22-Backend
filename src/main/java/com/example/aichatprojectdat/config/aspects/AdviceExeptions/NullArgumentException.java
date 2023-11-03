package com.example.aichatprojectdat.config.aspects.AdviceExeptions;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NullArgumentException extends RuntimeException {
    private final String message;
}
