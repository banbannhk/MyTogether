package org.th.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.th.dto.ShopListDTO;
import org.th.entity.User;
import org.th.entity.shops.Favorite;
import org.th.entity.shops.Shop;
import org.th.repository.FavoriteRepository;
import org.th.repository.ShopRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private static final Logger logger = LoggerFactory.getLogger(FavoriteService.class);

    private final FavoriteRepository favoriteRepository;
    private final ShopRepository shopRepository;
    private final ShopService shopService;

    /**
     * Get user's favorite shops
     */
    public List<ShopListDTO> getUserFavorites(User user) {
        logger.info("Fetching favorites for user: {}", user.getUsername());

        // Use optimized query to fetch Favorites + Shop + Photos in one go
        List<Favorite> favorites = favoriteRepository.findByUserIdWithShopAndPhotos(user.getId());

        return favorites.stream()
                .map(favorite -> shopService.convertToListDTO(favorite.getShop()))
                .collect(Collectors.toList());
    }

    /**
     * Add shop to favorites
     */
    @Transactional
    public void addToFavorites(Long shopId, User user, String notes) {
        logger.info("Adding shop {} to favorites for user {}", shopId, user.getUsername());

        // Check if already favorited
        if (favoriteRepository.existsByUserIdAndShopId(user.getId(), shopId)) {
            throw new IllegalStateException("Shop already in favorites");
        }

        // Get shop
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new IllegalArgumentException("Shop not found"));

        // Create favorite
        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setShop(shop);
        favorite.setNotes(notes);

        favoriteRepository.save(favorite);
        logger.info("Shop {} added to favorites successfully", shopId);
    }

    /**
     * Remove shop from favorites
     */
    @Transactional
    public void removeFromFavorites(Long shopId, User user) {
        logger.info("Removing shop {} from favorites for user {}", shopId, user.getUsername());

        favoriteRepository.deleteByUserIdAndShopId(user.getId(), shopId);
        logger.info("Shop {} removed from favorites successfully", shopId);
    }

    /**
     * Check if shop is favorited by user
     */
    public boolean isFavorited(Long shopId, User user) {
        return favoriteRepository.existsByUserIdAndShopId(user.getId(), shopId);
    }

    /**
     * Update favorite notes
     */
    @Transactional
    public void updateNotes(Long shopId, User user, String notes, String notesMm) {
        logger.info("Updating favorite notes for shop {} by user {}", shopId, user.getUsername());

        Favorite favorite = favoriteRepository.findByUserIdAndShopId(user.getId(), shopId)
                .orElseThrow(() -> new IllegalArgumentException("Favorite not found"));

        favorite.setNotes(notes);
        favorite.setNotesMm(notesMm);
        favoriteRepository.save(favorite);
    }
}
