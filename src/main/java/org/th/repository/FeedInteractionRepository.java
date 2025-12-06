package org.th.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.th.entity.FeedInteraction;
import org.th.entity.enums.FeedInteractionAction;
import org.th.entity.enums.FeedSectionType;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FeedInteractionRepository extends JpaRepository<FeedInteraction, Long> {

    /**
     * Find interactions by section type within date range
     */
    @Query("SELECT f FROM FeedInteraction f WHERE f.sectionType = :sectionType " +
            "AND f.timestamp BETWEEN :start AND :end")
    List<FeedInteraction> findBySectionTypeAndTimestampBetween(
            @Param("sectionType") FeedSectionType sectionType,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /**
     * Count interactions by action and section type
     */
    long countByActionAndSectionType(FeedInteractionAction action, FeedSectionType sectionType);

    /**
     * Count interactions by section type within date range
     */
    @Query("SELECT COUNT(f) FROM FeedInteraction f WHERE f.sectionType = :sectionType " +
            "AND f.timestamp BETWEEN :start AND :end")
    long countBySectionTypeAndTimestampBetween(
            @Param("sectionType") FeedSectionType sectionType,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /**
     * Get click-through rate by section type
     */
    @Query("SELECT f.sectionType, " +
            "COUNT(CASE WHEN f.action = 'VIEWED' THEN 1 END) as views, " +
            "COUNT(CASE WHEN f.action = 'CLICKED' THEN 1 END) as clicks " +
            "FROM FeedInteraction f " +
            "WHERE f.timestamp BETWEEN :start AND :end " +
            "GROUP BY f.sectionType")
    List<Object[]> getClickThroughRateBySectionType(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /**
     * Get most clicked shops by section
     */
    @Query("SELECT f.shopId, f.shopName, COUNT(f) as clickCount " +
            "FROM FeedInteraction f " +
            "WHERE f.sectionType = :sectionType AND f.action = 'CLICKED' " +
            "AND f.timestamp BETWEEN :start AND :end " +
            "GROUP BY f.shopId, f.shopName " +
            "ORDER BY clickCount DESC")
    List<Object[]> getMostClickedShopsBySection(
            @Param("sectionType") FeedSectionType sectionType,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /**
     * Get average click position by section
     */
    @Query("SELECT AVG(f.position) FROM FeedInteraction f " +
            "WHERE f.sectionType = :sectionType AND f.action = 'CLICKED' " +
            "AND f.timestamp BETWEEN :start AND :end")
    Double getAverageClickPositionBySection(
            @Param("sectionType") FeedSectionType sectionType,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /**
     * Find interactions by session
     */
    List<FeedInteraction> findBySessionIdOrderByTimestampAsc(String sessionId);
}
