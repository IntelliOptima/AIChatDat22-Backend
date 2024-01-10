package com.example.aichatprojectdat.ai_models.stability_ai_models.stablediffusion.models.text_to_image.JSONStructureRequest;

import com.example.aichatprojectdat.ai_models.stability_ai_models.stablediffusion.models.text_to_image.StableDiffusionTextToImageGenerationRequestBuilder;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StableDiffusionTextToImageRequest {

    private String height;

    private String width;

    @JsonProperty("text_prompts")
    private List<TextPrompt> textPrompts = new ArrayList<>();

    /**
     * Default: 7
     * How strictly the diffusion process adheres to the prompt text (higher values keep your image closer to your prompt)
     */
    private int cfgScale;

    @JsonProperty("clip_guidance_preset")
    private String clipGuidancePreset;

    /**
     * Which sampler to use for the diffusion process. If this value is omitted we'll stable diffusion select an appropriate sampler automatically.
     */
    private String sampler;

    /**
     * Number of images to generate
     */
    private int samples;

    /**
     * Default: 0
     * Random noise seed (omit this option or use 0 for a random seed)
     */
    private float seed;

    /**
     * Default: 30
     * Number of diffusion steps to run.
     */
    private int steps;

    /**
     * <blockquote><pre>
     * 3d-model
     * analog-film
     * anime
     * cinematic
     * comic-book
     * digital-art
     * enhance
     * fantasy-art
     * isometric
     * line-art
     * low-poly
     * modeling-compound
     * neon-punk
     * origami
     * photographic
     * pixel-art
     * tile-texture
     *
     * </pre></blockquote>
     * Pass in a style preset to guide the image model towards a particular style.
     */
    @JsonProperty("style_preset")
    private String stylePreset;

    public static StableDiffusionTextToImageRequest of(@NonNull String textPrompt, float weight) {
       return StableDiffusionTextToImageGenerationRequestBuilder.of(textPrompt, weight).build();
    }

    public void addTextPrompt(TextPrompt textPrompt) {
        this.textPrompts.add(textPrompt);
    }

}
