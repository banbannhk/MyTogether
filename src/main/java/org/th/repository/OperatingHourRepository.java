package org.th.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.th.entity.shops.OperatingHour;

import java.util.List;

@Repository
public interface OperatingHourRepository extends JpaRepository<OperatingHour, Long> {
    List<OperatingHour> findByShopId(Long shopId);
}
