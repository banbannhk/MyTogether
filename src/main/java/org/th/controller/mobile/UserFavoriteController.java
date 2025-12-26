package org.th.controller.mobile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.th.dto.ApiResponse;
import org.th.dto.ShopListDTO;
import org.th.entity.User;
import org.th.entity.UserFavorite;
import org.th.repository.UserFavoriteRepository;
import org.th.repository.UserRepository;
import org.th.service.mobile.FavoriteService;
import org.th.service.ShopService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/mobile/user/favorites")
@RequiredArgsConstructor
@Tag(name = "User Favorites", description = "Manage user favorite shops")
public class UserFavoriteController {

    private final UserFavoriteRepository userFavoriteRepository;
    private final UserRepository userRepository;
    private final ShopService shopService;
    private final FavoriteService favoriteService;

    @GetMapping
    @Operation(summary = "Get user favorites", description = "Get list of favorite shops for current user")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<ShopListDTO>>> getFavorites(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon) {

        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthorized"));
        }

        List<UserFavorite> favorites = userFavoriteRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        List<ShopListDTO> shopDTOs = favorites.stream()
                .map(fav -> shopService.convertToListDTO(fav.getShop(), lat, lon))
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Favorites retrieved", shopDTOs));
    }

    @PostMapping("/{shopId}")
    @Operation(summary = "Add favorite", description = "Add a shop to user favorites")
    public ResponseEntity<ApiResponse<Object>> addFavorite(@PathVariable Long shopId) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthorized"));
        }

        favoriteService.addToFavorites(shopId, user, null);
        return ResponseEntity.ok(ApiResponse.success("Shop added to favorites", null));
    }

    @DeleteMapping("/{shopId}")
    @Operation(summary = "Remove favorite", description = "Remove a shop from user favorites")
    public ResponseEntity<ApiResponse<Void>> removeFavorite(@PathVariable Long shopId) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthorized"));
        }

        favoriteService.removeFromFavorites(shopId, user);
        return ResponseEntity.ok(ApiResponse.success("Shop removed from favorites", null));
    }

    @PostMapping("/menu/{menuItemId}")
    @Operation(summary = "Add menu favorite", description = "Add a menu item to user favorites")
    public ResponseEntity<ApiResponse<Object>> addMenuFavorite(@PathVariable Long menuItemId) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthorized"));
        }

        favoriteService.addToMenuFavorites(menuItemId, user, null);
        return ResponseEntity.ok(ApiResponse.success("Menu item added to favorites", null));
    }

    @DeleteMapping("/menu/{menuItemId}")
    @Operation(summary = "Remove menu favorite", description = "Remove a menu item from user favorites")
    public ResponseEntity<ApiResponse<Void>> removeMenuFavorite(@PathVariable Long menuItemId) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthorized"));
        }

        favoriteService.removeFromMenuFavorites(menuItemId, user);
        return ResponseEntity.ok(ApiResponse.success("Menu item removed from favorites", null));
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return userRepository.findByUsername(auth.getName()).orElse(null);
    }
}
