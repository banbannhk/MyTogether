package org.th.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.th.dto.ApiResponse;
import org.th.dto.CreateMenuSubCategoryRequest;
import org.th.dto.MenuSubCategoryDTO;
import org.th.dto.UpdateMenuSubCategoryRequest;
import org.th.service.admin.AdminMenuSubCategoryService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/menu-sub-categories")
@RequiredArgsConstructor
@Tag(name = "Admin Menu SubCategory Management", description = "APIs for managing menu sub-categories (Admin only)")
@PreAuthorize("hasRole('ADMIN')")
public class AdminMenuSubCategoryController {

    private final AdminMenuSubCategoryService adminMenuSubCategoryService;

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get SubCategories by Menu Category", description = "Get all sub-categories for a given menu category")
    public ResponseEntity<ApiResponse<List<MenuSubCategoryDTO>>> getSubCategories(@PathVariable Long categoryId) {
        return ResponseEntity.ok(ApiResponse.success("SubCategories fetched successfully",
                adminMenuSubCategoryService.getMenuSubCategories(categoryId)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get SubCategory by ID", description = "Get details of a specific sub-category")
    public ResponseEntity<ApiResponse<MenuSubCategoryDTO>> getSubCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("SubCategory fetched successfully",
                adminMenuSubCategoryService.getMenuSubCategoryById(id)));
    }

    @PostMapping(value = "/category/{categoryId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create SubCategory", description = "Create a new sub-category with optional photos")
    public ResponseEntity<ApiResponse<MenuSubCategoryDTO>> createSubCategory(
            @PathVariable Long categoryId,
            @RequestPart("data") @jakarta.validation.Valid CreateMenuSubCategoryRequest request,
            @RequestPart(value = "photos", required = false) List<MultipartFile> photos) {

        return ResponseEntity.ok(ApiResponse.success("SubCategory created successfully",
                adminMenuSubCategoryService.createMenuSubCategory(categoryId, request, photos)));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update SubCategory", description = "Update an existing sub-category")
    public ResponseEntity<ApiResponse<MenuSubCategoryDTO>> updateSubCategory(
            @PathVariable Long id,
            @RequestPart(value = "data", required = false) UpdateMenuSubCategoryRequest request,
            @RequestPart(value = "photos", required = false) List<MultipartFile> photos) {

        if (request == null)
            request = new UpdateMenuSubCategoryRequest();

        return ResponseEntity.ok(ApiResponse.success("SubCategory updated successfully",
                adminMenuSubCategoryService.updateMenuSubCategory(id, request, photos)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete SubCategory", description = "Permanently delete a sub-category")
    public ResponseEntity<ApiResponse<Void>> deleteSubCategory(@PathVariable Long id) {
        adminMenuSubCategoryService.deleteMenuSubCategory(id);
        return ResponseEntity.ok(ApiResponse.success("SubCategory deleted successfully", null));
    }
}
