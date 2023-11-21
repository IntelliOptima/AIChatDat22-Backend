package com.example.aichatprojectdat.config.aspects.AdviceAnnotations;

import java.lang.annotation.*;

/**
 * ValidateParams
 * Interface for validating parameters
 * @see com.example.aichatprojectdat.config.aspects.ValidationAspect
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ValidateParams {
}
