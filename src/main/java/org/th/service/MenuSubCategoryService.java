package org.th.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.th.dto.MenuSubCategoryDTO;
import org.th.entity.shops.MenuSubCategory;
import org.th.entity.shops.MenuSubCategoryPhoto;
import org.th.repository.MenuSubCategoryRepository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuSubCategoryService {

    private final MenuSubCategoryRepository menuSubCategoryRepository;

    /**
     * Get active sub-categories by category ID
     */
    @Transactional(readOnly = true)
    public List<MenuSubCategoryDTO> getMenuSubCategoriesByCategoryId(Long categoryId) {
        List<MenuSubCategory> subCategories = menuSubCategoryRepository
                .findByMenuCategoryIdAndIsActiveTrueOrderByDisplayOrderAsc(categoryId);
        return subCategories.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private MenuSubCategoryDTO mapToDTO(MenuSubCategory subCategory) {
        List<String> photoUrls = (subCategory.getPhotos() == null) ? Collections.emptyList()
                : subCategory.getPhotos().stream()
                        .map(MenuSubCategoryPhoto::getUrl)
                        .collect(Collectors.toList());

        return MenuSubCategoryDTO.builder()
                .id(subCategory.getId())
                .name(subCategory.getName())
                .nameMm(subCategory.getNameMm())
                .nameEn(subCategory.getNameEn())
                .slug(subCategory.getSlug())
                .displayOrder(subCategory.getDisplayOrder())
                .isActive(subCategory.getIsActive())
                .menuCategoryId(subCategory.getMenuCategory().getId())
                .imageUrl(subCategory.getImageUrl())
                .photoUrls(photoUrls)
                .build();
    }
}
