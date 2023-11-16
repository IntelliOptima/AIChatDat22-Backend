package com.example.aichatprojectdat.user.exception;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomDuplicateUserException extends RuntimeException {

    private final String error;
}
