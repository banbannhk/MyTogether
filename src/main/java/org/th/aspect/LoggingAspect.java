package org.th.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Arrays;

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
     * Advice that logs when a method is entered.
     *
     * @param joinPoint join point for advice
     */
    /**
     * Advice that logs when a method is entered.
     *
     * @param joinPoint join point for advice
     */
    @Before("applicationPackagePointcut() && springBeanPointcut()")
    public void logBefore(JoinPoint joinPoint) {
        log.info("START :: {} : args : {}", joinPoint.getSignature().getName(), getMaskedArgs(joinPoint.getArgs()));
    }

    /**
     * Advice that logs when a method exits successfully.
     *
     * @param joinPoint join point for advice
     * @param result    the result returned by the method
     */
    @AfterReturning(pointcut = "applicationPackagePointcut() && springBeanPointcut()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        log.info("END :: {} : args : {}", joinPoint.getSignature().getName(), getMaskedArgs(joinPoint.getArgs()));
    }

    /**
     * Helper method to mask sensitive arguments.
     *
     * @param args Array of arguments
     * @return String representation of arguments with sensitive data masked
     */
    private String getMaskedArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }

        return Arrays.toString(Arrays.stream(args).map(arg -> {
            if (arg == null)
                return "null";
            String str = arg.toString();
            // Simple heuristic to detect sensitive data (can be improved with
            // reflection/annotations)
            if (str.toLowerCase().contains("password") ||
                    str.toLowerCase().contains("token") ||
                    str.toLowerCase().contains("secret") ||
                    str.toLowerCase().contains("key")) {
                return "*****";
            }
            return arg;
        }).toArray());
    }
}
