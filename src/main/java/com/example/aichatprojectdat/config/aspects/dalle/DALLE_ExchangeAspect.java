package com.example.aichatprojectdat.config.aspects.dalle;

import com.example.aichatprojectdat.open_ai_models.custom_interface.DALLE_Exchange;
import com.example.aichatprojectdat.open_ai_models.dall_e.model.generation.ImageGenerationRequest;

import com.example.aichatprojectdat.open_ai_models.dall_e.model.generation.ImageGenerationResponse;
import com.example.aichatprojectdat.open_ai_models.dall_e.spring.service.interfaces.IDALL_EService;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;



import org.aspectj.lang.annotation.Pointcut;


public class DALLE_ExchangeAspect {

    private final IDALL_EService iDallEService;

    public DALLE_ExchangeAspect(IDALL_EService iDallEService) {
        this.iDallEService = iDallEService;
    }


    @Pointcut("within(@com.example.aichatprojectdat.open_ai_models.custom_interface.DALLE_Exchange *)")
    public void dallEExchangeInterfacePointcut() {}

    @Around("dallEExchangeInterfacePointcut()")
    public Object aroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        DALLE_Exchange dalleExchangeAnnotation = getDALLE_ExchangeAnnotation(joinPoint);

        ImageGenerationRequest request = new ImageGenerationRequest();
        if (dalleExchangeAnnotation != null) {
            configureRequest(request, dalleExchangeAnnotation);
        }

        String methodName = joinPoint.getSignature().getName();
        return switch (methodName) {
            case "generateImage" -> iDallEService.generateImage(request).map(ImageGenerationResponse::getImageList);
            case "editGenerateImage" -> iDallEService.editGenerateImage(request).map(ImageGenerationResponse::getImageList);
            case "variationGenerateImage" -> iDallEService.variationGenerateImage(request).map(ImageGenerationResponse::getImageList);
            default -> throw new IllegalArgumentException("Unsupported method: " + methodName);
        };
    }

    private DALLE_Exchange getDALLE_ExchangeAnnotation(ProceedingJoinPoint joinPoint) {
        // Get the class of the object the method is being called on
        Class<?> targetClass = joinPoint.getTarget().getClass();

        // Check if the class itself is annotated with @DALLE_Exchange
        DALLE_Exchange annotation = targetClass.getAnnotation(DALLE_Exchange.class);
        if (annotation != null) {
            return annotation;
        }

        // If not found on the class, check its interfaces
        for (Class<?> interfaceClass : targetClass.getInterfaces()) {
            annotation = interfaceClass.getAnnotation(DALLE_Exchange.class);
            if (annotation != null) {
                return annotation;
            }
        }
        // If the annotation is not found, return null or throw an exception
        return null;
    }

    private void configureRequest(ImageGenerationRequest request, DALLE_Exchange annotation) {

            if (!annotation.model().isEmpty()) {
                request.setModel(annotation.model());
            }

            if (annotation.n() > 1) {
                request.setN(annotation.n());
            }

            if (!annotation.size().isEmpty()) {
                request.setSize(annotation.size());
            }

            if (!annotation.responseFormat().isEmpty()) {
                request.setResponseFormat(annotation.responseFormat());
            }

            if (!annotation.style().isEmpty()) {
                request.setStyle(annotation.style());
            }

            if (!annotation.quality().isEmpty()) {
                request.setQuality(annotation.quality());
            }
    }
}

