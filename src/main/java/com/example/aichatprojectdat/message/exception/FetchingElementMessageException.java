package com.example.aichatprojectdat.message.exception;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FetchingElementMessageException extends Throwable {
    private final String errorMessage;
}
