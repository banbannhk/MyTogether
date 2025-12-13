package org.th.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.th.dto.ApiResponse;
import org.th.dto.CreateMenuItemRequest;
import org.th.dto.UpdateMenuItemRequest;
import org.th.entity.shops.MenuItem;
import org.th.service.admin.AdminService;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Menu Item Management", description = "APIs for managing menu items (Admin only)")
@PreAuthorize("hasRole('ADMIN')")
public class AdminMenuItemController {

    private final AdminService adminService;

    @PostMapping(value = "/categories/{categoryId}/items", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create Menu Item", description = "Create a new menu item with optional image and gallery photos")
    public ResponseEntity<ApiResponse<MenuItem>> createMenuItem(
            @PathVariable Long categoryId,
            @RequestPart("data") String dataJson,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart(value = "galleryPhotos", required = false) List<MultipartFile> galleryPhotos) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            CreateMenuItemRequest request = mapper.readValue(dataJson, CreateMenuItemRequest.class);

            MenuItem item = adminService.createMenuItem(categoryId, request, image, galleryPhotos);
            return ResponseEntity.ok(ApiResponse.success("Item created successfully", item));
        } catch (Exception e) {
            throw new RuntimeException("Invalid data format: " + e.getMessage());
        }
    }

    @PutMapping(value = "/items/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update Menu Item", description = "Update a menu item")
    public ResponseEntity<ApiResponse<MenuItem>> updateMenuItem(
            @PathVariable Long id,
            @RequestPart(value = "data", required = false) String dataJson,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart(value = "galleryPhotos", required = false) List<MultipartFile> galleryPhotos) {

        try {
            UpdateMenuItemRequest request = new UpdateMenuItemRequest();
            if (dataJson != null) {
                ObjectMapper mapper = new ObjectMapper();
                request = mapper.readValue(dataJson, UpdateMenuItemRequest.class);
            }

            MenuItem item = adminService.updateMenuItem(id, request, image, galleryPhotos);
            return ResponseEntity.ok(ApiResponse.success("Item updated successfully", item));
        } catch (Exception e) {
            throw new RuntimeException("Invalid data format: " + e.getMessage());
        }
    }

    @DeleteMapping("/items/{id}")
    @Operation(summary = "Delete Menu Item", description = "Delete a menu item")
    public ResponseEntity<ApiResponse<Void>> deleteMenuItem(@PathVariable Long id) {
        adminService.deleteMenuItem(id);
        return ResponseEntity.ok(ApiResponse.success("Item deleted successfully", null));
    }
}
