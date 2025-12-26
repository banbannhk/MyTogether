package org.th.controller.mobile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.th.dto.ApiResponse;
import org.th.dto.ShopPhotoDTO;
import org.th.entity.shops.Shop;
import org.th.entity.shops.ShopPhoto;
import org.th.exception.ResourceNotFoundException;
import org.th.exception.SupabaseStorageException;
import org.th.repository.ShopRepository;
import org.th.service.SupabaseStorageService;
import org.th.service.ShopService;

import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller for managing shop photos
 */
@RestController
@RequestMapping("/api/mobile/shops/photos")
@Tag(name = "Shop Photos", description = "APIs for managing shop photos")
@Slf4j
public class ShopPhotoController {

    @Autowired
    private SupabaseStorageService supabaseStorageService;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private ShopService shopService;

    /**
     * Upload a photo for a shop
     */
    @PostMapping("/{shopId}/photos")
    @Operation(summary = "Upload shop photo", description = "Upload a new photo for a shop")
    public ResponseEntity<ApiResponse<ShopPhotoDTO>> uploadPhoto(
            @PathVariable Long shopId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "isPrimary", defaultValue = "false") Boolean isPrimary,
            @RequestParam(value = "photoType", defaultValue = "other") String photoType,
            @RequestParam(value = "caption", required = false) String caption) {
        try {
            // Find the shop
            Shop shop = shopService.getShopById(shopId)
                    .orElseThrow(() -> new ResourceNotFoundException("Shop not found with id: " + shopId));

            // Upload to Supabase Storage
            String folder = "shops/" + shopId;
            String imageUrl = supabaseStorageService.uploadImage(file, folder);

            // Create ShopPhoto entity
            ShopPhoto shopPhoto = new ShopPhoto();
            shopPhoto.setShop(shop);
            shopPhoto.setUrl(imageUrl);
            shopPhoto.setThumbnailUrl(imageUrl); // Use same URL for now
            shopPhoto.setPhotoType(photoType);
            shopPhoto.setCaption(caption);
            shopPhoto.setIsPrimary(isPrimary);
            shopPhoto.setDisplayOrder(shop.getPhotos().size());

            // If this is set as primary, unset other primary photos
            if (isPrimary) {
                shop.getPhotos().forEach(photo -> photo.setIsPrimary(false));
            }

            // Add to shop and save
            shop.getPhotos().add(shopPhoto);
            shopRepository.save(shop);

            // Convert to DTO
            ShopPhotoDTO photoDTO = ShopPhotoDTO.builder()
                    .id(shopPhoto.getId())
                    .url(shopPhoto.getUrl())
                    .thumbnailUrl(shopPhoto.getThumbnailUrl())
                    .photoType(shopPhoto.getPhotoType())
                    .caption(shopPhoto.getCaption())
                    .isPrimary(shopPhoto.getIsPrimary())
                    .displayOrder(shopPhoto.getDisplayOrder())
                    .uploadedAt(shopPhoto.getUploadedAt())
                    .build();

            log.info("Successfully uploaded photo for shop {}", shopId);
            return ResponseEntity.ok(ApiResponse.success("Photo uploaded successfully", photoDTO));

        } catch (ResourceNotFoundException e) {
            log.error("Shop not found: {}", shopId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (SupabaseStorageException e) {
            log.error("Failed to upload photo to Supabase", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to upload photo: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error uploading photo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An unexpected error occurred"));
        }
    }

    /**
     * Get all photos for a shop
     */
    @GetMapping("/{shopId}/photos")
    @Operation(summary = "Get shop photos", description = "Get all photos for a shop")
    public ResponseEntity<ApiResponse<List<ShopPhotoDTO>>> getShopPhotos(@PathVariable Long shopId) {
        try {
            Shop shop = shopRepository.findByIdWithDetails(shopId)
                    .orElseThrow(() -> new ResourceNotFoundException("Shop not found with id: " + shopId));

            List<ShopPhotoDTO> photoDTOs = shop.getPhotos().stream()
                    .map(photo -> ShopPhotoDTO.builder()
                            .id(photo.getId())
                            .url(photo.getUrl())
                            .thumbnailUrl(photo.getThumbnailUrl())
                            .photoType(photo.getPhotoType())
                            .caption(photo.getCaption())
                            .captionMm(photo.getCaptionMm())
                            .captionEn(photo.getCaptionEn())
                            .isPrimary(photo.getIsPrimary())
                            .displayOrder(photo.getDisplayOrder())
                            .uploadedAt(photo.getUploadedAt())
                            .build())
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success("Photos retrieved successfully", photoDTOs));

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Delete a photo
     */
    @DeleteMapping("/{shopId}/photos/{photoId}")
    @Operation(summary = "Delete shop photo", description = "Delete a photo from a shop")
    public ResponseEntity<ApiResponse<Void>> deletePhoto(
            @PathVariable Long shopId,
            @PathVariable Long photoId) {
        try {
            Shop shop = shopRepository.findByIdWithDetails(shopId)
                    .orElseThrow(() -> new ResourceNotFoundException("Shop not found with id: " + shopId));

            ShopPhoto photoToDelete = shop.getPhotos().stream()
                    .filter(photo -> photo.getId().equals(photoId))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Photo not found with id: " + photoId));

            // Extract file path from URL
            String url = photoToDelete.getUrl();
            String filePath = extractFilePathFromUrl(url);

            // Delete from Supabase Storage
            if (filePath != null) {
                supabaseStorageService.deleteImage(filePath);
            }

            // Remove from shop
            shop.getPhotos().remove(photoToDelete);
            shopRepository.save(shop);

            log.info("Successfully deleted photo {} from shop {}", photoId, shopId);
            return ResponseEntity.ok(ApiResponse.success("Photo deleted successfully", null));

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (SupabaseStorageException e) {
            log.error("Failed to delete photo from Supabase", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete photo: " + e.getMessage()));
        }
    }

    /**
     * Set a photo as primary
     */
    @PutMapping("/{shopId}/photos/{photoId}/primary")
    @Operation(summary = "Set primary photo", description = "Set a photo as the primary photo for a shop")
    public ResponseEntity<ApiResponse<Void>> setPrimaryPhoto(
            @PathVariable Long shopId,
            @PathVariable Long photoId) {
        try {
            Shop shop = shopRepository.findByIdWithDetails(shopId)
                    .orElseThrow(() -> new ResourceNotFoundException("Shop not found with id: " + shopId));

            // Unset all primary photos
            shop.getPhotos().forEach(photo -> photo.setIsPrimary(false));

            // Set the new primary photo
            ShopPhoto primaryPhoto = shop.getPhotos().stream()
                    .filter(photo -> photo.getId().equals(photoId))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Photo not found with id: " + photoId));

            primaryPhoto.setIsPrimary(true);
            shopRepository.save(shop);

            log.info("Set photo {} as primary for shop {}", photoId, shopId);
            return ResponseEntity.ok(ApiResponse.success("Primary photo set successfully", null));

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Extract file path from Supabase URL
     */
    private String extractFilePathFromUrl(String url) {
        if (url == null || !url.contains("/storage/v1/object/public/")) {
            return null;
        }

        String[] parts = url.split("/storage/v1/object/public/");
        if (parts.length < 2) {
            return null;
        }

        // Remove bucket name from path
        String pathWithBucket = parts[1];
        int firstSlash = pathWithBucket.indexOf('/');
        if (firstSlash == -1) {
            return null;
        }

        return pathWithBucket.substring(firstSlash + 1);
    }
}
