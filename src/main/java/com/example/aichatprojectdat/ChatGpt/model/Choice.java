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

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "index",
        "message",
        "finish_reason"
})
@Getter
@Setter
@NoArgsConstructor
public class Choice {

    @JsonProperty("index")
    private Integer index;
    @JsonProperty("message")
    private GptMessage gptMessage;
    @JsonProperty("finish_reason")
    private String finishReason;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    @Override
    public String toString() {
        return "Choice{" +
                "index=" + index +
                ", gptMessage=" + gptMessage +
                ", finishReason='" + finishReason + '\'' +
                ", additionalProperties=" + additionalProperties +
                '}';
    }
}
