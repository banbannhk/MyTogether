package org.th.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.th.dto.ApiResponse;
import org.th.dto.CreateMenuCategoryRequest;
import org.th.dto.UpdateMenuCategoryRequest;
import org.th.entity.shops.MenuCategory;
import org.th.service.admin.AdminService;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Menu Category Management", description = "APIs for managing menu categories (Admin only)")
@PreAuthorize("hasRole('ADMIN')")
public class AdminMenuCategoryController {

    private final AdminService adminService;

    @PostMapping(value = "/shops/{shopId}/categories", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create Menu Category", description = "Create a new menu category with optional image and gallery photos")
    public ResponseEntity<ApiResponse<MenuCategory>> createMenuCategory(
            @PathVariable Long shopId,
            @RequestPart("data") String dataJson,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart(value = "galleryPhotos", required = false) List<MultipartFile> galleryPhotos) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            CreateMenuCategoryRequest request = mapper.readValue(dataJson, CreateMenuCategoryRequest.class);

            MenuCategory category = adminService.createMenuCategory(shopId, request, image, galleryPhotos);
            return ResponseEntity.ok(ApiResponse.success("Category created successfully", category));
        } catch (Exception e) {
            throw new RuntimeException("Invalid data format: " + e.getMessage());
        }
    }

    @PutMapping(value = "/categories/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update Menu Category", description = "Update a menu category")
    public ResponseEntity<ApiResponse<MenuCategory>> updateMenuCategory(
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

            MenuCategory category = adminService.updateMenuCategory(id, request, image, galleryPhotos);
            return ResponseEntity.ok(ApiResponse.success("Category updated successfully", category));
        } catch (Exception e) {
            throw new RuntimeException("Invalid data format: " + e.getMessage());
        }
    }

    @DeleteMapping("/categories/{id}")
    @Operation(summary = "Delete Menu Category", description = "Delete a menu category")
    public ResponseEntity<ApiResponse<Void>> deleteMenuCategory(@PathVariable Long id) {
        adminService.deleteMenuCategory(id);
        return ResponseEntity.ok(ApiResponse.success("Category deleted successfully", null));
    }
}
