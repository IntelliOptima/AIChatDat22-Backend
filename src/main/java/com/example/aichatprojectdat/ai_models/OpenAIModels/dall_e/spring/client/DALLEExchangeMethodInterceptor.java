package com.example.aichatprojectdat.ai_models.OpenAIModels.dall_e.spring.client;


import com.example.aichatprojectdat.ai_models.OpenAIModels.custom_interface.DALLE_Exchange;
import com.example.aichatprojectdat.ai_models.OpenAIModels.dall_e.model.generation.ImageGenerationRequest;
import com.example.aichatprojectdat.ai_models.OpenAIModels.dall_e.spring.service.interfaces.IDALL_EService;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DALLEExchangeMethodInterceptor implements MethodInterceptor {

    private static final Logger log = LoggerFactory.getLogger(DALLEExchangeMethodInterceptor.class);
    private final IDALL_EService dallEService;

    public DALLEExchangeMethodInterceptor(IDALL_EService dallEService) {
        this.dallEService = dallEService;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        DALLE_Exchange dalleExchangeAnnotation = invocation.getMethod().getDeclaringClass().getAnnotation(DALLE_Exchange.class);

        if (dalleExchangeAnnotation != null) {
            log.info("Intercepting method for DALL-E request");

            ImageGenerationRequest request = new ImageGenerationRequest();
            configureRequest(request, dalleExchangeAnnotation);

            String methodName = invocation.getMethod().getName();
            return switch (methodName) {
                case "generateImage" -> dallEService.generateImage(request);
                case "editGenerateImage" -> dallEService.editGenerateImage(request);
                case "variationGenerateImage" -> dallEService.variationGenerateImage(request);
                default -> throw new IllegalArgumentException("Unsupported method: " + methodName);
            };
        }

        return invocation.proceed();
    }

    private void configureRequest(ImageGenerationRequest request, DALLE_Exchange annotation) {
        log.info("I'm configuring");
            request.setModel(annotation.model());
            request.setN(annotation.n());
            request.setSize(annotation.size());
            request.setResponseFormat(annotation.responseFormat());
            request.setStyle(annotation.style());
            request.setQuality(annotation.quality());
    }
}