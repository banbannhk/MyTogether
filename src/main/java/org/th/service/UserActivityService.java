package org.th.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.th.entity.User;
import org.th.entity.UserActivity;
import org.th.entity.enums.ActivityType;
import org.th.repository.UserActivityRepository;
import org.th.repository.UserRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class UserActivityService {

    private final UserActivityRepository userActivityRepository;
    private final UserRepository userRepository;
    private final org.th.repository.ShopRepository shopRepository;
    private final DeviceTrackingService deviceTrackingService;

    /**
     * ASYNC: Log user activity without blocking the main thread
     */
    @Async("trackingExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logActivity(ActivityType type, String query, Long targetId, String targetName,
            Double lat, Double lon, String metadata, HttpServletRequest request) {
        try {
            User user = getCurrentUser();
            String deviceId = request.getHeader("X-Device-ID"); // Assuming client sends this
            String ipAddress = deviceTrackingService.getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");

            String osName = parseOsName(userAgent);
            String deviceType = parseDeviceType(userAgent);

            UserActivity.UserActivityBuilder builder = UserActivity.builder()
                    .user(user)
                    .deviceId(deviceId)
                    .ipAddress(ipAddress)
                    .osName(osName)
                    .deviceType(deviceType)
                    .activityType(type)
                    .searchQuery(query)
                    .targetId(targetId)
                    .targetName(targetName)
                    .latitude(lat)
                    .longitude(lon)
                    .metadata(metadata);

            // Link Shop if applicable
            if (targetId != null && (type == ActivityType.VIEW_SHOP || type == ActivityType.CLICK_DIRECTIONS)) {
                shopRepository.findById(targetId).ifPresent(builder::shop);
            }

            UserActivity activity = builder.build();

            userActivityRepository.save(activity);

            log.debug("Logged activity: {} by user: {}", type, user != null ? user.getUsername() : "guest");

        } catch (Exception e) {
            log.error("Failed to log user activity: {}", e.getMessage());
            // Don't rethrow - logging failure shouldn't fail the request
        }
    }

    private String parseOsName(String userAgent) {
        if (userAgent == null)
            return "Unknown";
        String ua = userAgent.toLowerCase();
        if (ua.contains("android"))
            return "Android";
        if (ua.contains("iphone") || ua.contains("ipad") || ua.contains("ios"))
            return "iOS";
        if (ua.contains("windows"))
            return "Windows";
        if (ua.contains("mac"))
            return "macOS";
        if (ua.contains("linux"))
            return "Linux";
        return "Other";
    }

    private String parseDeviceType(String userAgent) {
        if (userAgent == null)
            return "Unknown";
        String ua = userAgent.toLowerCase();
        if (ua.contains("mobile"))
            return "Mobile";
        if (ua.contains("tablet") || ua.contains("ipad"))
            return "Tablet";
        return "Desktop";
    }

    /**
     * Helper to get current authenticated user
     */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String username = auth.getName();
            return userRepository.findByUsername(username).orElse(null);
        }
        return null;
    }

    /**
     * Bind anonymous device history to a user account
     * Binds ALL previous anonymous activity from this device
     */
    @Transactional
    public void bindDeviceHistory(String deviceId, Long userId) {
        if (deviceId != null && userId != null) {
            userActivityRepository.bindDeviceActivityToUser(deviceId, userId);
            log.info("Bound all anonymous activity for device {} to user {}", deviceId, userId);
        }
    }
}
