package org.th.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cities", indexes = {
        @Index(name = "idx_city_slug", columnList = "slug"),
        @Index(name = "idx_city_active", columnList = "active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name_en", nullable = false, length = 100)
    private String nameEn;

    @Column(name = "name_mm", length = 100)
    private String nameMm;

    @Column(name = "name_th", length = 100)
    private String nameTh;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;
}
