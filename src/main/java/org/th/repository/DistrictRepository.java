package org.th.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.th.entity.District;

import java.util.List;
import java.util.Optional;

@Repository
public interface DistrictRepository extends JpaRepository<District, Long> {
    List<District> findByCityIdAndActiveTrue(Long cityId);

    List<District> findByCitySlugAndActiveTrue(String citySlug);

    Optional<District> findBySlug(String slug);

    Optional<District> findByCityIdAndNameEnIgnoreCase(Long cityId, String nameEn);
}
