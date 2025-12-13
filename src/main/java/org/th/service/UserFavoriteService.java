package org.th.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.th.entity.User;
import org.th.entity.UserFavorite;
import org.th.entity.shops.Shop;
import org.th.repository.ShopRepository;
import org.th.repository.UserFavoriteRepository;
import org.th.repository.UserRepository;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing user favorites/bookmarks
 */
@Service
@Transactional
@lombok.extern.slf4j.Slf4j
public class UserFavoriteService {

    @Autowired
    private UserFavoriteRepository userFavoriteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShopRepository shopRepository;

    /**
     * Add shop to user's favorites
     */
    @Transactional
    public UserFavorite addFavorite(Long userId, Long shopId, String notes) {
        log.info("User {} adding shop {} to favorites", userId, shopId);

        // Check if already favorited
        if (userFavoriteRepository.existsByUserIdAndShopId(userId, shopId)) {
            log.warn("Shop {} already in user {}'s favorites", shopId, userId);
            throw new IllegalStateException("Shop is already in favorites");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new IllegalArgumentException("Shop not found: " + shopId));

        UserFavorite favorite = UserFavorite.builder()
                .user(user)
                .shop(shop)
                .notes(notes)
                .build();

        UserFavorite saved = userFavoriteRepository.save(favorite);
        log.info("Successfully added shop {} to user {}'s favorites", shopId, userId);
        return saved;
    }

    /**
     * Remove shop from user's favorites
     */
    @Transactional
    public void removeFavorite(Long userId, Long shopId) {
        log.info("User {} removing shop {} from favorites", userId, shopId);
        userFavoriteRepository.deleteByUserIdAndShopId(userId, shopId);
        log.info("Successfully removed shop {} from user {}'s favorites", shopId, userId);
    }

    /**
     * Get all favorites for a user
     */
    @Transactional(readOnly = true)
    public List<UserFavorite> getUserFavorites(Long userId) {
        log.info("Fetching all favorites for user {}", userId);
        return userFavoriteRepository.findByUserIdWithShop(userId);
    }

    /**
     * Get favorites by category
     */
    @Transactional(readOnly = true)
    public List<UserFavorite> getUserFavoritesByCategory(Long userId, String category) {
        log.info("Fetching favorites for user {} in category {}", userId, category);
        return userFavoriteRepository.findByUserIdAndCategory(userId, category);
    }

    /**
     * Check if shop is in user's favorites
     */
    @Transactional(readOnly = true)
    public boolean isFavorite(Long userId, Long shopId) {
        return userFavoriteRepository.existsByUserIdAndShopId(userId, shopId);
    }

    /**
     * Get total favorites count for user
     */
    @Transactional(readOnly = true)
    public Long getFavoritesCount(Long userId) {
        return userFavoriteRepository.countByUserId(userId);
    }

    /**
     * Update favorite notes
     */
    @Transactional
    public UserFavorite updateFavoriteNotes(Long userId, Long shopId, String notes) {
        log.info("Updating notes for user {}'s favorite shop {}", userId, shopId);

        UserFavorite favorite = userFavoriteRepository.findByUserIdAndShopId(userId, shopId)
                .orElseThrow(() -> new IllegalArgumentException("Favorite not found"));

        favorite.setNotes(notes);
        return userFavoriteRepository.save(favorite);
    }
}
