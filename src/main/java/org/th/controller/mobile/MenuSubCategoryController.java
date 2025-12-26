package org.th.controller.mobile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.th.dto.ApiResponse;
import org.th.dto.MenuSubCategoryDTO;
import org.th.service.MenuSubCategoryService;

import java.util.List;

@RestController
@RequestMapping("/api/mobile/menu-sub-categories")
@RequiredArgsConstructor
@Tag(name = "Mobile Menu Sub-Categories", description = "APIs for retrieving menu sub-categories (Mobile)")
public class MenuSubCategoryController {

    private final MenuSubCategoryService menuSubCategoryService;

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get Sub-Categories by Category ID", description = "Get a list of active sub-categories for a specific menu category")
    public ResponseEntity<ApiResponse<List<MenuSubCategoryDTO>>> getSubCategoriesByCategory(
            @Parameter(description = "Menu Category ID") @PathVariable Long categoryId) {

        List<MenuSubCategoryDTO> subCategories = menuSubCategoryService.getMenuSubCategoriesByCategoryId(categoryId);
        return ResponseEntity.ok(ApiResponse.success("Sub-categories fetched successfully", subCategories));
    }
}
