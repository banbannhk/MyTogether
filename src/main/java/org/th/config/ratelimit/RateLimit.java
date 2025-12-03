package org.th.config.ratelimit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * Annotation for rate limiting endpoints
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * Rate limit tier for different operation types
     */
    Tier tier() default Tier.MODERATE;

    /**
     * Custom capacity (overrides tier if specified)
     */
    long capacity() default -1;

    /**
     * Custom refill rate (tokens per period)
     */
    long refillTokens() default -1;

    /**
     * Refill period duration
     */
    long refillDuration() default -1;

    /**
     * Time unit for refill duration
     */
    TimeUnit refillUnit() default TimeUnit.MINUTES;

    /**
     * Apply per user (true) or per IP (false)
     */
    boolean perUser() default true;

    /**
     * Rate limit tiers based on operation intensity
     */
    enum Tier {
        /**
         * Authentication endpoints - Very strict (5 req/min)
         * Example: Login, Register
         */
        AUTH(5, 5, 1, TimeUnit.MINUTES),

        /**
         * CPU-intensive operations - Conservative (30 req/min, 500/hour)
         * Example: Geospatial search, complex queries
         */
        CPU_INTENSIVE(30, 500, 1, TimeUnit.HOURS),

        /**
         * Write operations - Strict (10 req/hour)
         * Example: Create review, add favorite
         */
        WRITE(10, 10, 1, TimeUnit.HOURS),

        /**
         * IO-intensive reads - Moderate (100 req/min, 2000/hour)
         * Example: Get shop details, get favorites
         */
        IO_INTENSIVE(100, 2000, 1, TimeUnit.HOURS),

        /**
         * Simple reads - Permissive (200 req/min, 5000/hour)
         * Example: Get list, simple queries
         */
        MODERATE(200, 5000, 1, TimeUnit.HOURS),

        /**
         * Public endpoints - Very permissive (500 req/min)
         * Example: Browse shops, view content
         */
        PUBLIC(500, 10000, 1, TimeUnit.HOURS);

        private final long capacity;
        private final long refillTokens;
        private final long refillDuration;
        private final TimeUnit refillUnit;

        Tier(long capacity, long refillTokens, long refillDuration, TimeUnit refillUnit) {
            this.capacity = capacity;
            this.refillTokens = refillTokens;
            this.refillDuration = refillDuration;
            this.refillUnit = refillUnit;
        }

        public long getCapacity() {
            return capacity;
        }

        public long getRefillTokens() {
            return refillTokens;
        }

        public long getRefillDuration() {
            return refillDuration;
        }

        public TimeUnit getRefillUnit() {
            return refillUnit;
        }
    }
}
