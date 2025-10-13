// File: src/main/java/org/th/repository/DeviceInfoRepository.java
package org.th.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.th.entity.DeviceInfo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceInfoRepository extends JpaRepository<DeviceInfo, Long> {

    Optional<DeviceInfo> findByDeviceId(String deviceId);

    List<DeviceInfo> findByDeviceType(String deviceType);

    List<DeviceInfo> findByIsActiveTrue();

    @Query("SELECT d FROM DeviceInfo d WHERE d.lastSeen >= ?1")
    List<DeviceInfo> findActiveDevicesSince(LocalDateTime since);

    @Query("SELECT d.deviceType, COUNT(d) FROM DeviceInfo d GROUP BY d.deviceType")
    List<Object[]> countByDeviceType();

    @Query("SELECT d.osName, COUNT(d) FROM DeviceInfo d GROUP BY d.osName")
    List<Object[]> countByOsName();
}