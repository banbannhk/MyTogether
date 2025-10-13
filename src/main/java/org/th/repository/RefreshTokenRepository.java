package org.th.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.th.entity.RefreshToken;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findByUserId(Long userId);

    List<RefreshToken> findByDeviceId(String deviceId);

    Optional<RefreshToken> findByUserIdAndDeviceId(Long userId, String deviceId);

    @Query("SELECT r FROM RefreshToken r WHERE r.userId = ?1 AND r.revoked = false AND r.expiresAt > ?2")
    List<RefreshToken> findActiveTokensByUserId(Long userId, LocalDateTime now);

    @Query("SELECT COUNT(r) FROM RefreshToken r WHERE r.userId = ?1 AND r.revoked = false")
    Long countActiveTokensByUserId(Long userId);

    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true, r.revokedAt = ?2 WHERE r.userId = ?1")
    void revokeAllUserTokens(Long userId, LocalDateTime revokedAt);

    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true, r.revokedAt = ?2 WHERE r.deviceId = ?1")
    void revokeAllDeviceTokens(String deviceId, LocalDateTime revokedAt);

    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.expiresAt < ?1")
    void deleteExpiredTokens(LocalDateTime now);
}