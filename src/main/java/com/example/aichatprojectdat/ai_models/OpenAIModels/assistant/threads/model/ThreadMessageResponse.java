package com.example.aichatprojectdat.ai_models.OpenAIModels.assistant.threads.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThreadMessageResponse {

    private String id;
    private String object;
    private Long created;
    private String threadId;
    private String role;
    private List<ThreadMessageContent> content;
    private String assistantId;
    private String runId;
}
