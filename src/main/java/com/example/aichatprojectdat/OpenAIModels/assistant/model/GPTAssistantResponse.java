package com.example.aichatprojectdat.OpenAIModels.assistant.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GPTAssistantResponse {

    private String id;
    private String name;
    private String model;
}
