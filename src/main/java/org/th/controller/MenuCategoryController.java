package org.th.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.th.dto.ApiResponse;
import org.th.dto.MenuCategoryDTO;
import org.th.service.MenuCategoryService;

import java.util.Optional;

@RestController
@RequestMapping("/api/menu-categories")
@RequiredArgsConstructor
@Tag(name = "Menu Categories", description = "Access specific menu category details")
public class MenuCategoryController {

    private final MenuCategoryService menuCategoryService; // Changed from ShopService

    /**
     * Get menu category details by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get menu category by ID", description = "Get details of a specific menu category including its menu items")
    public ResponseEntity<ApiResponse<MenuCategoryDTO>> getMenuCategory(
            @Parameter(description = "Menu Category ID") @PathVariable Long id) {

        Optional<MenuCategoryDTO> categoryOpt = menuCategoryService.getMenuCategoryById(id); // Changed from shopService

        if (categoryOpt.isEmpty()) {
            return ResponseEntity.status(404).body(ApiResponse.error("Menu category not found"));
        }

        return ResponseEntity.ok(ApiResponse.success("Menu category found", categoryOpt.get()));
    }
}
