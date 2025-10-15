package org.th.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    private static final int MAX_STRING_LENGTH = 700;
    private static final int MAX_COLLECTION_SIZE = 20;

    @Around("execution(* org.th.service..*(..)) || execution(* org.th.controller..*(..)) || execution(* org.th.repository..*(..))")
    public Object logAllMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Logger log = LoggerFactory.getLogger(joinPoint.getTarget().getClass());

        // Performance optimization: skip if logging not enabled
        if (!log.isInfoEnabled()) {
            return joinPoint.proceed();
        }

        Method method = signature.getMethod();
        String methodName = method.getName();

        // Exclude specific methods
        if (shouldSkipLogging(methodName)) {
            return joinPoint.proceed(); // Skip logging, just execute
        }

        // Build strings ONCE
        String methodSignature = buildMethodSignature(method);
        String paramValues = buildParameterValues(method, joinPoint.getArgs());

        // Log START
        log.info("START :: {}({}) : {}", methodName, methodSignature, paramValues);

        long startTime = System.currentTimeMillis();

        try {
            return joinPoint.proceed();
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            log.info("END :: {}({}) : {} [Execution time: {} ms]",
                    methodName, methodSignature, paramValues, executionTime);
        }
    }

    /**
     * Check if method should skip logging
     */
    private boolean shouldSkipLogging(String methodName) {
        // Add methods you want to exclude
        return methodName.equals("getClientIpAddress")
                || methodName.equals("toString")
                || methodName.equals("hashCode")
                || methodName.equals("equals");
    }

    /**
     * Build method signature: "UpdateUserDetailsRequest request, String token"
     */
    private String buildMethodSignature(Method method) {
        Parameter[] parameters = method.getParameters();
        if (parameters == null || parameters.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parameters.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(parameters[i].getType().getSimpleName())
                    .append(" ")
                    .append(parameters[i].getName());
        }

        return sb.toString();
    }

    /**
     * Build parameter values with NULL checking and type handling
     */
    private String buildParameterValues(Method method, Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        }

        Parameter[] parameters = method.getParameters();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }

            Object arg = args[i];

            // NULL CHECK
            if (arg == null) {
                sb.append("null");
                continue;
            }

            String paramName = parameters[i].getName();

            // Mask sensitive parameters
            if (isSensitiveParameter(paramName)) {
                sb.append("***MASKED***");
                continue;
            }

            // Handle different types
            sb.append(formatValue(arg));
        }

        return sb.toString();
    }

    /**
     * Format value based on its type
     * Handles: primitives, String, List, Set, Map, Arrays, Custom Objects
     */
    private String formatValue(Object value) {
        if (value == null) {
            return "null";
        }

        try {
            // Primitives and wrapper types
            if (isPrimitiveOrWrapper(value)) {
                return String.valueOf(value);
            }

            // String
            if (value instanceof String) {
                String str = (String) value;
                return str.length() > MAX_STRING_LENGTH
                        ? str.substring(0, MAX_STRING_LENGTH) + "..."
                        : str;
            }

            // List
            if (value instanceof List) {
                List<?> list = (List<?>) value;
                return formatCollection(list, "List");
            }

            // Set
            if (value instanceof Set) {
                Set<?> set = (Set<?>) value;
                return formatCollection(set, "Set");
            }

            // Map
            if (value instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) value;
                return formatMap(map);
            }

            // Array
            if (value.getClass().isArray()) {
                return formatArray(value);
            }

            // Custom objects (uses toString())
            String str = value.toString();
            return str.length() > MAX_STRING_LENGTH
                    ? str.substring(0, MAX_STRING_LENGTH) + "..."
                    : str;

        } catch (Exception e) {
            // Fallback if toString() fails
            return value.getClass().getSimpleName() + "@" + System.identityHashCode(value);
        }
    }

    /**
     * Check if value is primitive or wrapper type
     */
    private boolean isPrimitiveOrWrapper(Object value) {
        return value instanceof Number
                || value instanceof Boolean
                || value instanceof Character;
    }

    /**
     * Format Collection (List, Set)
     */
    private String formatCollection(Collection<?> collection, String type) {
        if (collection == null) {
            return "null";
        }

        if (collection.isEmpty()) {
            return type + "[]";
        }

        StringBuilder sb = new StringBuilder(type).append("[");
        int count = 0;

        for (Object item : collection) {
            if (count > 0) {
                sb.append(", ");
            }

            if (count >= MAX_COLLECTION_SIZE) {
                sb.append("... (").append(collection.size()).append(" total items)");
                break;
            }

            sb.append(formatValue(item));
            count++;
        }

        sb.append("]");
        return sb.toString();
    }

    /**
     * Format Map
     */
    private String formatMap(Map<?, ?> map) {
        if (map == null) {
            return "null";
        }

        if (map.isEmpty()) {
            return "Map{}";
        }

        StringBuilder sb = new StringBuilder("Map{");
        int count = 0;

        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (count > 0) {
                sb.append(", ");
            }

            if (count >= MAX_COLLECTION_SIZE) {
                sb.append("... (").append(map.size()).append(" total entries)");
                break;
            }

            sb.append(formatValue(entry.getKey()))
                    .append("=")
                    .append(formatValue(entry.getValue()));
            count++;
        }

        sb.append("}");
        return sb.toString();
    }

    /**
     * Format Array
     */
    private String formatArray(Object array) {
        if (array == null) {
            return "null";
        }

        // Primitive arrays
        if (array instanceof int[]) {
            return Arrays.toString((int[]) array);
        } else if (array instanceof long[]) {
            return Arrays.toString((long[]) array);
        } else if (array instanceof double[]) {
            return Arrays.toString((double[]) array);
        } else if (array instanceof boolean[]) {
            return Arrays.toString((boolean[]) array);
        } else if (array instanceof byte[]) {
            return "byte[" + ((byte[]) array).length + "]";
        } else if (array instanceof char[]) {
            return Arrays.toString((char[]) array);
        } else if (array instanceof float[]) {
            return Arrays.toString((float[]) array);
        } else if (array instanceof short[]) {
            return Arrays.toString((short[]) array);
        }

        // Object array
        Object[] objArray = (Object[]) array;
        if (objArray.length == 0) {
            return "[]";
        }

        if (objArray.length > MAX_COLLECTION_SIZE) {
            return Arrays.toString(Arrays.copyOf(objArray, MAX_COLLECTION_SIZE))
                    + "... (" + objArray.length + " total items)";
        }

        return Arrays.toString(objArray);
    }

    /**
     * Check if parameter is sensitive
     */
    private boolean isSensitiveParameter(String paramName) {
        if (paramName == null) {
            return false;
        }

        String lower = paramName.toLowerCase();
        return lower.contains("password")
                || lower.contains("token")
                || lower.contains("secret")
                || lower.contains("apikey")
                || lower.contains("credential")
                || lower.contains("auth");
    }
}