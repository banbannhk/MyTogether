package org.th.config.ratelimit;

import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Interceptor for rate limiting using Bucket4j
 */
@Component
@lombok.extern.slf4j.Slf4j
public class RateLimitInterceptor implements HandlerInterceptor {

    // Cache for buckets per user/IP
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RateLimit rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);

        if (rateLimit == null) {
            return true; // No rate limit annotation
        }

        String key = resolveKey(request, rateLimit.perUser());
        Bucket bucket = resolveBucket(key, rateLimit);

        if (bucket.tryConsume(1)) {
            long remaining = bucket.getAvailableTokens();
            response.addHeader("X-RateLimit-Remaining", String.valueOf(remaining));
            return true;
        }

        // Rate limit exceeded
        log.warn("Rate limit exceeded for key: {}", key);
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");
        response.getWriter().write(String.format(
                "{\"success\":false,\"message\":\"Rate limit exceeded\",\"error\":\"Too many requests. Please try again later.\"}"));

        return false;
    }

    /**
     * Resolve bucket key (user ID or IP address)
     */
    private String resolveKey(HttpServletRequest request, boolean perUser) {
        if (perUser) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                return "user:" + auth.getName();
            }
        }
        // Fallback to IP address for anonymous users or IP-based limits
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return "ip:" + ip;
    }

    /**
     * Resolve or create bucket for the given key
     */
    private Bucket resolveBucket(String key, RateLimit rateLimit) {
        return buckets.computeIfAbsent(key, k -> createBucket(rateLimit));
    }

    /**
     * Create a new bucket with the specified rate limit configuration
     */
    private Bucket createBucket(RateLimit rateLimit) {
        long capacity;
        long refillTokens;
        Duration refillDuration;

        // Use custom values if specified, otherwise use tier defaults
        if (rateLimit.capacity() > 0) {
            capacity = rateLimit.capacity();
            refillTokens = rateLimit.refillTokens() > 0 ? rateLimit.refillTokens() : rateLimit.capacity();
            refillDuration = Duration.of(rateLimit.refillDuration(),
                    rateLimit.refillUnit().toChronoUnit());
        } else {
            RateLimit.Tier tier = rateLimit.tier();
            capacity = tier.getCapacity();
            refillTokens = tier.getRefillTokens();
            refillDuration = Duration.of(tier.getRefillDuration(),
                    tier.getRefillUnit().toChronoUnit());
        }

        // Use new builder pattern (non-deprecated)
        return Bucket.builder()
                .addLimit(limit -> limit.capacity(capacity)
                        .refillIntervally(refillTokens, refillDuration))
                .build();
    }
}
