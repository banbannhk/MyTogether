package org.th.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.th.dto.ApiResponse;
import org.th.dto.CreateMenuItemRequest;
import org.th.dto.MenuItemDTO;
import org.th.dto.UpdateMenuItemRequest;
import org.th.entity.shops.MenuItem;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/admin/items")
@RequiredArgsConstructor
@Tag(name = "Admin Menu Item Management", description = "APIs for managing menu items (Admin only)")
@PreAuthorize("hasRole('ADMIN')")
public class AdminMenuItemController {

    private final org.th.service.admin.AdminMenuItemService adminMenuItemService;

    @GetMapping
    @Operation(summary = "Get All Menu Items", description = "Get a paginated list of menu items, filtered by category or shop")
    public ResponseEntity<ApiResponse<org.springframework.data.domain.Page<MenuItemDTO>>> getMenuItems(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long shopId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Items fetched successfully",
                adminMenuItemService.getMenuItems(categoryId, shopId, page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Menu Item by ID", description = "Get menu item details")
    public ResponseEntity<ApiResponse<MenuItemDTO>> getMenuItemById(@PathVariable Long id) {
        return ResponseEntity
                .ok(ApiResponse.success("Item fetched successfully", adminMenuItemService.getMenuItemById(id)));
    }

    @PostMapping(value = "/categories/{categoryId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create Menu Item", description = "Create a new menu item for a category")
    public ResponseEntity<ApiResponse<MenuItemDTO>> createMenuItem(
            @PathVariable Long categoryId,
            @RequestPart("data") @jakarta.validation.Valid CreateMenuItemRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart(value = "galleryPhotos", required = false) List<MultipartFile> galleryPhotos) {

        MenuItemDTO item = adminMenuItemService.createMenuItem(categoryId, request, image, galleryPhotos);
        return ResponseEntity.ok(ApiResponse.success("Menu item created successfully", item));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update Menu Item", description = "Update an existing menu item")
    public ResponseEntity<ApiResponse<MenuItemDTO>> updateMenuItem(
            @PathVariable Long id,
            @RequestPart(value = "data", required = false) @jakarta.validation.Valid UpdateMenuItemRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart(value = "galleryPhotos", required = false) List<MultipartFile> galleryPhotos) {

        if (request == null) {
            request = new UpdateMenuItemRequest();
        }

        MenuItemDTO item = adminMenuItemService.updateMenuItem(id, request, image, galleryPhotos);
        return ResponseEntity.ok(ApiResponse.success("Menu item updated successfully", item));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Menu Item", description = "Delete a menu item")
    public ResponseEntity<ApiResponse<Void>> deleteMenuItem(@PathVariable Long id) {
        adminMenuItemService.deleteMenuItem(id);
        return ResponseEntity.ok(ApiResponse.success("Item deleted successfully", null));
    }
}
