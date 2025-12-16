package org.th.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.th.entity.shops.MenuItem;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByCategoryId(Long categoryId);

    /**
     * Find menu items by category ID (Paginated)
     */
    org.springframework.data.domain.Page<MenuItem> findByCategoryId(Long categoryId,
            org.springframework.data.domain.Pageable pageable);

    /**
     * Find menu items by shop ID (Paginated)
     */
    org.springframework.data.domain.Page<MenuItem> findByShopId(Long shopId,
            org.springframework.data.domain.Pageable pageable);
}
