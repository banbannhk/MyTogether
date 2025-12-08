package org.th.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "districts", indexes = {
        @Index(name = "idx_district_city", columnList = "city_id"),
        @Index(name = "idx_district_slug", columnList = "slug"),
        @Index(name = "idx_district_active", columnList = "active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class District {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    @Column(name = "name_en", nullable = false, length = 100)
    private String nameEn;

    @Column(name = "name_mm", length = 100)
    private String nameMm;

    @Column(name = "name_th", length = 100)
    private String nameTh;

    @Column(nullable = false, length = 100)
    private String slug;

    @Column(precision = 10, scale = 8)
    private java.math.BigDecimal latitude;

    @Column(precision = 11, scale = 8)
    private java.math.BigDecimal longitude;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;
}
