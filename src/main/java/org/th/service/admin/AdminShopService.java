package org.th.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.th.dto.OperatingHourDTO;
import org.th.dto.ShopDetailDTO;
import org.th.dto.ShopListDTO;
import org.th.dto.ShopPhotoDTO;
import org.th.dto.CreateShopRequest;
import org.th.dto.OperatingHourRequest;
import org.th.dto.UpdateShopRequest;
import org.th.entity.shops.OperatingHour;
import org.th.entity.shops.Shop;
import org.th.entity.shops.ShopPhoto;
import org.th.exception.ResourceNotFoundException;
import org.th.repository.ShopRepository;
import org.th.service.SupabaseStorageService;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminShopService {

    private final ShopRepository shopRepository;
    private final SupabaseStorageService supabaseStorageService;
    private final org.th.repository.DistrictRepository districtRepository;

    @Transactional(readOnly = true)
    public Page<ShopListDTO> getAllShops(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Shop> shops;
        if (search != null && !search.isEmpty()) {
            shops = shopRepository.searchByShopName(search, pageable);
        } else {
            shops = shopRepository.findAll(pageable);
        }
        return shops.map(this::mapToShopListDTO);
    }

    private ShopListDTO mapToShopListDTO(Shop shop) {
        // Use the primary photo if available
        String primaryPhoto = null;
        if (shop.getPhotos() != null) {
            primaryPhoto = shop.getPhotos().stream()
                    .filter(p -> Boolean.TRUE.equals(p.getIsPrimary()))
                    .map(ShopPhoto::getUrl)
                    .findFirst()
                    .orElse(shop.getPhotos().stream().map(ShopPhoto::getUrl).findFirst().orElse(null));
        }

        return ShopListDTO.builder()
                .id(shop.getId())
                .name(shop.getName())
                .nameMm(shop.getNameMm())
                .nameEn(shop.getNameEn())
                .slug(shop.getSlug())
                .category(shop.getCategory())
                .subCategory(shop.getSubCategory())
                .address(shop.getAddress())
                .addressMm(shop.getAddressMm())
                .district(shop.getDistrict() != null ? shop.getDistrict().getNameEn() : null)
                .city(shop.getDistrict() != null && shop.getDistrict().getCity() != null
                        ? shop.getDistrict().getCity().getNameEn()
                        : null)
                .latitude(shop.getLatitude())
                .longitude(shop.getLongitude())
                .ratingAvg(shop.getRatingAvg())
                .ratingCount(shop.getRatingCount())
                .logoUrl(shop.getLogoUrl())
                .coverUrl(shop.getCoverUrl())
                .primaryPhotoUrl(primaryPhoto)
                .hasDelivery(shop.getHasDelivery())
                .hasParking(shop.getHasParking())
                .hasWifi(shop.getHasWifi())
                .isVerified(shop.getIsVerified())
                .isHalal(shop.getIsHalal())
                .isVegetarian(shop.getIsVegetarian())
                .pricePreference(shop.getPricePreference())
                .build();
    }

    @Transactional(readOnly = true)
    public ShopDetailDTO getShopById(Long id) {
        Shop shop = shopRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found with id: " + id));

        // Initialize lazy collections
        if (shop.getPhotos() != null) {
            shop.getPhotos().size();
        }
        if (shop.getOperatingHours() != null) {
            shop.getOperatingHours().size();
        }

        return mapToShopDetailDTO(shop);
    }

    /**
     * Create a new shop with optional cover and gallery photos
     */
    @Transactional
    public ShopDetailDTO createShop(CreateShopRequest request, MultipartFile logoPhoto, MultipartFile coverPhoto,
            List<MultipartFile> galleryPhotos) {
        log.info("Creating new shop: {}", request.getName());

        String slug = generateSlug(request.getName());
        if (shopRepository.findBySlug(slug) != null) {
            slug = slug + "-" + System.currentTimeMillis();
        }

        Shop shop = new Shop();
        shop.setName(request.getName());
        shop.setNameMm(request.getNameMm());
        shop.setNameEn(request.getNameEn());
        shop.setSlug(slug);

        shop.setCategory(request.getCategory());
        shop.setSubCategory(request.getSubCategory());

        shop.setLatitude(request.getLatitude());
        shop.setLongitude(request.getLongitude());
        shop.setAddress(request.getAddress());
        shop.setAddressMm(request.getAddressMm());
        // Lookup and set District
        org.th.entity.District district = districtRepository.findById(request.getDistrictId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("District not found with id: " + request.getDistrictId()));
        shop.setDistrict(district);

        shop.setPhone(request.getPhone());
        shop.setDescription(request.getDescription());
        shop.setDescriptionMm(request.getDescriptionMm());

        shop.setHasDelivery(request.getHasDelivery());
        shop.setHasParking(request.getHasParking());
        shop.setHasWifi(request.getHasWifi());

        shop.setIsHalal(request.getIsHalal());
        shop.setIsVegetarian(request.getIsVegetarian());
        shop.setPricePreference(request.getPricePreference());

        shop.setIsActive(true);
        shop.setIsVerified(true);

        shop = shopRepository.save(shop);

        if (logoPhoto != null && !logoPhoto.isEmpty()) {
            String url = supabaseStorageService.uploadImage(logoPhoto, "shops/" + shop.getId() + "/logo");
            shop.setLogoUrl(url);
        }

        if (coverPhoto != null && !coverPhoto.isEmpty()) {
            String url = supabaseStorageService.uploadImage(coverPhoto, "shops/" + shop.getId() + "/cover");
            shop.setCoverUrl(url);

            // Add to photos collection as primary for backward compatibility
            ShopPhoto photo = new ShopPhoto();
            photo.setShop(shop);
            photo.setUrl(url);
            photo.setThumbnailUrl(url);
            photo.setPhotoType("cover");
            photo.setIsPrimary(true);
            shop.getPhotos().add(photo);
        }

        if (galleryPhotos != null && !galleryPhotos.isEmpty()) {
            for (MultipartFile file : galleryPhotos) {
                if (file != null && !file.isEmpty()) {
                    String url = supabaseStorageService.uploadImage(file, "shops/" + shop.getId() + "/gallery");
                    ShopPhoto photo = new ShopPhoto();
                    photo.setShop(shop);
                    photo.setUrl(url);
                    photo.setThumbnailUrl(url);
                    photo.setPhotoType("gallery");
                    photo.setIsPrimary(false);
                    shop.getPhotos().add(photo);
                }
            }
        }

        if (request.getOperatingHours() != null) {
            for (OperatingHourRequest hr : request.getOperatingHours()) {
                OperatingHour hour = new OperatingHour();
                hour.setShop(shop);
                hour.setDayOfWeek(hr.getDayOfWeek());
                hour.setOpeningTime(LocalTime.parse(hr.getOpenTime()));
                hour.setClosingTime(LocalTime.parse(hr.getCloseTime()));
                hour.setIsClosed(hr.getIsClosed());
                shop.getOperatingHours().add(hour);
            }
        }

        Shop savedShop = shopRepository.save(shop);
        return mapToShopDetailDTO(savedShop);
    }

    private String generateSlug(String name) {
        if (name == null)
            return "shop-" + System.currentTimeMillis();
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-");
    }

    /**
     * Update an existing shop
     */
    @Transactional
    public ShopDetailDTO updateShop(Long id, UpdateShopRequest request, MultipartFile logoPhoto,
            MultipartFile coverPhoto) {
        Shop shop = shopRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found with id: " + id));

        if (request.getName() != null)
            shop.setName(request.getName());
        if (request.getNameMm() != null)
            shop.setNameMm(request.getNameMm());
        if (request.getNameEn() != null)
            shop.setNameEn(request.getNameEn());

        if (request.getCategory() != null)
            shop.setCategory(request.getCategory());
        if (request.getSubCategory() != null)
            shop.setSubCategory(request.getSubCategory());

        if (request.getLatitude() != null)
            shop.setLatitude(request.getLatitude());
        if (request.getLongitude() != null)
            shop.setLongitude(request.getLongitude());
        if (request.getAddress() != null)
            shop.setAddress(request.getAddress());
        if (request.getAddressMm() != null)
            shop.setAddressMm(request.getAddressMm());
        if (request.getDistrictId() != null) {
            org.th.entity.District district = districtRepository.findById(request.getDistrictId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "District not found with id: " + request.getDistrictId()));
            shop.setDistrict(district);
        }

        if (request.getPhone() != null)
            shop.setPhone(request.getPhone());
        if (request.getDescription() != null)
            shop.setDescription(request.getDescription());
        if (request.getDescriptionMm() != null)
            shop.setDescriptionMm(request.getDescriptionMm());

        if (request.getHasDelivery() != null)
            shop.setHasDelivery(request.getHasDelivery());
        if (request.getHasParking() != null)
            shop.setHasParking(request.getHasParking());
        if (request.getHasWifi() != null)
            shop.setHasWifi(request.getHasWifi());

        if (request.getIsHalal() != null)
            shop.setIsHalal(request.getIsHalal());
        if (request.getIsVegetarian() != null)
            shop.setIsVegetarian(request.getIsVegetarian());
        if (request.getPricePreference() != null)
            shop.setPricePreference(request.getPricePreference());

        if (request.getIsActive() != null)
            shop.setIsActive(request.getIsActive());
        if (request.getIsVerified() != null)
            shop.setIsVerified(request.getIsVerified());

        if (logoPhoto != null && !logoPhoto.isEmpty()) {
            String url = supabaseStorageService.uploadImage(logoPhoto, "shops/" + shop.getId() + "/logo");
            shop.setLogoUrl(url);
        }

        if (coverPhoto != null && !coverPhoto.isEmpty()) {
            // Find existing cover and remove or unset?
            // For now just add as new cover, user can manage photos separately
            // OR strictly replace:
            shop.getPhotos().stream().filter(ShopPhoto::getIsPrimary).forEach(p -> p.setIsPrimary(false));

            String url = supabaseStorageService.uploadImage(coverPhoto, "shops/" + shop.getId() + "/cover");
            shop.setCoverUrl(url);

            // Add to photos collection as primary for backward compatibility
            ShopPhoto photo = new ShopPhoto();
            photo.setShop(shop);
            photo.setUrl(url);
            photo.setThumbnailUrl(url);
            photo.setPhotoType("cover");
            photo.setIsPrimary(true);
            shop.getPhotos().add(photo);
        }

        if (request.getOperatingHours() != null) {
            shop.getOperatingHours().clear();
            for (OperatingHourRequest hr : request.getOperatingHours()) {
                OperatingHour hour = new OperatingHour();
                hour.setShop(shop);
                hour.setDayOfWeek(hr.getDayOfWeek());
                hour.setOpeningTime(LocalTime.parse(hr.getOpenTime()));
                hour.setClosingTime(LocalTime.parse(hr.getCloseTime()));
                hour.setIsClosed(hr.getIsClosed());
                shop.getOperatingHours().add(hour);
            }
        }

        Shop savedShop = shopRepository.save(shop);
        return mapToShopDetailDTO(savedShop);
    }

    @Transactional
    public void deleteShop(Long id) {
        Shop shop = shopRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found"));
        shopRepository.delete(shop);
    }

    @Transactional
    public ShopPhoto uploadShopPhoto(Long shopId, MultipartFile file, boolean isPrimary) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found"));

        if (isPrimary) {
            shop.getPhotos().stream().filter(ShopPhoto::getIsPrimary).forEach(p -> p.setIsPrimary(false));
        }

        String url = supabaseStorageService.uploadImage(file, "shops/" + shopId + "/gallery");

        ShopPhoto photo = new ShopPhoto();
        photo.setShop(shop);
        photo.setUrl(url);
        photo.setThumbnailUrl(url);
        photo.setIsPrimary(isPrimary);
        photo.setPhotoType(isPrimary ? "cover" : "gallery");

        shop.getPhotos().add(photo);
        shopRepository.save(shop);
        return photo;
    }

    @Transactional
    public void deleteShopPhoto(Long shopId, Long photoId) {
        Shop shop = shopRepository.findByIdWithDetails(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found"));

        ShopPhoto photo = shop.getPhotos().stream()
                .filter(p -> p.getId().equals(photoId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Photo not found"));

        shop.getPhotos().remove(photo);
        shopRepository.save(shop);
    }

    private ShopDetailDTO mapToShopDetailDTO(Shop shop) {
        return ShopDetailDTO.builder()
                .id(shop.getId())
                .name(shop.getName())
                .nameMm(shop.getNameMm())
                .nameEn(shop.getNameEn())
                .slug(shop.getSlug())
                .category(shop.getCategory())
                .subCategory(shop.getSubCategory())
                .latitude(shop.getLatitude())
                .longitude(shop.getLongitude())
                .address(shop.getAddress())
                .addressMm(shop.getAddressMm())
                .district(shop.getDistrict() != null ? shop.getDistrict().getNameEn() : null)
                .city(shop.getDistrict() != null && shop.getDistrict().getCity() != null
                        ? shop.getDistrict().getCity().getNameEn()
                        : null)
                .phone(shop.getPhone())
                .email(shop.getEmail())
                .description(shop.getDescription())
                .descriptionMm(shop.getDescriptionMm())
                .hasDelivery(shop.getHasDelivery())
                .hasParking(shop.getHasParking())
                .hasWifi(shop.getHasWifi())
                .ratingAvg(shop.getRatingAvg())
                .ratingCount(shop.getRatingCount())
                .isActive(shop.getIsActive())
                .isVerified(shop.getIsVerified())
                .isHalal(shop.getIsHalal())
                .isVegetarian(shop.getIsVegetarian())

                .pricePreference(shop.getPricePreference())
                .logoUrl(shop.getLogoUrl())
                .coverUrl(shop.getCoverUrl())
                .photos(mapToShopPhotoDTOs(shop.getPhotos()))
                .operatingHours(mapToOperatingHourDTOs(shop.getOperatingHours()))
                .createdAt(shop.getCreatedAt())
                .updatedAt(shop.getUpdatedAt())
                .menuCategories(Collections.emptyList()) // Admin shop details doesn't strictly need full menu tree
                .recentReviews(Collections.emptyList()) // Admin shop details doesn't strictly need reviews
                .build();
    }

    private List<ShopPhotoDTO> mapToShopPhotoDTOs(List<ShopPhoto> photos) {
        if (photos == null)
            return Collections.emptyList();
        return photos.stream()
                .map(photo -> ShopPhotoDTO.builder()
                        .id(photo.getId())
                        .url(photo.getUrl())
                        .thumbnailUrl(photo.getThumbnailUrl())
                        .photoType(photo.getPhotoType())
                        .isPrimary(photo.getIsPrimary())
                        .build())
                .collect(Collectors.toList());
    }

    private List<OperatingHourDTO> mapToOperatingHourDTOs(java.util.Collection<OperatingHour> hours) {
        if (hours == null)
            return Collections.emptyList();
        return hours.stream()
                .map(hour -> OperatingHourDTO.builder()
                        .id(hour.getId())
                        .dayOfWeek(hour.getDayOfWeek())
                        .openingTime(hour.getOpeningTime())
                        .closingTime(hour.getClosingTime())
                        .isClosed(hour.getIsClosed())
                        .build())
                .collect(Collectors.toList());
    }
}
