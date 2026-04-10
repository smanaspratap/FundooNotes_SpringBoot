package com.fundoonotes.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * AOP Aspect for measuring service method execution time.
 *
 * Scoped to service.impl.* only for narrow, predictable behavior.
 * Logs INFO for fast methods (<500ms) and WARN for slow methods (≥500ms).
 */
@Slf4j
@Aspect
@Component
public class PerformanceAspect {

    private static final long SLOW_THRESHOLD_MS = 500;

    @Around("execution(* com.fundoonotes.service.impl.*.*(..))")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long elapsed = System.currentTimeMillis() - startTime;

        if (elapsed >= SLOW_THRESHOLD_MS) {
            log.warn("⚠ SLOW  {}.{}() took {}ms (threshold={}ms)",
                    className, methodName, elapsed, SLOW_THRESHOLD_MS);
        } else {
            log.info("⏱ PERF  {}.{}() completed in {}ms",
                    className, methodName, elapsed);
        }

        return result;
    }
}
