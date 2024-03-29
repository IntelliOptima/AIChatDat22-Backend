package com.example.aichatprojectdat.ai_models.OpenAIModels.custom_interface;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GenerationCompletions {

    String model() default "";

    int n() default 1;

    String quality() default "";

    String responseFormat() default "";

    String size() default "";

    String style() default "";
}
