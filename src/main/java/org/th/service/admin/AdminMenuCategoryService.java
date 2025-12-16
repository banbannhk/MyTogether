package org.th.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.th.dto.CreateMenuCategoryRequest;
import org.th.dto.MenuCategoryDTO;
import org.th.dto.MenuItemDTO;
import org.th.dto.PhotoDTO;
import org.th.dto.UpdateMenuCategoryRequest;
import org.th.entity.shops.MenuCategory;
import org.th.entity.shops.MenuCategoryPhoto;
import org.th.entity.shops.MenuItem;
import org.th.entity.shops.Shop;
import org.th.exception.ResourceNotFoundException;
import org.th.repository.MenuCategoryRepository;
import org.th.repository.ShopRepository;
import org.th.service.SupabaseStorageService;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminMenuCategoryService {

    private final MenuCategoryRepository menuCategoryRepository;
    private final ShopRepository shopRepository;
    private final SupabaseStorageService supabaseStorageService;

    @Transactional(readOnly = true)
    public MenuCategoryDTO getMenuCategoryById(Long id) {
        MenuCategory category = menuCategoryRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return mapToMenuCategoryDTO(category);
    }

    @Transactional
    public MenuCategoryDTO createMenuCategory(Long shopId, CreateMenuCategoryRequest request, MultipartFile image,
            List<MultipartFile> galleryPhotos) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found"));

        MenuCategory category = new MenuCategory();
        category.setShop(shop);
        category.setName(request.getName());
        category.setNameMm(request.getNameMm());
        category.setNameEn(request.getNameEn());
        category.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);
        category.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        category = menuCategoryRepository.save(category);

        if (image != null && !image.isEmpty()) {
            String url = supabaseStorageService.uploadImage(image,
                    "shops/" + shopId + "/categories/" + category.getId() + "/main");
            category.setImageUrl(url);
            category = menuCategoryRepository.save(category);
        }

        if (galleryPhotos != null && !galleryPhotos.isEmpty()) {
            for (MultipartFile photo : galleryPhotos) {
                if (!photo.isEmpty()) {
                    String url = supabaseStorageService.uploadImage(photo, "shops/" + shopId + "/categories/"
                            + category.getId() + "/gallery/" + System.currentTimeMillis());
                    MenuCategoryPhoto catPhoto = new MenuCategoryPhoto();
                    catPhoto.setCategory(category);
                    catPhoto.setUrl(url);
                    catPhoto.setThumbnailUrl(url);
                    catPhoto.setIsPrimary(false);
                    category.getPhotos().add(catPhoto);
                }
            }
            category = menuCategoryRepository.save(category);
        }

        return mapToMenuCategoryDTO(category);
    }

    @Transactional
    public MenuCategoryDTO updateMenuCategory(Long id, UpdateMenuCategoryRequest request, MultipartFile image,
            List<MultipartFile> galleryPhotos) {
        MenuCategory category = menuCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (request.getName() != null)
            category.setName(request.getName());
        if (request.getNameMm() != null)
            category.setNameMm(request.getNameMm());
        if (request.getNameEn() != null)
            category.setNameEn(request.getNameEn());
        if (request.getDisplayOrder() != null)
            category.setDisplayOrder(request.getDisplayOrder());
        if (request.getIsActive() != null)
            category.setIsActive(request.getIsActive());

        if (image != null && !image.isEmpty()) {
            String url = supabaseStorageService.uploadImage(image,
                    "shops/" + category.getShop().getId() + "/categories/" + category.getId() + "/main");
            category.setImageUrl(url);
        }

        if (galleryPhotos != null && !galleryPhotos.isEmpty()) {
            for (MultipartFile photo : galleryPhotos) {
                if (!photo.isEmpty()) {
                    String url = supabaseStorageService.uploadImage(photo, "shops/" + category.getShop().getId()
                            + "/categories/" + category.getId() + "/gallery/" + System.currentTimeMillis());
                    MenuCategoryPhoto catPhoto = new MenuCategoryPhoto();
                    catPhoto.setCategory(category);
                    catPhoto.setUrl(url);
                    catPhoto.setThumbnailUrl(url);
                    catPhoto.setIsPrimary(false);
                    category.getPhotos().add(catPhoto);
                }
            }
        }

        MenuCategory savedCategory = menuCategoryRepository.save(category);
        return mapToMenuCategoryDTO(savedCategory);
    }

    @Transactional
    public void deleteMenuCategory(Long id) {
        MenuCategory category = menuCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        menuCategoryRepository.delete(category);
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<MenuCategoryDTO> getMenuCategories(Long shopId, int page, int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<MenuCategory> categoryPage;

        if (shopId != null) {
            categoryPage = menuCategoryRepository.findByShopId(shopId, pageable);
        } else {
            categoryPage = menuCategoryRepository.findAll(pageable);
        }

        return categoryPage.map(this::mapToMenuCategoryListDTO);
    }

    private MenuCategoryDTO mapToMenuCategoryListDTO(MenuCategory cat) {
        return MenuCategoryDTO.builder()
                .id(cat.getId())
                .name(cat.getName())
                .nameMm(cat.getNameMm())
                .nameEn(cat.getNameEn())
                .displayOrder(cat.getDisplayOrder())
                .isActive(cat.getIsActive())
                .imageUrl(cat.getImageUrl())
                // Do NOT map photos or items for list view to verify performance
                .photos(Collections.emptyList())
                .items(Collections.emptyList())
                .build();
    }

    private MenuCategoryDTO mapToMenuCategoryDTO(MenuCategory cat) {
        // Map photos
        List<PhotoDTO> catPhotos = (cat.getPhotos() == null) ? Collections.emptyList()
                : cat.getPhotos().stream()
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
        List<MenuItemDTO> items = (cat.getItems() == null) ? Collections.emptyList()
                : cat.getItems().stream()
                        // .filter(MenuItem::getIsAvailable) // Should admin see unavailable items? YES.
                        .map(item -> {
                            List<PhotoDTO> itemPhotos = (item.getPhotos() == null) ? Collections.emptyList()
                                    : item.getPhotos().stream()
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
