package com.example.aichatprojectdat.OpenAIModels.dall_e.spring.client;

import com.example.aichatprojectdat.OpenAIModels.dall_e.spring.service.interfaces.IDALL_EService;

import org.springframework.aop.framework.ProxyFactory;

public class DALL_EServiceProxyFactory {
    private final IDALL_EService dallEService;

    public DALL_EServiceProxyFactory(IDALL_EService dallEService) {
        this.dallEService = dallEService;
    }

    @SuppressWarnings("unchecked")
    public <T> T createClient(Class<T> clazz) {
        return ProxyFactory.getProxy(clazz, new DALLEExchangeMethodInterceptor(this.dallEService));
    }
}
