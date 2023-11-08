package com.example.aichatprojectdat.ChatGpt.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "model",
        "messages",
        "n",
        "temperature",
        "max_tokens",
        "stream",
        "presence_penalty"
})
@Getter
@Setter
@NoArgsConstructor
public class GptRequest {

    @JsonProperty("model")
    private String model;
    @JsonProperty("messages")
    private List<GptMessage> gptMessages;
    @JsonProperty("n")
    private Integer n;
    @JsonProperty("temperature")
    private Integer temperature;
    @JsonProperty("max_tokens")
    private Integer maxTokens;
    @JsonProperty("stream")
    private Boolean stream;
    @JsonProperty("presence_penalty")
    private Integer presencePenalty;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();


}
