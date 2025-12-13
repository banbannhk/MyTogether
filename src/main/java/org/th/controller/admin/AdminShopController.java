package org.th.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.th.dto.ApiResponse;
import org.th.dto.CreateShopRequest;
import org.th.dto.ShopListDTO;
import org.th.dto.UpdateShopRequest;
import org.th.entity.shops.Shop;
import org.th.service.admin.AdminService;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/admin/shops")
@RequiredArgsConstructor
@Tag(name = "Admin Shop Management", description = "APIs for managing shops (Admin only)")
@PreAuthorize("hasRole('ADMIN')")
public class AdminShopController {

    private final AdminService adminService;

    @GetMapping
    @Operation(summary = "Get All Shops", description = "Get a paginated list of all shops")
    public ResponseEntity<ApiResponse<org.springframework.data.domain.Page<ShopListDTO>>> getAllShops(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        return ResponseEntity
                .ok(ApiResponse.success("Shops fetched successfully", adminService.getAllShops(page, size, search)));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create Shop", description = "Create a new shop with optional cover and gallery photos")
    public ResponseEntity<ApiResponse<Shop>> createShop(
            @RequestPart("data") String dataJson,
            @RequestPart(value = "coverPhoto", required = false) MultipartFile coverPhoto,
            @RequestPart(value = "galleryPhotos", required = false) List<MultipartFile> galleryPhotos) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            CreateShopRequest request = mapper.readValue(dataJson, CreateShopRequest.class);

            Shop shop = adminService.createShop(request, coverPhoto, galleryPhotos);
            return ResponseEntity.ok(ApiResponse.success("Shop created successfully", shop));
        } catch (Exception e) {
            throw new RuntimeException("Invalid data format: " + e.getMessage());
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update Shop", description = "Update an existing shop")
    public ResponseEntity<ApiResponse<Shop>> updateShop(
            @PathVariable Long id,
            @RequestPart(value = "data", required = false) String dataJson,
            @RequestPart(value = "coverPhoto", required = false) MultipartFile coverPhoto) {

        try {
            UpdateShopRequest request = new UpdateShopRequest();
            if (dataJson != null) {
                ObjectMapper mapper = new ObjectMapper();
                request = mapper.readValue(dataJson, UpdateShopRequest.class);
            }

            Shop shop = adminService.updateShop(id, request, coverPhoto);
            return ResponseEntity.ok(ApiResponse.success("Shop updated successfully", shop));
        } catch (Exception e) {
            throw new RuntimeException("Invalid data format: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Shop", description = "Permanently delete a shop")
    public ResponseEntity<ApiResponse<Void>> deleteShop(@PathVariable Long id) {
        adminService.deleteShop(id);
        return ResponseEntity.ok(ApiResponse.success("Shop deleted successfully", null));
    }

    @PostMapping(value = "/{id}/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload Shop Photo", description = "Upload a single photo to shop gallery")
    public ResponseEntity<ApiResponse<String>> uploadShopPhoto(
            @PathVariable Long id,
            @RequestParam("photo") MultipartFile photo) {
        String url = adminService.uploadShopPhoto(id, photo, false).getUrl();
        return ResponseEntity.ok(ApiResponse.success("Photo uploaded successfully", url));
    }
}
