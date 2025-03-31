package com.example.tasks.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.hibernate.mapping.Join;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {
    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Before("@annotation(com.example.tasks.aspect.annotaion.LogExecution)")
    public void logBefore(JoinPoint joinPoint) {
        logger.info("Вызван метод " + joinPoint.getSignature().getName());
    }

    @AfterThrowing("@annotation(com.example.tasks.aspect.annotaion.LogException)")
    public void logAfterThrowing(JoinPoint joinPoint) {
        logger.info("Произошла ошибка в методе " + joinPoint.getSignature().getName());
    }

    @AfterReturning("@annotation(com.example.tasks.aspect.annotaion.LogExecution)")
    public void logAfterReturning(JoinPoint joinPoint) {
        logger.info("Завершен метод " + joinPoint.getSignature().getName());
    }

    @Around("@annotation(com.example.tasks.aspect.annotaion.LogTracking)")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        logger.info("Начинается выполнение метода " + joinPoint.getSignature().getName());

        Object result = joinPoint.proceed();

        long endTime = System.currentTimeMillis() - startTime;
        logger.info("Завершено выполнение метода " + joinPoint.getSignature().getName() + " за {} ms", endTime);

        return result;
    }
}
