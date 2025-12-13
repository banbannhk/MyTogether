
// File: src/main/java/org/th/service/DeviceTrackingService.java
package org.th.service;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;
import org.th.entity.DeviceInfo;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Service
@lombok.extern.slf4j.Slf4j
public class DeviceTrackingService {

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
}