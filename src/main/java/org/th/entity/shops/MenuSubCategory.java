package org.th.entity.shops;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "menu_sub_categories", indexes = {
        @Index(name = "idx_menu_subcategory_slug", columnList = "slug"),
        @Index(name = "idx_menu_subcategory_category", columnList = "menu_category_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuSubCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "name_mm", length = 100)
    private String nameMm;

    @Column(name = "name_en", length = 100)
    private String nameEn;

    @Column(unique = true, nullable = false, length = 100)
    private String slug;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_category_id", nullable = false)
    @ToString.Exclude
    private MenuCategory menuCategory;

    @ToString.Exclude
    @OneToMany(mappedBy = "menuSubCategory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MenuSubCategoryPhoto> photos = new ArrayList<>();
}
