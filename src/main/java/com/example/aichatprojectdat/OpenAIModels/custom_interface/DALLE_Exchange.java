package com.example.aichatprojectdat.OpenAIModels.custom_interface;



import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DALLE_Exchange {
    String model() default "dall-e-3";

    int n() default 1;

    String quality() default "standard";

    String responseFormat() default "url";

    /**
     * 256x256, 512x512, or 1024x1024 for dall-e-2
     * 1024x1024, 1792x1024, or 1024x1792 for dall-e-3
     * @return Preferred image size for Dall-e generation
     */
    String size() default "1024x1024";

    /**
     * Available input = [vivid, natural]
     * @return style for dall-e
     */
    String style() default "vivid";
}
