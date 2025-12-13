package org.th.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.th.entity.DeviceInfo;
import org.th.entity.RefreshToken;
import org.th.repository.RefreshTokenRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@lombok.extern.slf4j.Slf4j
public class RefreshTokenService {

    private static final int REFRESH_TOKEN_VALIDITY_DAYS = 30; // 30 days

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private DeviceTrackingService deviceTrackingService;

    /**
     * Create new refresh token test
     */
    public RefreshToken createRefreshToken(Long userId, String deviceId, HttpServletRequest request) {

        // Check if token already exists for this user+device
        Optional<RefreshToken> existing = refreshTokenRepository.findByUserIdAndDeviceId(userId, deviceId);

        if (existing.isPresent() && existing.get().isValid()) {
            // Reuse existing valid token
            RefreshToken token = existing.get();
            token.setLastUsedAt(LocalDateTime.now());
            return refreshTokenRepository.save(token);
        }

        // Create new token
        RefreshToken refreshToken = new RefreshToken();
        // refreshToken.setUserId(userId);
        refreshToken.setDeviceInfo(new DeviceInfo());
        refreshToken.setToken(generateTokenString());
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(REFRESH_TOKEN_VALIDITY_DAYS));
        refreshToken.setIpAddress(getClientIp(request));
        refreshToken.setUserAgent(request.getHeader("User-Agent"));

        refreshToken = refreshTokenRepository.save(refreshToken);
        log.info("✅ Created refresh token for user {} on device {}", userId, deviceId);

        return refreshToken;
    }

    /**
     * Verify and use refresh token
     */
    public RefreshToken verifyAndUseToken(String token) {
        Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByToken(token);

        if (refreshTokenOpt.isEmpty()) {
            throw new RuntimeException("Invalid refresh token");
        }

        RefreshToken refreshToken = refreshTokenOpt.get();

        if (!refreshToken.isValid()) {
            throw new RuntimeException("Refresh token is expired or revoked");
        }

        // Update last used
        refreshToken.setLastUsedAt(LocalDateTime.now());
        refreshToken = refreshTokenRepository.save(refreshToken);

        // Track token usage in device info
        // deviceTrackingService.trackTokenRefresh(refreshToken.getDeviceId(), token);

        log.info("✅ Refresh token used for user on device {}", refreshToken.getDeviceInfo().getDeviceId());

        return refreshToken;
    }

    /**
     * Revoke specific token
     */
    @Transactional
    public void revokeToken(String token) {
        Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByToken(token);

        if (refreshTokenOpt.isPresent()) {
            RefreshToken refreshToken = refreshTokenOpt.get();
            refreshToken.setRevoked(true);
            refreshToken.setRevokedAt(LocalDateTime.now());
            refreshTokenRepository.save(refreshToken);
            log.info("✅ Revoked token for user on device {}", refreshToken.getDeviceInfo().getDeviceId());
        }
    }

    /**
     * Generate secure token string
     */
    private String generateTokenString() {
        return UUID.randomUUID().toString() + "_" + System.currentTimeMillis();
    }

    /**
     * Get client IP
     */
    private String getClientIp(HttpServletRequest request) {
        String[] headers = { "X-Forwarded-For", "Proxy-Client-IP", "REMOTE_ADDR" };
        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }
}