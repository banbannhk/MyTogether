package org.th.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.th.entity.UserSession;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    /**
     * Find session by session ID
     */
    Optional<UserSession> findBySessionId(String sessionId);

    /**
     * Find active session for user and device
     */
    @Query("SELECT s FROM UserSession s WHERE s.user.id = :userId " +
            "AND s.deviceInfo.id = :deviceId AND s.isActive = true")
    Optional<UserSession> findActiveSessionByUserIdAndDeviceId(
            @Param("userId") Long userId,
            @Param("deviceId") Long deviceId);

    /**
     * Find sessions by user within date range
     */
    @Query("SELECT s FROM UserSession s WHERE s.user.id = :userId " +
            "AND s.sessionStart BETWEEN :start AND :end " +
            "ORDER BY s.sessionStart DESC")
    List<UserSession> findByUserIdAndSessionStartBetween(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /**
     * Count active sessions for user
     */
    long countByUserIdAndIsActiveTrue(Long userId);

    /**
     * Get average session duration (completed sessions only)
     */
    @Query("SELECT AVG(s.durationSeconds) FROM UserSession s " +
            "WHERE s.isActive = false AND s.durationSeconds IS NOT NULL")
    Double getAverageSessionDuration();

    /**
     * Get average activities per session
     */
    @Query("SELECT AVG(s.activityCount) FROM UserSession s WHERE s.isActive = false")
    Double getAverageActivitiesPerSession();

    /**
     * Get sessions within date range
     */
    @Query("SELECT s FROM UserSession s WHERE s.sessionStart BETWEEN :start AND :end " +
            "ORDER BY s.sessionStart DESC")
    List<UserSession> findBySessionStartBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /**
     * Count total sessions in date range
     */
    long countBySessionStartBetween(LocalDateTime start, LocalDateTime end);
}
