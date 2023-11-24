package com.example.aichatprojectdat.open_ai_models.dall_e.spring.client;

import com.example.aichatprojectdat.open_ai_models.custom_interface.DALLE_Exchange;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aot.generate.GenerationContext;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.ProxyHints;
import org.springframework.aot.hint.ReflectionHints;
import org.springframework.beans.factory.aot.BeanRegistrationAotContribution;
import org.springframework.beans.factory.aot.BeanRegistrationAotProcessor;
import org.springframework.beans.factory.aot.BeanRegistrationCode;
import org.springframework.beans.factory.support.RegisteredBean;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
public class DALLEExchangeBeanRegistrationAotProcessor implements BeanRegistrationAotProcessor  {

    private final static MergedAnnotations.Search SEARCH = MergedAnnotations.search(MergedAnnotations.SearchStrategy.TYPE_HIERARCHY);


    @Override
    @Nullable
    public BeanRegistrationAotContribution processAheadOfTime(RegisteredBean registeredBean) {
        Class<?> beanClass = registeredBean.getBeanClass();
        List<Class<?>> dalleExchangeInterfaces = new ArrayList<>();
        log.info("I'm running beanregistration");

        // Find all interfaces for the bean class that are annotated with DALLE_Exchange
        Class<?>[] interfaces = ClassUtils.getAllInterfacesForClass(beanClass);
        if (log.isDebugEnabled()) {
            log.debug("DALLE_Exchange interfaces: {}", interfaces);
        }

        for (Class<?> interfaceClass : interfaces) {
            ReflectionUtils.doWithMethods(interfaceClass, method -> {
                if (!dalleExchangeInterfaces.contains(interfaceClass)
                        && SEARCH.from(method).isPresent(DALLE_Exchange.class)) {
                    dalleExchangeInterfaces.add(interfaceClass);
                    if (log.isDebugEnabled()) {
                        log.debug("Adding {} to the collection of DALLE_Exchange interfaces", interfaceClass.getName());
                    }
                }
            });
        }

        // Create AOT contribution if DALLE_Exchange interfaces are found
        return !dalleExchangeInterfaces.isEmpty() ?
                new DalleExchangeBeanRegistrationAotContribution(dalleExchangeInterfaces) : null;
    }
    private static class DalleExchangeBeanRegistrationAotContribution implements BeanRegistrationAotContribution {
        private final List<Class<?>> dalleExchangeInterfaces;

        DalleExchangeBeanRegistrationAotContribution(List<Class<?>> dalleExchangeInterfaces) {
            this.dalleExchangeInterfaces = dalleExchangeInterfaces;
        }

        @Override
        public void applyTo(GenerationContext generationContext, BeanRegistrationCode beanRegistrationCode) {
            ProxyHints proxyHints = generationContext.getRuntimeHints().proxies();
            ReflectionHints reflectionHints = generationContext.getRuntimeHints().reflection();

            // Register proxy and reflection hints for each DALLE_Exchange interface
            for (Class<?> exchangeInterface : this.dalleExchangeInterfaces) {
                proxyHints.registerJdkProxy(AopProxyUtils.completeJdkProxyInterfaces(exchangeInterface));
                ReflectionUtils.doWithMethods(exchangeInterface, method -> {
                    if (SEARCH.from(method).isPresent(DALLE_Exchange.class)) {
                        Stream.of(method.getParameterTypes())
                                .forEach(c -> reflectionHints.registerType(c, MemberCategory.values()));
                    }
                });
            }
        }
    }
}