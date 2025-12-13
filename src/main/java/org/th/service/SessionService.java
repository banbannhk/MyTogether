package org.th.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.th.dto.SessionDTO;
import org.th.dto.analytics.SessionAnalyticsDTO;
import org.th.entity.DeviceInfo;
import org.th.entity.User;
import org.th.entity.UserSession;
import org.th.entity.enums.SessionEntryPoint;
import org.th.repository.UserSessionRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing user sessions
 */
@Service
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class SessionService {

    private final UserSessionRepository sessionRepository;

    /**
     * Start a new session
     */
    @Transactional
    public UserSession startSession(User user, DeviceInfo deviceInfo, SessionEntryPoint entryPoint) {
        String sessionId = UUID.randomUUID().toString();

        UserSession session = UserSession.builder()
                .sessionId(sessionId)
                .user(user)
                .deviceInfo(deviceInfo)
                .sessionStart(LocalDateTime.now())
                .entryPoint(entryPoint != null ? entryPoint : SessionEntryPoint.DIRECT)
                .activityCount(0)
                .shopsViewed(0)
                .searchesPerformed(0)
                .isActive(true)
                .build();

        session = sessionRepository.save(session);
        log.info("Started session {} for user {}", sessionId, user != null ? user.getUsername() : "guest");
        return session;
    }

    /**
     * End a session
     */
    @Transactional
    public void endSession(String sessionId, String exitPoint) {
        Optional<UserSession> sessionOpt = sessionRepository.findBySessionId(sessionId);
        if (sessionOpt.isPresent()) {
            UserSession session = sessionOpt.get();
            session.setSessionEnd(LocalDateTime.now());
            session.setExitPoint(exitPoint);
            session.setIsActive(false);

            // Calculate duration
            if (session.getSessionStart() != null && session.getSessionEnd() != null) {
                long seconds = ChronoUnit.SECONDS.between(session.getSessionStart(), session.getSessionEnd());
                session.setDurationSeconds((int) seconds);
            }

            sessionRepository.save(session);
            log.info("Ended session {}, duration: {}s", sessionId, session.getDurationSeconds());
        }
    }

    /**
     * Update session activity counters (async to avoid blocking)
     */
    @Async("trackingExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateSessionActivity(String sessionId, String activityType) {
        try {
            Optional<UserSession> sessionOpt = sessionRepository.findBySessionId(sessionId);
            if (sessionOpt.isPresent()) {
                UserSession session = sessionOpt.get();

                // Increment activity count
                session.setActivityCount(session.getActivityCount() + 1);

                // Increment specific counters based on activity type
                if (activityType != null) {
                    if (activityType.contains("VIEW_SHOP")) {
                        session.setShopsViewed(session.getShopsViewed() + 1);
                    } else if (activityType.contains("SEARCH")) {
                        session.setSearchesPerformed(session.getSearchesPerformed() + 1);
                    }
                }

                sessionRepository.save(session);
                log.debug("Updated session {} activity count: {}", sessionId, session.getActivityCount());
            }
        } catch (Exception e) {
            log.error("Failed to update session activity: {}", e.getMessage());
        }
    }

    /**
     * Get active session for user and device
     */
    @Transactional(readOnly = true)
    public Optional<UserSession> getActiveSession(Long userId, Long deviceId) {
        return sessionRepository.findActiveSessionByUserIdAndDeviceId(userId, deviceId);
    }

    /**
     * Get session by ID
     */
    @Transactional(readOnly = true)
    public Optional<UserSession> getSessionById(String sessionId) {
        return sessionRepository.findBySessionId(sessionId);
    }

    /**
     * Get session analytics for a date range
     */
    @Transactional(readOnly = true)
    public SessionAnalyticsDTO getSessionAnalytics(LocalDateTime start, LocalDateTime end) {
        List<UserSession> sessions = sessionRepository.findBySessionStartBetween(start, end);

        // Calculate statistics
        long totalSessions = sessions.size();
        double avgDuration = sessions.stream()
                .filter(s -> !s.getIsActive() && s.getDurationSeconds() != null)
                .mapToInt(UserSession::getDurationSeconds)
                .average()
                .orElse(0.0);

        double avgActivities = sessions.stream()
                .mapToInt(UserSession::getActivityCount)
                .average()
                .orElse(0.0);

        double avgShopsViewed = sessions.stream()
                .mapToInt(UserSession::getShopsViewed)
                .average()
                .orElse(0.0);

        double avgSearches = sessions.stream()
                .mapToInt(UserSession::getSearchesPerformed)
                .average()
                .orElse(0.0);

        // Top entry points
        Map<SessionEntryPoint, Long> entryPoints = sessions.stream()
                .filter(s -> s.getEntryPoint() != null)
                .collect(Collectors.groupingBy(UserSession::getEntryPoint, Collectors.counting()));

        // Top exit points
        Map<String, Long> exitPoints = sessions.stream()
                .filter(s -> s.getExitPoint() != null && !s.getIsActive())
                .collect(Collectors.groupingBy(UserSession::getExitPoint, Collectors.counting()));

        // Active sessions count
        long activeSessions = sessions.stream()
                .filter(UserSession::getIsActive)
                .count();

        return SessionAnalyticsDTO.builder()
                .totalSessions(totalSessions)
                .averageDurationSeconds(avgDuration)
                .averageActivitiesPerSession(avgActivities)
                .averageShopsViewed(avgShopsViewed)
                .averageSearches(avgSearches)
                .topEntryPoints(entryPoints)
                .topExitPoints(exitPoints)
                .activeSessions(activeSessions)
                .build();
    }

    /**
     * Convert entity to DTO
     */
    public SessionDTO toDTO(UserSession session) {
        return SessionDTO.builder()
                .sessionId(session.getSessionId())
                .userId(session.getUser() != null ? session.getUser().getId() : null)
                .deviceId(session.getDeviceInfo() != null ? session.getDeviceInfo().getId() : null)
                .sessionStart(session.getSessionStart())
                .sessionEnd(session.getSessionEnd())
                .durationSeconds(session.getDurationSeconds())
                .activityCount(session.getActivityCount())
                .shopsViewed(session.getShopsViewed())
                .searchesPerformed(session.getSearchesPerformed())
                .entryPoint(session.getEntryPoint())
                .exitPoint(session.getExitPoint())
                .isActive(session.getIsActive())
                .build();
    }
}
