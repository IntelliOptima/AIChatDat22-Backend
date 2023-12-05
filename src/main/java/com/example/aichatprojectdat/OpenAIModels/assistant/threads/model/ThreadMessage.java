package com.example.aichatprojectdat.OpenAIModels.assistant.threads.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThreadMessage {

    private String role;
    private String content;

}
