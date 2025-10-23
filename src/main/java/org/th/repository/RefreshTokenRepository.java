package org.th.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.th.entity.RefreshToken;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // Find by token
    Optional<RefreshToken> findByToken(String token);

    // Find by user ID
    List<RefreshToken> findByUserId(Long userId);

    // Find by device ID
    List<RefreshToken> findByDeviceId(String deviceId);

    // Find by user AND device
    Optional<RefreshToken> findByUserIdAndDeviceId(Long userId, String deviceId);

    // Find active tokens by user
    @Query("SELECT r FROM RefreshToken r WHERE r.userId = :userId AND r.revoked = false AND r.expiresAt > :now")
    List<RefreshToken> findActiveTokensByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    // Find active tokens by device
    @Query("SELECT r FROM RefreshToken r WHERE r.deviceId = :deviceId AND r.revoked = false AND r.expiresAt > :now")
    List<RefreshToken> findActiveTokensByDeviceId(@Param("deviceId") String deviceId, @Param("now") LocalDateTime now);

    // Count active tokens by user
    @Query("SELECT COUNT(r) FROM RefreshToken r WHERE r.userId = :userId AND r.revoked = false AND r.expiresAt > :now")
    Long countActiveTokensByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    // Count active tokens by device
    @Query("SELECT COUNT(r) FROM RefreshToken r WHERE r.deviceId = :deviceId AND r.revoked = false")
    Long countActiveTokensByDeviceId(@Param("deviceId") String deviceId);

    // Revoke all user tokens
    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true, r.revokedAt = :revokedAt WHERE r.userId = :userId")
    void revokeAllUserTokens(@Param("userId") Long userId, @Param("revokedAt") LocalDateTime revokedAt);

    // Revoke all device tokens
    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true, r.revokedAt = :revokedAt WHERE r.deviceId = :deviceId")
    void revokeAllDeviceTokens(@Param("deviceId") String deviceId, @Param("revokedAt") LocalDateTime revokedAt);

    // Revoke specific token
    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true, r.revokedAt = :revokedAt WHERE r.token = :token")
    void revokeToken(@Param("token") String token, @Param("revokedAt") LocalDateTime revokedAt);

    // Delete expired tokens (cleanup)
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

    // Delete revoked tokens older than X days
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.revoked = true AND r.revokedAt < :cutoffDate")
    void deleteRevokedTokensOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
}