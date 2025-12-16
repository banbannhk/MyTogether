package org.th.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.th.dto.ApiResponse;
import org.th.dto.CreateMenuCategoryRequest;
import org.th.dto.MenuCategoryDTO;
import org.th.dto.UpdateMenuCategoryRequest;
import org.th.entity.shops.MenuCategory;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
@Tag(name = "Admin Menu Category Management", description = "APIs for managing menu categories (Admin only)")
@PreAuthorize("hasRole('ADMIN')")
public class AdminMenuCategoryController {

    private final org.th.service.admin.AdminMenuCategoryService adminMenuCategoryService;

    @GetMapping
    @Operation(summary = "Get All Menu Categories", description = "Get a paginated list of menu categories, optionally filtered by shop")
    public ResponseEntity<ApiResponse<org.springframework.data.domain.Page<MenuCategoryDTO>>> getMenuCategories(
            @RequestParam(required = false) Long shopId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Categories fetched successfully",
                adminMenuCategoryService.getMenuCategories(shopId, page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Menu Category by ID", description = "Get menu category details")
    public ResponseEntity<ApiResponse<MenuCategoryDTO>> getMenuCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Category fetched successfully", adminMenuCategoryService.getMenuCategoryById(id)));
    }

    @PostMapping(value = "/shop/{shopId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create Menu Category", description = "Create a new menu category for a shop")
    public ResponseEntity<ApiResponse<MenuCategoryDTO>> createMenuCategory(
            @PathVariable Long shopId,
            @RequestPart("data") String dataJson,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart(value = "galleryPhotos", required = false) List<MultipartFile> galleryPhotos) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            CreateMenuCategoryRequest request = mapper.readValue(dataJson, CreateMenuCategoryRequest.class);

            MenuCategoryDTO category = adminMenuCategoryService.createMenuCategory(shopId, request, image,
                    galleryPhotos);
            return ResponseEntity.ok(ApiResponse.success("Menu category created successfully", category));
        } catch (Exception e) {
            throw new RuntimeException("Invalid data format: " + e.getMessage());
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update Menu Category", description = "Update an existing menu category")
    public ResponseEntity<ApiResponse<MenuCategoryDTO>> updateMenuCategory(
            @PathVariable Long id,
            @RequestPart(value = "data", required = false) String dataJson,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart(value = "galleryPhotos", required = false) List<MultipartFile> galleryPhotos) {

        try {
            UpdateMenuCategoryRequest request = new UpdateMenuCategoryRequest();
            if (dataJson != null) {
                ObjectMapper mapper = new ObjectMapper();
                request = mapper.readValue(dataJson, UpdateMenuCategoryRequest.class);
            }

            MenuCategoryDTO category = adminMenuCategoryService.updateMenuCategory(id, request, image, galleryPhotos);
            return ResponseEntity.ok(ApiResponse.success("Menu category updated successfully", category));
        } catch (Exception e) {
            throw new RuntimeException("Invalid data format: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Menu Category", description = "Delete a menu category")
    public ResponseEntity<ApiResponse<Void>> deleteMenuCategory(@PathVariable Long id) {
        adminMenuCategoryService.deleteMenuCategory(id);
        return ResponseEntity.ok(ApiResponse.success("Category deleted successfully", null));
    }
}
