package org.th.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.th.entity.shops.MenuSubCategory;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuSubCategoryRepository extends JpaRepository<MenuSubCategory, Long> {
    List<MenuSubCategory> findByMenuCategoryId(Long menuCategoryId);

    List<MenuSubCategory> findByMenuCategoryIdAndIsActiveTrueOrderByDisplayOrderAsc(Long menuCategoryId);

    Optional<MenuSubCategory> findBySlug(String slug);
}
