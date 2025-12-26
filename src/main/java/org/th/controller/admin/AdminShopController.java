package org.th.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.th.dto.ApiResponse;
import org.th.dto.CreateShopRequest;
import org.th.dto.ShopDetailDTO;
import org.th.dto.ShopListDTO;
import org.th.dto.UpdateShopRequest;
import org.th.entity.shops.Shop;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/admin/shops")
@RequiredArgsConstructor
@Tag(name = "Admin Shop Management", description = "APIs for managing shops (Admin only)")
@PreAuthorize("hasRole('ADMIN')")
@lombok.extern.slf4j.Slf4j
public class AdminShopController {

    private final org.th.service.admin.AdminShopService adminShopService;

    @GetMapping
    @Operation(summary = "Get All Shops", description = "Get a paginated list of all shops")
    public ResponseEntity<ApiResponse<org.springframework.data.domain.Page<ShopListDTO>>> getAllShops(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        long start = System.currentTimeMillis();
        log.info("Performance: getAllShops started");

        var result = adminShopService.getAllShops(page, size, search);

        long end = System.currentTimeMillis();
        log.info("Performance: getAllShops finished in {} ms", end - start);

        return ResponseEntity
                .ok(ApiResponse.success("Shops fetched successfully", result));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Shop by ID", description = "Get shop details including photos and operating hours")
    public ResponseEntity<ApiResponse<ShopDetailDTO>> getShopById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Shop fetched successfully", adminShopService.getShopById(id)));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create Shop", description = "Create a new shop with optional cover and gallery photos")
    public ResponseEntity<ApiResponse<ShopDetailDTO>> createShop(
            @RequestPart("data") @jakarta.validation.Valid CreateShopRequest request,
            @RequestPart(value = "logoPhoto", required = false) MultipartFile logoPhoto,
            @RequestPart(value = "coverPhoto", required = false) MultipartFile coverPhoto,
            @RequestPart(value = "galleryPhotos", required = false) List<MultipartFile> galleryPhotos) {

        ShopDetailDTO shop = adminShopService.createShop(request, logoPhoto, coverPhoto, galleryPhotos);
        return ResponseEntity.ok(ApiResponse.success("Shop created successfully", shop));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update Shop", description = "Update an existing shop")
    public ResponseEntity<ApiResponse<ShopDetailDTO>> updateShop(
            @PathVariable Long id,
            @RequestPart(value = "data", required = false) @jakarta.validation.Valid UpdateShopRequest request,
            @RequestPart(value = "logoPhoto", required = false) MultipartFile logoPhoto,
            @RequestPart(value = "coverPhoto", required = false) MultipartFile coverPhoto) {

        if (request == null) {
            request = new UpdateShopRequest();
        }

        ShopDetailDTO shop = adminShopService.updateShop(id, request, logoPhoto, coverPhoto);
        return ResponseEntity.ok(ApiResponse.success("Shop updated successfully", shop));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Shop", description = "Permanently delete a shop")
    public ResponseEntity<ApiResponse<Void>> deleteShop(@PathVariable Long id) {
        adminShopService.deleteShop(id);
        return ResponseEntity.ok(ApiResponse.success("Shop deleted successfully", null));
    }

}
