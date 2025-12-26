package org.th.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.th.entity.shops.MenuSubCategoryPhoto;

@Repository
public interface MenuSubCategoryPhotoRepository extends JpaRepository<MenuSubCategoryPhoto, Long> {
}
