// File: src/main/java/org/th/service/DeviceTrackingService.java
package org.th.service;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.th.entity.DeviceInfo;
import org.th.entity.RouteUsage;
import org.th.repository.DeviceInfoRepository;
import org.th.repository.RouteUsageRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class DeviceTrackingService {

    private static final Logger logger = LoggerFactory.getLogger(DeviceTrackingService.class);

    @Autowired
    private DeviceInfoRepository deviceInfoRepository;

    @Autowired
    private RouteUsageRepository routeUsageRepository;

    @Autowired
    private HttpServletRequest request;

    public CompletableFuture<String> trackDeviceAndRouteAsync(
            String deviceId, String origin, String destination,
            String routeType, String transitMode,
            Double distanceKm, Integer durationMinutes,
            Double fareAmount, String ipAddress, String userAgent) {

        try {
            logger.debug("🔄 [ASYNC] Starting background tracking for device: {}", deviceId);

            // VALIDATE: deviceId is REQUIRED
            if (deviceId == null || deviceId.isEmpty()) {
                throw new IllegalArgumentException("deviceId is required");
            }

            trackDeviceInfoAndRoute(deviceId, origin, destination,
                    routeType, transitMode,
                    distanceKm, durationMinutes,
                    fareAmount, ipAddress, userAgent);

            return CompletableFuture.completedFuture(deviceId);

        } catch (Exception e) {
            logger.error("❌ [ASYNC] Error tracking device and route: {}", e.getMessage());
            return CompletableFuture.completedFuture(deviceId != null ? deviceId : "unknown");
        }
    }

    /**
     * ASYNC: Track device and route in background thread
     * Returns immediately with deviceId
     */
    @Async("trackingExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void trackDeviceInfoAndRoute(String deviceId, String origin, String destination, String routeType, String transitMode, Double distanceKm, Integer durationMinutes, Double fareAmount, String ipAddress, String userAgent) {
        // Get lock for this specific device
        ReentrantLock lock = deviceLockManager.getLock(deviceId);

        // Use try-with-resources pattern with LockResource
        try (LockResource lockResource = new LockResource(lock, deviceId, LOCK_TIMEOUT_SECONDS)) {

            if (!lockResource.isAcquired()) {
                logger.warn("⚠️ Could not acquire lock for device {} within {} seconds",
                        deviceId, LOCK_TIMEOUT_SECONDS);
                throw new RuntimeException("Could not acquire lock for device tracking");
            }

            logger.info("🔒 [LOCKED] Thread {} acquired lock for device {}",
                    Thread.currentThread().getName(), deviceId);

            // ===== CRITICAL SECTION (Protected by lock) =====

            // 1. Save/Update device info
            DeviceInfo deviceInfo = trackOrUpdateDevice(deviceId);

            // 2. Save route usage
            RouteUsage routeUsage = new RouteUsage();
            routeUsage.setDeviceId(deviceInfo.getDeviceId());
            routeUsage.setOrigin(origin);
            routeUsage.setDestination(destination);
            routeUsage.setRouteType(routeType);
            routeUsage.setTransitMode(transitMode);
            routeUsage.setDistanceKm(distanceKm);
            routeUsage.setDurationMinutes(durationMinutes);
            routeUsage.setFareAmount(fareAmount);
            routeUsage.setIpAddress(ipAddress);

            routeUsageRepository.save(routeUsage);

            logger.info("✅ [TRANSACTION] Saved device {} and route {} -> {}",
                    deviceInfo.getDeviceId(), origin, destination);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("❌ Thread interrupted while waiting for lock: {}", deviceId, e);
            throw new RuntimeException("Thread interrupted during device tracking", e);

        } catch (Exception e) {
            logger.error("❌ Error saving device and route for device {}: {}",
                    deviceId, e.getMessage(), e);
            throw new RuntimeException("Failed to save device and route", e);
        }
        // Lock is automatically released here by LockResource.close()
    }

    /**
     * SYNC: Track device (use when you need immediate response with deviceId)
     */
    public DeviceInfo trackOrUpdateDevice(String deviceId) {
        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");

        return trackOrUpdateDeviceInternal(deviceId, ipAddress, userAgent);
    }

    /**
     * Internal method to track/update device
     */
    private DeviceInfo trackOrUpdateDeviceInternal(String deviceId, String ipAddress, String userAgent) {

        // Check if device exists
        Optional<DeviceInfo> existingDevice = deviceInfoRepository.findByDeviceId(deviceId);

        DeviceInfo deviceInfo;
        if (existingDevice.isPresent()) {
            // Update existing device
            deviceInfo = existingDevice.get();
            deviceInfo.setLastSeen(LocalDateTime.now());
        } else {
            // Create new device
            deviceInfo = new DeviceInfo();
            deviceInfo.setDeviceId(deviceId);
        }

        deviceInfo.setUserAgent(userAgent);
        deviceInfo.setIpAddress(ipAddress);

        // Parse user agent
        parseUserAgent(userAgent, deviceInfo);

        return deviceInfoRepository.save(deviceInfo);
    }

    /**
     * Get client IP address
     */
    public String getClientIpAddress(HttpServletRequest request) {
        String[] headers = {
                "X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR", "HTTP_CLIENT_IP", "REMOTE_ADDR"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }

        return request.getRemoteAddr();
    }

    /**
     * Parse User-Agent
     */
    private void parseUserAgent(String userAgent, DeviceInfo deviceInfo) {
        if (userAgent == null || userAgent.isEmpty()) {
            return;
        }

        String ua = userAgent.toLowerCase();

        // Device Type
        if (ua.contains("mobile") || ua.contains("android") || ua.contains("iphone")) {
            deviceInfo.setDeviceType("MOBILE");
        } else if (ua.contains("tablet") || ua.contains("ipad")) {
            deviceInfo.setDeviceType("TABLET");
        } else {
            deviceInfo.setDeviceType("WEB");
        }

        // OS
        if (ua.contains("android")) {
            deviceInfo.setOsName("Android");
        } else if (ua.contains("iphone") || ua.contains("ipad")) {
            deviceInfo.setOsName("iOS");
        } else if (ua.contains("windows")) {
            deviceInfo.setOsName("Windows");
        } else if (ua.contains("mac")) {
            deviceInfo.setOsName("macOS");
        } else if (ua.contains("linux")) {
            deviceInfo.setOsName("Linux");
        }

        // Browser
        if (ua.contains("chrome") && !ua.contains("edg")) {
            deviceInfo.setBrowserName("Chrome");
        } else if (ua.contains("safari") && !ua.contains("chrome")) {
            deviceInfo.setBrowserName("Safari");
        } else if (ua.contains("firefox")) {
            deviceInfo.setBrowserName("Firefox");
        } else if (ua.contains("edg")) {
            deviceInfo.setBrowserName("Edge");
        }
    }

    /**
     * ASYNC: Track token refresh
     */
    @Async("trackingExecutor")
    public void trackTokenRefreshAsync(String deviceId, String refreshToken) {
        try {
            Optional<DeviceInfo> deviceOpt = deviceInfoRepository.findByDeviceId(deviceId);

            if (deviceOpt.isPresent()) {
                DeviceInfo deviceInfo = deviceOpt.get();
                deviceInfo.setLastSeen(LocalDateTime.now());
                deviceInfoRepository.save(deviceInfo);

                logger.info("✅ [ASYNC] Token refresh tracked for device {}", deviceId);
            }
        } catch (Exception e) {
            logger.error("❌ [ASYNC] Error tracking token refresh: {}", e.getMessage());
        }
    }

    /**
     * Link device to user
     */
    @Transactional
    public void linkDeviceToUser(String deviceId, Long userId) {
        Optional<DeviceInfo> deviceOpt = deviceInfoRepository.findByDeviceId(deviceId);

        if (deviceOpt.isPresent()) {
            DeviceInfo deviceInfo = deviceOpt.get();
            //deviceInfo.setCurrentUserId(userId);
            deviceInfoRepository.save(deviceInfo);
            logger.info("✅ Device {} linked to user {}", deviceId, userId);
        }
    }

    /**
     * AutoCloseable wrapper for ReentrantLock
     * Ensures lock is ALWAYS released, even if exception occurs
     */
    private static class LockResource implements AutoCloseable {

        private final ReentrantLock lock;
        private final String deviceId;
        private boolean acquired = false;

        public LockResource(ReentrantLock lock, String deviceId, int timeoutSeconds)
                throws InterruptedException {
            this.lock = lock;
            this.deviceId = deviceId;

            // Try to acquire lock with timeout
            this.acquired = lock.tryLock(timeoutSeconds, TimeUnit.SECONDS);
        }

        public boolean isAcquired() {
            return acquired;
        }

        @Override
        public void close() {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
                logger.info("🔓 [UNLOCKED] Thread {} released lock for device {}",
                                Thread.currentThread().getName(), deviceId);
            }
        }
    }
}