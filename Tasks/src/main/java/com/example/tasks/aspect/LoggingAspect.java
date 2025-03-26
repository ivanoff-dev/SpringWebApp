package com.example.tasks.aspect;

import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {
    @Before("execution(* com.example.tasks.service.TaskService.*(..))")
    public void logBefore() {
        System.out.println("Метод вызван");
    }

    @AfterReturning("execution(* com.example.tasks.service.TaskService.*(..))")
    public void logAfterReturning() {
        System.out.println("Метод успешно завершен");
    }

    @AfterThrowing("execution(* com.example.tasks.service.TaskService.*(..))")
    public void logAfterThrowing() {
        System.out.println("Произошла ошибка");
    }
}
