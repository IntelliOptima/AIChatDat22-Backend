package com.example.aichatprojectdat.aspects.AdviceAnnotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ValidateParams
 * Interface for validating parameters
 * @see com.example.aichatprojectdat.aspects.ValidationAspect
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ValidateParams {
}