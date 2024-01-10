package com.example.aichatprojectdat.ai_models.OpenAIModels.dall_e.model.generation;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageGenerationMessage {

    private String url;

    @JsonProperty("revised_prompt")
    private String revisedPrompt;
}
