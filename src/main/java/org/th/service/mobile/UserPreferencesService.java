
package org.th.service.mobile;

import org.th.repository.*;
import org.th.entity.*;
import org.th.entity.shops.*;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.th.entity.User;
import org.th.entity.UserPreferences;
import org.th.repository.UserPreferencesRepository;
import org.th.repository.UserRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing user preferences and settings
 */
@Service
@lombok.extern.slf4j.Slf4j
public class UserPreferencesService {

    @Autowired
    private UserPreferencesRepository userPreferencesRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Get or create user preferences
     */
    @Transactional
    public UserPreferences getUserPreferences(Long userId) {
        log.info("Fetching preferences for user {}", userId);

        Optional<UserPreferences> existing = userPreferencesRepository.findByUserId(userId);
        if (existing.isPresent()) {
            return existing.get();
        }

        // Create default preferences if not exists
        log.info("Creating default preferences for user {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        UserPreferences preferences = UserPreferences.builder()
                .user(user)
                .preferredRadiusKm(5.0)
                .preferredLanguage("en")
                .receiveNotifications(true)
                .notificationDistanceKm(1.0)
                .notifyNewShops(true)
                .notifyFavoriteUpdates(true)
                .notifySpecialOffers(true)
                .build();

        return userPreferencesRepository.save(preferences);
    }

    /**
     * Update user preferences
     */
    @Transactional
    public UserPreferences updatePreferences(Long userId, UserPreferences updatedPreferences) {
        log.info("Updating preferences for user {}", userId);

        UserPreferences existing = getUserPreferences(userId);

        // Update fields
        if (updatedPreferences.getFavoriteCategories() != null) {
            existing.setFavoriteCategories(updatedPreferences.getFavoriteCategories());
        }
        if (updatedPreferences.getFavoriteCuisines() != null) {
            existing.setFavoriteCuisines(updatedPreferences.getFavoriteCuisines());
        }
        if (updatedPreferences.getDietaryRestrictions() != null) {
            existing.setDietaryRestrictions(updatedPreferences.getDietaryRestrictions());
        }
        if (updatedPreferences.getPreferredRadiusKm() != null) {
            existing.setPreferredRadiusKm(updatedPreferences.getPreferredRadiusKm());
        }
        if (updatedPreferences.getPreferredLanguage() != null) {
            existing.setPreferredLanguage(updatedPreferences.getPreferredLanguage());
        }
        if (updatedPreferences.getPriceRangeMin() != null) {
            existing.setPriceRangeMin(updatedPreferences.getPriceRangeMin());
        }
        if (updatedPreferences.getPriceRangeMax() != null) {
            existing.setPriceRangeMax(updatedPreferences.getPriceRangeMax());
        }

        // Update notification settings
        if (updatedPreferences.getReceiveNotifications() != null) {
            existing.setReceiveNotifications(updatedPreferences.getReceiveNotifications());
        }
        if (updatedPreferences.getNotificationDistanceKm() != null) {
            existing.setNotificationDistanceKm(updatedPreferences.getNotificationDistanceKm());
        }
        if (updatedPreferences.getNotifyNewShops() != null) {
            existing.setNotifyNewShops(updatedPreferences.getNotifyNewShops());
        }
        if (updatedPreferences.getNotifyFavoriteUpdates() != null) {
            existing.setNotifyFavoriteUpdates(updatedPreferences.getNotifyFavoriteUpdates());
        }
        if (updatedPreferences.getNotifySpecialOffers() != null) {
            existing.setNotifySpecialOffers(updatedPreferences.getNotifySpecialOffers());
        }

        UserPreferences saved = userPreferencesRepository.save(existing);
        log.info("Successfully updated preferences for user {}", userId);
        return saved;
    }

    /**
     * Add favorite category
     */
    @Transactional
    public UserPreferences addFavoriteCategory(Long userId, String category) {
        log.info("Adding category {} to user {}'s favorites", category, userId);

        UserPreferences prefs = getUserPreferences(userId);
        List<String> categories = getFavoriteCategoriesAsList(prefs);

        if (!categories.contains(category)) {
            categories.add(category);
            prefs.setFavoriteCategories(String.join(",", categories));
            return userPreferencesRepository.save(prefs);
        }

        return prefs;
    }

    /**
     * Remove favorite category
     */
    @Transactional
    public UserPreferences removeFavoriteCategory(Long userId, String category) {
        log.info("Removing category {} from user {}'s favorites", category, userId);

        UserPreferences prefs = getUserPreferences(userId);
        List<String> categories = getFavoriteCategoriesAsList(prefs);

        categories.remove(category);
        prefs.setFavoriteCategories(String.join(",", categories));
        return userPreferencesRepository.save(prefs);
    }

    /**
     * Get favorite categories as list
     */
    public List<String> getFavoriteCategoriesAsList(UserPreferences prefs) {
        if (prefs.getFavoriteCategories() == null || prefs.getFavoriteCategories().isEmpty()) {
            return Arrays.asList();
        }
        return Arrays.stream(prefs.getFavoriteCategories().split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }

    /**
     * Get favorite cuisines as list
     */
    public List<String> getFavoriteCuisinesAsList(UserPreferences prefs) {
        if (prefs.getFavoriteCuisines() == null || prefs.getFavoriteCuisines().isEmpty()) {
            return Arrays.asList();
        }
        return Arrays.stream(prefs.getFavoriteCuisines().split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }

    /**
     * Get dietary restrictions as list
     */
    public List<String> getDietaryRestrictionsAsList(UserPreferences prefs) {
        if (prefs.getDietaryRestrictions() == null || prefs.getDietaryRestrictions().isEmpty()) {
            return Arrays.asList();
        }
        return Arrays.stream(prefs.getDietaryRestrictions().split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }
}
