package com.example.aichatprojectdat.ChatGpt.model;

import java.util.LinkedHashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.annotation.processing.Generated;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "prompt_tokens",
        "completion_tokens",
        "total_tokens"
})
@Getter
@Setter
@NoArgsConstructor
public class Usage {

    @JsonProperty("prompt_tokens")
    private Integer promptTokens;
    @JsonProperty("completion_tokens")
    private Integer completionTokens;
    @JsonProperty("total_tokens")
    private Integer totalTokens;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

}
