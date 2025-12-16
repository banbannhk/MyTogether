package org.th.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.th.dto.CreateMenuItemRequest;
import org.th.dto.MenuItemDTO;
import org.th.dto.PhotoDTO;
import org.th.dto.UpdateMenuItemRequest;
import org.th.entity.shops.MenuCategory;
import org.th.entity.shops.MenuItem;
import org.th.entity.shops.MenuItemPhoto;
import org.th.exception.ResourceNotFoundException;
import org.th.repository.MenuCategoryRepository;
import org.th.repository.MenuItemRepository;
import org.th.service.SupabaseStorageService;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminMenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final SupabaseStorageService supabaseStorageService;

    @Transactional(readOnly = true)
    public MenuItemDTO getMenuItemById(Long id) {
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + id));
        return mapToMenuItemDTO(item);
    }

    @Transactional
    public MenuItemDTO createMenuItem(Long categoryId, CreateMenuItemRequest request, MultipartFile image,
            List<MultipartFile> galleryPhotos) {
        MenuCategory category = menuCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        MenuItem item = new MenuItem();
        item.setCategory(category);
        item.setShop(category.getShop());
        item.setName(request.getName());
        item.setNameMm(request.getNameMm());
        item.setNameEn(request.getNameEn());
        item.setPrice(request.getPrice());
        item.setCurrency(request.getCurrency() != null ? request.getCurrency() : "MMK");
        item.setIsAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true);
        item.setIsPopular(request.getIsPopular() != null ? request.getIsPopular() : false);
        item.setIsVegetarian(request.getIsVegetarian() != null ? request.getIsVegetarian() : false);
        item.setIsSpicy(request.getIsSpicy() != null ? request.getIsSpicy() : false);
        item.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);

        item = menuItemRepository.save(item);

        if (image != null && !image.isEmpty()) {
            String url = supabaseStorageService.uploadImage(image,
                    "shops/" + category.getShop().getId() + "/items/" + item.getId() + "/main");
            item.setImageUrl(url);
            item = menuItemRepository.save(item);
        }

        if (galleryPhotos != null && !galleryPhotos.isEmpty()) {
            for (MultipartFile photo : galleryPhotos) {
                if (!photo.isEmpty()) {
                    String url = supabaseStorageService.uploadImage(photo, "shops/" + category.getShop().getId()
                            + "/items/" + item.getId() + "/gallery/" + System.currentTimeMillis());
                    MenuItemPhoto itemPhoto = new MenuItemPhoto();
                    itemPhoto.setItem(item);
                    itemPhoto.setUrl(url);
                    itemPhoto.setThumbnailUrl(url);
                    itemPhoto.setIsPrimary(false);
                    item.getPhotos().add(itemPhoto);
                }
            }
            item = menuItemRepository.save(item);
        }

        return mapToMenuItemDTO(item);
    }

    @Transactional
    public MenuItemDTO updateMenuItem(Long id, UpdateMenuItemRequest request, MultipartFile image,
            List<MultipartFile> galleryPhotos) {
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        if (request.getName() != null)
            item.setName(request.getName());
        if (request.getNameMm() != null)
            item.setNameMm(request.getNameMm());
        if (request.getNameEn() != null)
            item.setNameEn(request.getNameEn());
        if (request.getPrice() != null)
            item.setPrice(request.getPrice());
        if (request.getCurrency() != null)
            item.setCurrency(request.getCurrency());
        if (request.getIsAvailable() != null)
            item.setIsAvailable(request.getIsAvailable());
        if (request.getIsPopular() != null)
            item.setIsPopular(request.getIsPopular());
        if (request.getIsVegetarian() != null)
            item.setIsVegetarian(request.getIsVegetarian());
        if (request.getIsSpicy() != null)
            item.setIsSpicy(request.getIsSpicy());
        if (request.getDisplayOrder() != null)
            item.setDisplayOrder(request.getDisplayOrder());

        if (request.getCategoryId() != null) {
            MenuCategory newCategory = menuCategoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("New Category not found"));
            item.setCategory(newCategory);
        }

        if (image != null && !image.isEmpty()) {
            String url = supabaseStorageService.uploadImage(image,
                    "shops/" + item.getShop().getId() + "/items/" + item.getId() + "/main");
            item.setImageUrl(url);
        }

        if (galleryPhotos != null && !galleryPhotos.isEmpty()) {
            for (MultipartFile photo : galleryPhotos) {
                if (!photo.isEmpty()) {
                    String url = supabaseStorageService.uploadImage(photo, "shops/" + item.getShop().getId() + "/items/"
                            + item.getId() + "/gallery/" + System.currentTimeMillis());
                    MenuItemPhoto itemPhoto = new MenuItemPhoto();
                    itemPhoto.setItem(item);
                    itemPhoto.setUrl(url);
                    itemPhoto.setThumbnailUrl(url);
                    itemPhoto.setIsPrimary(false);
                    item.getPhotos().add(itemPhoto);
                }
            }
        }

        MenuItem savedItem = menuItemRepository.save(item);
        return mapToMenuItemDTO(savedItem);
    }

    @Transactional
    public void deleteMenuItem(Long id) {
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));
        menuItemRepository.delete(item);
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<MenuItemDTO> getMenuItems(Long categoryId, Long shopId, int page,
            int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<MenuItem> itemPage;

        if (categoryId != null) {
            itemPage = menuItemRepository.findByCategoryId(categoryId, pageable);
        } else if (shopId != null) {
            itemPage = menuItemRepository.findByShopId(shopId, pageable);
        } else {
            itemPage = menuItemRepository.findAll(pageable);
        }

        return itemPage.map(this::mapToMenuItemListDTO);
    }

    private MenuItemDTO mapToMenuItemListDTO(MenuItem item) {
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
                .photos(Collections.emptyList()) // No photos for list view
                .build();
    }

    private MenuItemDTO mapToMenuItemDTO(MenuItem item) {
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
    }
}
