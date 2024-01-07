package com.example.aichatprojectdat.ai_models.OpenAIModels.assistant.threads.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThreadResponse {

    private String id;
    private String object;
    private Long createdAt;

}
