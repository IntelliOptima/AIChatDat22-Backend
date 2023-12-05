package com.example.aichatprojectdat.OpenAIModels.assistant.threads.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ThreadRequest {
    
    private List<ThreadMessage> threadMessage;

}
