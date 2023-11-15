package com.example.aichatprojectdat.ChatGpt.model;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "role",
        "content"
})
@Getter
@Setter
@NoArgsConstructor
public class GptMessage {

    @JsonProperty("role")
    private String role;
    @JsonProperty("content")
    private String content;
   /* @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    */

    public GptMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }


}
