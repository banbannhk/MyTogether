package org.th.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.th.dto.MenuCategoryDTO;
import org.th.dto.MenuItemDTO;
import org.th.dto.PhotoDTO;
import org.th.entity.shops.MenuItem;
import org.th.repository.MenuCategoryRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuCategoryService {

    private final MenuCategoryRepository menuCategoryRepository;

    /**
     * Get menu category details by ID
     * 
     * @param id Menu Category ID
     * @return Optional containing MenuCategoryDTO
     */
    @Transactional(readOnly = true)
    public Optional<MenuCategoryDTO> getMenuCategoryById(Long id) {
        return menuCategoryRepository.findByIdWithItems(id)
                .map(this::convertToDTO);
    }

    /**
     * Search menu categories by keyword
     */
    @Transactional(readOnly = true)
    public List<MenuCategoryDTO> searchMenuCategories(String keyword) {
        return menuCategoryRepository.searchMenuCategories(keyword).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all menu categories for a shop
     */
    @Transactional(readOnly = true)
    public List<MenuCategoryDTO> getMenuCategoriesByShopId(Long shopId) {
        return menuCategoryRepository.findByShopIdWithItems(shopId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private MenuCategoryDTO convertToDTO(org.th.entity.shops.MenuCategory cat) {
        // Map photos
        List<PhotoDTO> catPhotos = cat.getPhotos().stream()
                .map(photo -> PhotoDTO.builder()
                        .id(photo.getId())
                        .url(photo.getUrl())
                        .thumbnailUrl(photo.getThumbnailUrl())
                        .photoType(photo.getPhotoType())
                        .caption(photo.getCaption())
                        .captionMm(photo.getCaptionMm())
                        .captionEn(photo.getCaptionEn())
                        .isPrimary(photo.getIsPrimary())
                        .displayOrder(photo.getDisplayOrder())
                        .uploadedAt(photo.getUploadedAt())
                        .build())
                .collect(Collectors.toList());

        // Map items
        List<MenuItemDTO> items = cat.getItems().stream()
                .filter(MenuItem::getIsAvailable)
                .map(item -> {
                    List<PhotoDTO> itemPhotos = item.getPhotos().stream()
                            .map(photo -> PhotoDTO.builder()
                                    .id(photo.getId())
                                    .url(photo.getUrl())
                                    .thumbnailUrl(photo.getThumbnailUrl())
                                    .photoType(photo.getPhotoType())
                                    .caption(photo.getCaption())
                                    .captionMm(photo.getCaptionMm())
                                    .captionEn(photo.getCaptionEn())
                                    .isPrimary(photo.getIsPrimary())
                                    .displayOrder(photo.getDisplayOrder())
                                    .uploadedAt(photo.getUploadedAt())
                                    .build())
                            .collect(Collectors.toList());

                    return MenuItemDTO.builder()
                            .id(item.getId())
                            .name(item.getName())
                            .nameMm(item.getNameMm())
                            .nameEn(item.getNameEn())
                            .price(item.getPrice())
                            .currency(item.getCurrency())
                            .imageUrl(item.getImageUrl())
                            .isAvailable(item.getIsAvailable())
                            .isPopular(item.getIsPopular())
                            .isVegetarian(item.getIsVegetarian())
                            .isSpicy(item.getIsSpicy())
                            .displayOrder(item.getDisplayOrder())
                            .photos(itemPhotos)
                            .build();
                })
                .collect(Collectors.toList());

        return MenuCategoryDTO.builder()
                .id(cat.getId())
                .name(cat.getName())
                .nameMm(cat.getNameMm())
                .nameEn(cat.getNameEn())
                .displayOrder(cat.getDisplayOrder())
                .isActive(cat.getIsActive())
                .imageUrl(cat.getImageUrl())
                .photos(catPhotos)
                .items(items)
                .build();
    }
}
