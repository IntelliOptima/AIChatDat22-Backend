package com.example.aichatprojectdat.ai_models.OpenAIModels.assistant.threads.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThreadMessageContent {

    private String type;
    private ThreadMessageContentText text;
}
