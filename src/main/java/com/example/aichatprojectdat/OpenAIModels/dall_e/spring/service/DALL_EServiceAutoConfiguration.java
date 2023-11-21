package com.example.aichatprojectdat.OpenAIModels.dall_e.spring.service;

import com.example.aichatprojectdat.OpenAIModels.custom_interface.CustomOpenAI_DALL_E_API;
import com.example.aichatprojectdat.OpenAIModels.dall_e.model.generation.ImageGenerationMessage;
import com.example.aichatprojectdat.OpenAIModels.dall_e.model.generation.ImageGenerationRequest;
import com.example.aichatprojectdat.OpenAIModels.dall_e.model.generation.ImageGenerationResponse;
import com.example.aichatprojectdat.OpenAIModels.dall_e.spring.client.DALL_EServiceProxyFactory;
import com.example.aichatprojectdat.OpenAIModels.dall_e.spring.service.interfaces.IDALL_EService;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mvnsearch.chatgpt.spring.ChatGPTProperties;
import org.mvnsearch.chatgpt.spring.client.ChatGPTServiceProxyFactory;
import org.mvnsearch.chatgpt.spring.service.ChatGPTService;
import org.mvnsearch.chatgpt.spring.service.PromptManager;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;


@RegisterReflectionForBinding({ ImageGenerationMessage.class, ImageGenerationResponse.class, ImageGenerationRequest.class})
@Configuration
@NoArgsConstructor
@Slf4j
public class DALL_EServiceAutoConfiguration {

    @Bean
    IDALL_EService dallEService(CustomOpenAI_DALL_E_API openAIDallEApi) {
        return new DALL_EServiceImpl(openAIDallEApi);
    }

    @Bean
    DALL_EServiceProxyFactory dallEServiceProxyFactory(IDALL_EService idallEService) {
        return new DALL_EServiceProxyFactory(idallEService);
    }

    @Bean
    CustomOpenAI_DALL_E_API customOpenAIDallEApi(ChatGPTProperties properties) {
        String openaiApiKey = properties.api().key();
        String openaiApiUrl = StringUtils.hasText(properties.api().url()) ? properties.api().url() : "https://api.openai.com/v1";

        WebClient client;
            client = WebClient.builder().defaultHeader("Authorization", new String[]{"Bearer " + openaiApiKey}).baseUrl(openaiApiUrl).build();

        return (CustomOpenAI_DALL_E_API) HttpServiceProxyFactory.builder().clientAdapter(WebClientAdapter.forClient(client)).build().createClient(CustomOpenAI_DALL_E_API.class);
    }

}
