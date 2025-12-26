package org.th.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.th.dto.CreateMenuSubCategoryRequest;
import org.th.dto.MenuSubCategoryDTO;
import org.th.dto.UpdateMenuSubCategoryRequest;
import org.th.entity.shops.MenuCategory;
import org.th.entity.shops.MenuSubCategory;
import org.th.entity.shops.MenuSubCategoryPhoto;
import org.th.exception.ResourceNotFoundException;
import org.th.repository.MenuCategoryRepository;
import org.th.repository.MenuSubCategoryRepository;
import org.th.service.SupabaseStorageService;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminMenuSubCategoryService {

    private final MenuSubCategoryRepository menuSubCategoryRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final SupabaseStorageService supabaseStorageService;

    @Transactional(readOnly = true)
    public MenuSubCategoryDTO getMenuSubCategoryById(Long id) {
        MenuSubCategory subCategory = menuSubCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SubCategory not found with id: " + id));
        return mapToDTO(subCategory);
    }

    @Transactional(readOnly = true)
    public List<MenuSubCategoryDTO> getMenuSubCategories(Long categoryId) {
        return menuSubCategoryRepository.findByMenuCategoryId(categoryId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public MenuSubCategoryDTO createMenuSubCategory(Long categoryId, CreateMenuSubCategoryRequest request,
            List<MultipartFile> photos) {
        MenuCategory menuCategory = menuCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("MenuCategory not found"));

        MenuSubCategory subCategory = new MenuSubCategory();
        subCategory.setMenuCategory(menuCategory);
        subCategory.setName(request.getName());
        subCategory.setNameMm(request.getNameMm());
        subCategory.setNameEn(request.getNameEn());

        // Generate slug from name
        String slug = generateSlug(request.getName());
        subCategory.setSlug(slug);

        subCategory.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);
        subCategory.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        subCategory = menuSubCategoryRepository.save(subCategory);

        if (photos != null && !photos.isEmpty()) {
            for (int i = 0; i < photos.size(); i++) {
                MultipartFile photo = photos.get(i);
                if (!photo.isEmpty()) {
                    String url = supabaseStorageService.uploadImage(photo,
                            "shops/" + menuCategory.getShop().getId() + "/subcategories/" + subCategory.getId() + "/"
                                    + System.currentTimeMillis());

                    // Set first photo as main image
                    if (i == 0) {
                        subCategory.setImageUrl(url);
                    }

                    MenuSubCategoryPhoto subCatPhoto = new MenuSubCategoryPhoto();
                    subCatPhoto.setMenuSubCategory(subCategory);
                    subCatPhoto.setUrl(url);
                    subCategory.getPhotos().add(subCatPhoto);
                }
            }
            subCategory = menuSubCategoryRepository.save(subCategory);
        }

        return mapToDTO(subCategory);
    }

    @Transactional
    public MenuSubCategoryDTO updateMenuSubCategory(Long id, UpdateMenuSubCategoryRequest request,
            List<MultipartFile> photos) {
        MenuSubCategory subCategory = menuSubCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SubCategory not found"));

        if (request.getName() != null)
            subCategory.setName(request.getName());
        if (request.getNameMm() != null)
            subCategory.setNameMm(request.getNameMm());
        if (request.getNameEn() != null)
            subCategory.setNameEn(request.getNameEn());
        if (request.getDisplayOrder() != null)
            subCategory.setDisplayOrder(request.getDisplayOrder());
        if (request.getIsActive() != null)
            subCategory.setIsActive(request.getIsActive());

        if (photos != null && !photos.isEmpty()) {
            for (int i = 0; i < photos.size(); i++) {
                MultipartFile photo = photos.get(i);
                if (!photo.isEmpty()) {
                    String url = supabaseStorageService.uploadImage(photo,
                            "shops/" + subCategory.getMenuCategory().getShop().getId() + "/subcategories/"
                                    + subCategory.getId() + "/" + System.currentTimeMillis());

                    // Set first photo as main image if not already set, or overwrite?
                    // Strategy: If new photos uploaded, update the main image to the first new one
                    if (i == 0) {
                        subCategory.setImageUrl(url);
                    }

                    MenuSubCategoryPhoto subCatPhoto = new MenuSubCategoryPhoto();
                    subCatPhoto.setMenuSubCategory(subCategory);
                    subCatPhoto.setUrl(url);
                    subCategory.getPhotos().add(subCatPhoto);
                }
            }
        }

        MenuSubCategory savedSubCategory = menuSubCategoryRepository.save(subCategory);
        return mapToDTO(savedSubCategory);
    }

    @Transactional
    public void deleteMenuSubCategory(Long id) {
        MenuSubCategory subCategory = menuSubCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SubCategory not found"));
        menuSubCategoryRepository.delete(subCategory);
    }

    private MenuSubCategoryDTO mapToDTO(MenuSubCategory subCategory) {
        List<String> photoUrls = subCategory.getPhotos() != null
                ? subCategory.getPhotos().stream().map(MenuSubCategoryPhoto::getUrl).collect(Collectors.toList())
                : Collections.emptyList();

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

    private String generateSlug(String name) {
        return name.toLowerCase().replaceAll("[^a-z0-9]", "-").replaceAll("-+", "-");
    }
}
