package org.th.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.th.entity.enums.LocationType;

import java.time.LocalDateTime;

/**
 * Entity for tracking user's frequent locations (home, work, etc.)
 */
@Entity
@Table(name = "user_locations",
    indexes = {
        @Index(name = "idx_location_user", columnList = "user_id"),
        @Index(name = "idx_location_type", columnList = "location_type")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "location_name", length = 255)
    private String locationName; // "Home", "Work", or geocoded address

    @Enumerated(EnumType.STRING)
    @Column(name = "location_type", length = 50)
    private LocationType locationType; // HOME, WORK, FREQUENT, OTHER

    @Column(name = "visit_count")
    private Integer visitCount = 1;

    @Column(name = "last_visited_at")
    private LocalDateTime lastVisitedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastVisitedAt = LocalDateTime.now();
    }
}