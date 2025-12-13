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
import org.th.repository.ReviewCommentRepository;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuItemReviewService {

    private final MenuItemReviewRepository menuItemReviewRepository;
    private final MenuItemRepository menuItemRepository;
    private final ReviewCommentRepository reviewCommentRepository;

    @Transactional
    public MenuItemReviewDTO addReview(Long menuItemId, org.th.dto.CreateMenuItemReviewRequest request, User user) {
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found"));

        if (menuItemReviewRepository.existsByUserIdAndMenuItemId(user.getId(), menuItemId)) {
            throw new IllegalStateException("You have already reviewed this item");
        }

        MenuItemReview review = new MenuItemReview();
        review.setMenuItem(menuItem);
        review.setUser(user);
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        // Process photos
        if (request.getPhotoUrls() != null) {
            for (String url : request.getPhotoUrls()) {
                org.th.entity.shops.MenuItemReviewPhoto photo = new org.th.entity.shops.MenuItemReviewPhoto();
                photo.setUrl(url);
                photo.setThumbnailUrl(url); // Can optimize later
                photo.setMenuItemReview(review);
                review.getPhotos().add(photo);
            }
        }

        MenuItemReview saved = menuItemReviewRepository.save(review);
        return convertToDTO(saved);
    }

    @Transactional
    public org.th.dto.ReviewCommentDTO addComment(Long reviewId, String content, User user) {
        MenuItemReview review = menuItemReviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        org.th.entity.shops.ReviewComment comment = new org.th.entity.shops.ReviewComment();
        comment.setContent(content);
        comment.setUser(user);
        comment.setMenuItemReview(review);

        org.th.entity.shops.ReviewComment saved = reviewCommentRepository.save(comment);
        return convertToCommentDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<MenuItemReviewDTO> getReviews(Long menuItemId) {
        return menuItemReviewRepository.findByMenuItemIdOrderByCreatedAtDesc(menuItemId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private MenuItemReviewDTO convertToDTO(MenuItemReview review) {
        // Convert photos
        java.util.List<org.th.dto.ReviewPhotoDTO> photos = review.getPhotos().stream()
                .map(p -> org.th.dto.ReviewPhotoDTO.builder()
                        .id(p.getId())
                        .url(p.getUrl())
                        .thumbnailUrl(p.getThumbnailUrl())
                        .build())
                .collect(Collectors.toList());

        // Convert comments (top-level only)
        java.util.List<org.th.dto.ReviewCommentDTO> comments = review.getComments().stream()
                .filter(c -> c.getParentComment() == null)
                .map(this::convertToCommentDTO)
                .collect(Collectors.toList());

        MenuItemReviewDTO dto = new MenuItemReviewDTO();
        dto.setId(review.getId());
        dto.setMenuItemId(review.getMenuItem().getId());
        dto.setMenuItemName(review.getMenuItem().getName());
        dto.setReviewerName(review.getUser().getFullName());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setPhotos(photos);
        dto.setComments(comments);
        return dto;
    }

    private org.th.dto.ReviewCommentDTO convertToCommentDTO(org.th.entity.shops.ReviewComment comment) {
        List<org.th.dto.ReviewCommentDTO> replies = comment.getReplies().stream()
                .map(this::convertToCommentDTO)
                .collect(Collectors.toList());

        return org.th.dto.ReviewCommentDTO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .userName(comment.getUser().getFullName())
                .createdAt(comment.getCreatedAt())
                .replies(replies)
                .build();
    }
}
