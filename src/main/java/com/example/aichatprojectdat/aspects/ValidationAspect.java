package com.example.aichatprojectdat.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

import com.example.aichatprojectdat.aspects.AdviceExeptions.NullArgumentException;

@Aspect
public class ValidationAspect {

    @Pointcut("@annotation(ValidateParams)")
    public void validateParams() {}


    // Advice that runs before the methods matched by the pointcut
    @Before("validateParams()")
    public void beforeAdvice(JoinPoint joinPoint) {
        for (Object arg : joinPoint.getArgs()) {
            if (arg == null) {
                throw new NullArgumentException("Arguments cannot be null");
            }
        }
    }

}
