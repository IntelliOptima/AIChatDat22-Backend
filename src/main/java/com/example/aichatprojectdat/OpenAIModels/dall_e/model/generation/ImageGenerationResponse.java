package com.example.aichatprojectdat.OpenAIModels.dall_e.model.generation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ImageGenerationResponse {

    @JsonProperty("created")
    private LocalDate createdDate;

    @JsonProperty("data")
    private List<ImageGenerationMessage> imageList;
}
