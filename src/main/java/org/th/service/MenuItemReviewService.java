package org.th.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.th.dto.MenuItemReviewDTO;
import org.th.entity.User;
import org.th.entity.shops.MenuItem;
import org.th.entity.shops.MenuItemReview;
import org.th.repository.MenuItemRepository;
import org.th.repository.MenuItemReviewRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuItemReviewService {

    private final MenuItemReviewRepository menuItemReviewRepository;
    private final MenuItemRepository menuItemRepository;

    @Transactional
    public MenuItemReviewDTO addReview(Long menuItemId, Integer rating, String comment, User user) {
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found"));

        if (menuItemReviewRepository.existsByUserIdAndMenuItemId(user.getId(), menuItemId)) {
            throw new IllegalStateException("You have already reviewed this item");
        }

        MenuItemReview review = new MenuItemReview();
        review.setMenuItem(menuItem);
        review.setUser(user);
        review.setRating(rating);
        review.setComment(comment);

        MenuItemReview saved = menuItemReviewRepository.save(review);
        return convertToDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<MenuItemReviewDTO> getReviews(Long menuItemId) {
        return menuItemReviewRepository.findByMenuItemIdOrderByCreatedAtDesc(menuItemId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private MenuItemReviewDTO convertToDTO(MenuItemReview review) {
        MenuItemReviewDTO dto = new MenuItemReviewDTO();
        dto.setId(review.getId());
        dto.setMenuItemId(review.getMenuItem().getId());
        dto.setMenuItemName(review.getMenuItem().getName());
        dto.setReviewerName(review.getUser().getFullName());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());
        return dto;
    }
}
