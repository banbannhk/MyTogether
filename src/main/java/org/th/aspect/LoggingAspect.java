package org.th.aspect;

import lombok.extern.slf4j.Slf4j;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Parameter;
import java.util.LinkedHashMap;
import java.util.Map;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    /**
     * Pointcut that matches all repositories, services and web REST endpoints.
     */
    @Pointcut("within(@org.springframework.stereotype.Repository *)" +
            " || within(@org.springframework.stereotype.Service *)" +
            " || within(@org.springframework.web.bind.annotation.RestController *)")
    public void springBeanPointcut() {
        // Method is empty as this is just a Pointcut, the implementations are in the
        // advices.
    }

    /**
     * Pointcut that matches all beans in the application's main packages.
     */
    @Pointcut("execution(* org.th.controller..*(..))" +
            " || execution(* org.th.service..*(..))" +
            " || execution(* org.th.repository..*(..))")
    public void applicationPackagePointcut() {
        // Method is empty as this is just a Pointcut, the implementations are in the
        // advices.
    }

    /**
     * Pointcut that matches only controller methods.
     */
    @Pointcut("execution(* org.th.controller..*(..))")
    public void controllerPackagePointcut() {
        // Method is empty as this is just a Pointcut, the implementations are in the
        // advices.
    }

    /**
     * Advice that logs when a method is entered and exited, calculating execution
     * time.
     *
     * @param joinPoint join point for advice
     * @return result
     * @throws Throwable throws IllegalArgumentException
     */
    @Around("controllerPackagePointcut() && springBeanPointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        // Get HTTP request details if available (for controllers)
        String httpInfo = getHttpRequestInfo();

        // Get parameter names and values
        String paramsInfo = getParametersInfo(joinPoint);

        // Log method entry with HTTP info and parameters
        if (httpInfo != null) {
            log.info("Start: {}.{}() {} - Params: {}",
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(),
                    httpInfo,
                    paramsInfo);
        } else {
            log.info("Start: {}.{}() - Params: {}",
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(),
                    paramsInfo);
        }

        try {
            Object result = joinPoint.proceed();

            long executionTime = System.currentTimeMillis() - start;
            double executionTimeSeconds = executionTime / 1000.0;

            // Determine performance indicator
            String performanceIndicator = "";
            if (executionTime > 1000) {
                performanceIndicator = " - ⚠️ VERY SLOW - NEEDS OPTIMIZATION";
                log.warn("Finished: {}.{}() - Execution Time: {} ms ({} s){}",
                        joinPoint.getSignature().getDeclaringTypeName(),
                        joinPoint.getSignature().getName(),
                        executionTime,
                        String.format("%.2f", executionTimeSeconds),
                        performanceIndicator);
            } else if (executionTime > 300) {
                performanceIndicator = " - ⚠️ SLOW - Consider optimization";
                log.info("Finished: {}.{}() - Execution Time: {} ms ({} s){}",
                        joinPoint.getSignature().getDeclaringTypeName(),
                        joinPoint.getSignature().getName(),
                        executionTime,
                        String.format("%.2f", executionTimeSeconds),
                        performanceIndicator);
            } else {
                log.info("Finished: {}.{}() - Execution Time: {} ms ({} s)",
                        joinPoint.getSignature().getDeclaringTypeName(),
                        joinPoint.getSignature().getName(),
                        executionTime,
                        String.format("%.2f", executionTimeSeconds));
            }

            return result;
        } catch (IllegalArgumentException e) {
            log.error("Illegal argument: {} in {}.{}()", getParametersInfo(joinPoint),
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName());
            throw e;
        }
    }

    /**
     * Get HTTP request information (method and URI) if available.
     */
    private String getHttpRequestInfo() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return String.format("[%s %s]", request.getMethod(), request.getRequestURI());
            }
        } catch (Exception e) {
            // Not in a web request context
        }
        return null;
    }

    /**
     * Get parameter names and values as a formatted string.
     */
    private String getParametersInfo(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Parameter[] parameters = signature.getMethod().getParameters();
        Object[] args = joinPoint.getArgs();

        if (parameters.length == 0) {
            return "none";
        }

        Map<String, Object> paramMap = new LinkedHashMap<>();
        for (int i = 0; i < parameters.length; i++) {
            String paramName = parameters[i].getName();
            Object paramValue = i < args.length ? args[i] : null;

            // Skip framework objects that add noise to logs
            if (paramValue != null) {
                String className = paramValue.getClass().getName();
                if (className.startsWith("jakarta.servlet.") ||
                        className.startsWith("org.springframework.") ||
                        className.contains("HttpServletRequest") ||
                        className.contains("HttpServletResponse")) {
                    continue; // Skip this parameter
                }
            }

            paramMap.put(paramName, maskSensitiveValue(paramName, paramValue));
        }

        return paramMap.isEmpty() ? "none" : paramMap.toString();
    }

    /**
     * Mask sensitive parameter values.
     */
    private Object maskSensitiveValue(String paramName, Object value) {
        if (value == null) {
            return "null";
        }

        String lowerName = paramName.toLowerCase();
        String valueStr = value.toString();

        // Mask sensitive parameters
        if (lowerName.contains("password") ||
                lowerName.contains("token") ||
                lowerName.contains("secret") ||
                lowerName.contains("key") ||
                valueStr.toLowerCase().contains("password") ||
                valueStr.toLowerCase().contains("token")) {
            return "*****";
        }

        return value;
    }
}
