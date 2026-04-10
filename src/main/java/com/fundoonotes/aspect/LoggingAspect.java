package com.fundoonotes.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * AOP Aspect for logging service method entry and exit.
 *
 * Scoped to service.impl.* only for narrow, predictable behavior.
 * SECURITY: Never logs parameter values that could contain passwords/tokens.
 * Only logs method name and parameter types on entry, result type on exit.
 */
@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Around("execution(* com.fundoonotes.service.impl.*.*(..))")
    public Object logServiceMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        // Log entry with parameter types only (no values for security)
        String paramTypes = Arrays.stream(joinPoint.getArgs())
                .map(arg -> arg != null ? arg.getClass().getSimpleName() : "null")
                .collect(Collectors.joining(", "));

        log.debug("▸ ENTER {}.{}({})", className, methodName, paramTypes);

        Object result = joinPoint.proceed();

        // Log exit with result type only
        String resultType = result != null ? result.getClass().getSimpleName() : "void";
        log.debug("◂ EXIT  {}.{}() → {}", className, methodName, resultType);

        return result;
    }
}
