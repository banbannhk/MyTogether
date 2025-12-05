package org.th.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.th.entity.UserLocation;
import org.th.entity.enums.LocationType;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserLocationRepository extends JpaRepository<UserLocation, Long> {

    /**
     * Find all locations for a user
     */
    @Query("SELECT ul FROM UserLocation ul " +
           "WHERE ul.user.id = :userId " +
           "ORDER BY ul.visitCount DESC, ul.lastVisitedAt DESC")
    List<UserLocation> findByUserId(@Param("userId") Long userId);

    /**
     * Find location by user and type
     */
    @Query("SELECT ul FROM UserLocation ul " +
           "WHERE ul.user.id = :userId AND ul.locationType = :locationType")
    Optional<UserLocation> findByUserIdAndLocationType(@Param("userId") Long userId,
                                                         @Param("locationType") LocationType locationType);

    /**
     * Find user's home location
     */
    @Query("SELECT ul FROM UserLocation ul " +
           "WHERE ul.user.id = :userId AND ul.locationType = 'HOME'")
    Optional<UserLocation> findHomeLocation(@Param("userId") Long userId);

    /**
     * Find user's work location
     */
    @Query("SELECT ul FROM UserLocation ul " +
           "WHERE ul.user.id = :userId AND ul.locationType = 'WORK'")
    Optional<UserLocation> findWorkLocation(@Param("userId") Long userId);

    /**
     * Find user's most frequent locations
     */
    @Query("SELECT ul FROM UserLocation ul " +
           "WHERE ul.user.id = :userId " +
           "ORDER BY ul.visitCount DESC, ul.lastVisitedAt DESC")
    List<UserLocation> findMostFrequentLocations(@Param("userId") Long userId);
}