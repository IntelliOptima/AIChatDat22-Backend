package com.example.aichatprojectdat.ai_models.OpenAIModels.assistant.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GPTAssistantRequest {

    private String name;
    private String instructions;
    @Builder.Default
    private List<Map<String, String>> tools = new ArrayList<>();
    private String model;


}
