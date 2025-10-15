package org.th.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Manages per-device locks to prevent race conditions
 * Each device gets its own lock, allowing parallel processing of different devices
 */
@Component
@Slf4j
public class DeviceLockManager {

    private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    /**
     * Get or create a lock for the specified device
     * @param deviceId unique device identifier
     * @return ReentrantLock for this device
     */
    public ReentrantLock getLock(String deviceId) {
        return locks.computeIfAbsent(deviceId, k -> {
            log.info("🔧 Creating new lock for device: {}", deviceId);
            return new ReentrantLock();
        });
    }

    /**
     * Get total number of device locks currently tracked
     * Useful for monitoring/debugging
     */
    public int getLockCount() {
        return locks.size();
    }

    /**
     * Remove lock for a device (optional cleanup)
     * Call this when a device is permanently deleted
     */
    public void removeLock(String deviceId) {
        ReentrantLock removed = locks.remove(deviceId);
        if (removed != null) {
            log.debug("🗑️ Removed lock for device: {}", deviceId);
        }
    }

    /**
     * Clear all locks (use with caution, mainly for testing)
     */
    public void clearAllLocks() {
        locks.clear();
        log.warn("⚠️ All device locks cleared");
    }
}