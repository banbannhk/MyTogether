package org.th.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.th.dto.UserEngagementDTO;
import org.th.entity.User;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameOrEmail(String username, String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%'))")
    org.springframework.data.domain.Page<User> searchUsers(@Param("query") String query,
            org.springframework.data.domain.Pageable pageable);

    /**
     * Get consolidated user engagement metrics (optimized - 1 query instead of 4)
     * Combines activity, favorite, and review counts in single query
     */
    @Query("SELECT new org.th.dto.UserEngagementDTO(" +
            "COUNT(DISTINCT ua.id), " +
            "COUNT(DISTINCT f.id), " +
            "COUNT(DISTINCT r.id), " +
            "SUM(CASE WHEN ua.createdAt > :since THEN 1 ELSE 0 END)) " +
            "FROM User u " +
            "LEFT JOIN UserActivity ua ON ua.user.id = u.id " +
            "LEFT JOIN Favorite f ON f.user.id = u.id " +
            "LEFT JOIN ShopReview r ON r.user.id = u.id " +
            "WHERE u.id = :userId " +
            "GROUP BY u.id")
    UserEngagementDTO getUserEngagement(@Param("userId") Long userId, @Param("since") LocalDateTime since);
}