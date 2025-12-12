package org.th.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.th.service.ShopService;

@Component
public class CacheWarmup {

    private static final Logger logger = LoggerFactory.getLogger(CacheWarmup.class);

    @Autowired
    private ShopService shopService;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        logger.info("Initializing Cache Warm-up...");
        long start = System.currentTimeMillis();

        try {
            // Warm up the 'homeShops' cache (Page 0)
            // This triggers the DB fetch and populates the cache
            shopService.getAllShops(PageRequest.of(0, 20), null, null);

            long duration = System.currentTimeMillis() - start;
            logger.info("Cache Warm-up completed in {} ms. Home feed is now ready.", duration);
        } catch (Exception e) {
            logger.warn("Cache Warm-up failed: {}", e.getMessage());
            // Do not rethrow, let the app start even if warmup fails
        }
    }
}
