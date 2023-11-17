package com.example.aichatprojectdat.chatroom.exception;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NotFoundException extends RuntimeException{

    private final String message;
}
