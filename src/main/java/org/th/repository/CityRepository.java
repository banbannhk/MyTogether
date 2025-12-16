package org.th.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.th.entity.City;

import java.util.List;
import java.util.Optional;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {
    Optional<City> findBySlug(String slug);

    Optional<City> findByNameEnIgnoreCase(String nameEn);

    List<City> findByActiveTrue();
}
