package org.th.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Cache Configuration for Performance Optimization
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();

        cacheManager.setCaches(Arrays.asList(
                // Shop searches - cache for 5 minutes
                buildCache("shopSearch", 1000, 5, TimeUnit.MINUTES),

                // Nearby shops - cache for 2 minutes (geospatial queries are expensive)
                buildCache("nearbyShops", 500, 2, TimeUnit.MINUTES),

                // Shop details - cache for 10 minutes
                buildCache("shopDetails", 1000, 10, TimeUnit.MINUTES),

                // Reviews - cache for 5 minutes
                buildCache("shopReviews", 500, 5, TimeUnit.MINUTES),

                // Trending shops - cache for 5 minutes (frequently accessed)
                buildCache("trendingShops", 100, 5, TimeUnit.MINUTES),

                // Home Feed (Page 0) - cache for 2 minutes (bypass network latency)
                buildCache("homeShops", 10, 2, TimeUnit.MINUTES),

                // Shops by category - cache for 15 minutes
                buildCache("shopsByCategory", 500, 15, TimeUnit.MINUTES),

                // Shops by township - cache for 15 minutes
                buildCache("shopsByTownship", 500, 15, TimeUnit.MINUTES),

                // Time context - cache for 15 minutes
                buildCache("timeContext", 10, 15, TimeUnit.MINUTES),

                // User segments - cache for 1 hour
                buildCache("userSegment", 1000, 1, TimeUnit.HOURS)));

        return cacheManager;
    }

    private Cache buildCache(String name, int maxSize, long duration, TimeUnit unit) {
        return new CaffeineCache(name, Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(duration, unit)
                .recordStats()
                .build());
    }
}
