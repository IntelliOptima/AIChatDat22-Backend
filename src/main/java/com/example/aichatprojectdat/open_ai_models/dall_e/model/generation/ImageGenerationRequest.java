package com.example.aichatprojectdat.open_ai_models.dall_e.model.generation;

import com.example.aichatprojectdat.open_ai_models.dall_e.model.ImageRequestBuilder;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.io.File;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ImageGenerationRequest {

    private File image;

    private String prompt;

    private String model;

    private int n;

    private String quality;

    @JsonProperty("response_format")
    private String responseFormat;

    private String size;

    private String style;


    public static ImageGenerationRequest of(@NonNull String userPrompt) {
        return ImageRequestBuilder.of(userPrompt).build();
    }

    public static ImageGenerationRequest of(@NonNull String userPrompt, @NonNull File imageFile) {
        return ImageRequestBuilder.of(imageFile, userPrompt).build();
    }

}
