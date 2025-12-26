package org.th.entity.shops;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "menu_sub_category_photos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuSubCategoryPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String url;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_sub_category_id", nullable = false)
    @ToString.Exclude
    private MenuSubCategory menuSubCategory;
}
