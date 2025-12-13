package org.th.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.th.dto.CreateShopRequest;
import org.th.dto.OperatingHourRequest;
import org.th.dto.ShopListDTO;
import org.th.dto.UpdateShopRequest;
import org.th.dto.UserDTO;
import org.th.dto.CreateMenuCategoryRequest;
import org.th.dto.CreateMenuItemRequest;
import org.th.dto.UpdateMenuCategoryRequest;
import org.th.dto.UpdateMenuItemRequest;
import org.th.entity.User;
import org.th.entity.shops.MenuCategory;
import org.th.entity.shops.MenuCategoryPhoto;
import org.th.entity.shops.MenuItem;
import org.th.entity.shops.MenuItemPhoto;
import org.th.entity.shops.Shop;
import org.th.entity.shops.ShopPhoto;
import org.th.entity.shops.OperatingHour;
import org.th.exception.ResourceNotFoundException;
import org.th.repository.MenuCategoryRepository;
import org.th.repository.MenuItemRepository;
import org.th.repository.ShopRepository;
import org.th.repository.ShopReviewRepository;
import org.th.repository.UserRepository;
import org.th.service.SupabaseStorageService;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final ShopReviewRepository shopReviewRepository;
    private final SupabaseStorageService supabaseStorageService;
    private final MenuCategoryRepository menuCategoryRepository;
    private final MenuItemRepository menuItemRepository;

    /**
     * Get system-wide statistics for the admin dashboard.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getSystemStats() {
        log.info("Fetching system statistics for admin dashboard");

        Map<String, Object> stats = new HashMap<>();

        long totalUsers = userRepository.count();
        long totalShops = shopRepository.count();
        long totalReviews = shopReviewRepository.count();

        stats.put("totalUsers", totalUsers);
        stats.put("totalShops", totalShops);
        stats.put("totalReviews", totalReviews);

        return stats;
    }

    @Transactional(readOnly = true)
    public Page<UserDTO> getAllUsers(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users;
        if (search != null && !search.isEmpty()) {
            users = userRepository.searchUsers(search, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }
        return users.map(this::mapToUserDTO);
    }

    private UserDTO mapToUserDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName()); // Assuming fullName exists in User
        dto.setEnabled(user.isEnabled());
        // Simple role mapping logic
        if (user.getRole() != null) {
            dto.setRole(user.getRole().name());
        }
        return dto;
    }

    @Transactional(readOnly = true)
    public Page<ShopListDTO> getAllShops(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Shop> shops;
        if (search != null && !search.isEmpty()) {
            // Assuming searchShops exists, otherwise fallback to findAll or implement it
            shops = shopRepository.findAll(pageable); // TODO: Implement search
        } else {
            shops = shopRepository.findAll(pageable);
        }
        return shops.map(this::mapToShopListDTO);
    }

    private ShopListDTO mapToShopListDTO(Shop shop) {
        ShopListDTO dto = new ShopListDTO();
        dto.setId(shop.getId());
        dto.setName(shop.getName());
        dto.setCategory(shop.getCategory());
        // Map other fields as needed
        return dto;
    }

    /**
     * Create a new shop with optional cover and gallery photos
     */
    @Transactional
    public Shop createShop(CreateShopRequest request, MultipartFile coverPhoto, List<MultipartFile> galleryPhotos) {
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
        shop.setTownship(request.getTownship());
        shop.setCity(request.getCity() != null ? request.getCity() : "Yangon");

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

        if (coverPhoto != null && !coverPhoto.isEmpty()) {
            String url = supabaseStorageService.uploadImage(coverPhoto, "shops/" + shop.getId() + "/cover");
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

        return shopRepository.save(shop);
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
    public Shop updateShop(Long id, UpdateShopRequest request, MultipartFile coverPhoto) {
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
        if (request.getTownship() != null)
            shop.setTownship(request.getTownship());
        if (request.getCity() != null)
            shop.setCity(request.getCity());

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

        if (coverPhoto != null && !coverPhoto.isEmpty()) {
            // Find existing cover and remove or unset?
            // For now just add as new cover, user can manage photos separately
            // OR strictly replace:
            shop.getPhotos().stream().filter(ShopPhoto::getIsPrimary).forEach(p -> p.setIsPrimary(false));

            String url = supabaseStorageService.uploadImage(coverPhoto, "shops/" + shop.getId() + "/cover");
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

        return shopRepository.save(shop);
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

    // ==========================================
    // MENU CATEGORY MANAGEMENT
    // ==========================================

    @Transactional
    public MenuCategory createMenuCategory(Long shopId, CreateMenuCategoryRequest request, MultipartFile image,
            List<MultipartFile> galleryPhotos) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found"));

        MenuCategory category = new MenuCategory();
        category.setShop(shop);
        category.setName(request.getName());
        category.setNameMm(request.getNameMm());
        category.setNameEn(request.getNameEn());
        category.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);
        category.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        category = menuCategoryRepository.save(category);

        if (image != null && !image.isEmpty()) {
            String url = supabaseStorageService.uploadImage(image,
                    "shops/" + shopId + "/categories/" + category.getId() + "/main");
            category.setImageUrl(url);
            category = menuCategoryRepository.save(category);
        }

        if (galleryPhotos != null && !galleryPhotos.isEmpty()) {
            for (MultipartFile photo : galleryPhotos) {
                if (!photo.isEmpty()) {
                    String url = supabaseStorageService.uploadImage(photo, "shops/" + shopId + "/categories/"
                            + category.getId() + "/gallery/" + System.currentTimeMillis());
                    MenuCategoryPhoto catPhoto = new MenuCategoryPhoto();
                    catPhoto.setCategory(category);
                    catPhoto.setUrl(url);
                    catPhoto.setThumbnailUrl(url);
                    catPhoto.setIsPrimary(false);
                    category.getPhotos().add(catPhoto);
                }
            }
            category = menuCategoryRepository.save(category);
        }

        return category;
    }

    @Transactional
    public MenuCategory updateMenuCategory(Long id, UpdateMenuCategoryRequest request, MultipartFile image,
            List<MultipartFile> galleryPhotos) {
        MenuCategory category = menuCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (request.getName() != null)
            category.setName(request.getName());
        if (request.getNameMm() != null)
            category.setNameMm(request.getNameMm());
        if (request.getNameEn() != null)
            category.setNameEn(request.getNameEn());
        if (request.getDisplayOrder() != null)
            category.setDisplayOrder(request.getDisplayOrder());
        if (request.getIsActive() != null)
            category.setIsActive(request.getIsActive());

        if (image != null && !image.isEmpty()) {
            String url = supabaseStorageService.uploadImage(image,
                    "shops/" + category.getShop().getId() + "/categories/" + category.getId() + "/main");
            category.setImageUrl(url);
        }

        if (galleryPhotos != null && !galleryPhotos.isEmpty()) {
            for (MultipartFile photo : galleryPhotos) {
                if (!photo.isEmpty()) {
                    String url = supabaseStorageService.uploadImage(photo, "shops/" + category.getShop().getId()
                            + "/categories/" + category.getId() + "/gallery/" + System.currentTimeMillis());
                    MenuCategoryPhoto catPhoto = new MenuCategoryPhoto();
                    catPhoto.setCategory(category);
                    catPhoto.setUrl(url);
                    catPhoto.setThumbnailUrl(url);
                    catPhoto.setIsPrimary(false);
                    category.getPhotos().add(catPhoto);
                }
            }
        }

        return menuCategoryRepository.save(category);
    }

    @Transactional
    public void deleteMenuCategory(Long id) {
        MenuCategory category = menuCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        menuCategoryRepository.delete(category);
    }

    // ==========================================
    // MENU ITEM MANAGEMENT
    // ==========================================

    @Transactional
    public MenuItem createMenuItem(Long categoryId, CreateMenuItemRequest request, MultipartFile image,
            List<MultipartFile> galleryPhotos) {
        MenuCategory category = menuCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        MenuItem item = new MenuItem();
        item.setCategory(category);
        item.setShop(category.getShop());
        item.setName(request.getName());
        item.setNameMm(request.getNameMm());
        item.setNameEn(request.getNameEn());
        item.setPrice(request.getPrice());
        item.setCurrency(request.getCurrency() != null ? request.getCurrency() : "MMK");
        item.setIsAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true);
        item.setIsPopular(request.getIsPopular() != null ? request.getIsPopular() : false);
        item.setIsVegetarian(request.getIsVegetarian() != null ? request.getIsVegetarian() : false);
        item.setIsSpicy(request.getIsSpicy() != null ? request.getIsSpicy() : false);
        item.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);

        item = menuItemRepository.save(item);

        if (image != null && !image.isEmpty()) {
            String url = supabaseStorageService.uploadImage(image,
                    "shops/" + category.getShop().getId() + "/items/" + item.getId() + "/main");
            item.setImageUrl(url);
            item = menuItemRepository.save(item);
        }

        if (galleryPhotos != null && !galleryPhotos.isEmpty()) {
            for (MultipartFile photo : galleryPhotos) {
                if (!photo.isEmpty()) {
                    String url = supabaseStorageService.uploadImage(photo, "shops/" + category.getShop().getId()
                            + "/items/" + item.getId() + "/gallery/" + System.currentTimeMillis());
                    MenuItemPhoto itemPhoto = new MenuItemPhoto();
                    itemPhoto.setItem(item);
                    itemPhoto.setUrl(url);
                    itemPhoto.setThumbnailUrl(url);
                    itemPhoto.setIsPrimary(false);
                    item.getPhotos().add(itemPhoto);
                }
            }
            item = menuItemRepository.save(item);
        }

        return item;
    }

    @Transactional
    public MenuItem updateMenuItem(Long id, UpdateMenuItemRequest request, MultipartFile image,
            List<MultipartFile> galleryPhotos) {
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        if (request.getName() != null)
            item.setName(request.getName());
        if (request.getNameMm() != null)
            item.setNameMm(request.getNameMm());
        if (request.getNameEn() != null)
            item.setNameEn(request.getNameEn());
        if (request.getPrice() != null)
            item.setPrice(request.getPrice());
        if (request.getCurrency() != null)
            item.setCurrency(request.getCurrency());
        if (request.getIsAvailable() != null)
            item.setIsAvailable(request.getIsAvailable());
        if (request.getIsPopular() != null)
            item.setIsPopular(request.getIsPopular());
        if (request.getIsVegetarian() != null)
            item.setIsVegetarian(request.getIsVegetarian());
        if (request.getIsSpicy() != null)
            item.setIsSpicy(request.getIsSpicy());
        if (request.getDisplayOrder() != null)
            item.setDisplayOrder(request.getDisplayOrder());

        if (request.getCategoryId() != null) {
            MenuCategory newCategory = menuCategoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("New Category not found"));
            item.setCategory(newCategory);
        }

        if (image != null && !image.isEmpty()) {
            String url = supabaseStorageService.uploadImage(image,
                    "shops/" + item.getShop().getId() + "/items/" + item.getId() + "/main");
            item.setImageUrl(url);
        }

        if (galleryPhotos != null && !galleryPhotos.isEmpty()) {
            for (MultipartFile photo : galleryPhotos) {
                if (!photo.isEmpty()) {
                    String url = supabaseStorageService.uploadImage(photo, "shops/" + item.getShop().getId() + "/items/"
                            + item.getId() + "/gallery/" + System.currentTimeMillis());
                    MenuItemPhoto itemPhoto = new MenuItemPhoto();
                    itemPhoto.setItem(item);
                    itemPhoto.setUrl(url);
                    itemPhoto.setThumbnailUrl(url);
                    itemPhoto.setIsPrimary(false);
                    item.getPhotos().add(itemPhoto);
                }
            }
        }

        return menuItemRepository.save(item);
    }

    @Transactional
    public void deleteMenuItem(Long id) {
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));
        menuItemRepository.delete(item);
    }
}
