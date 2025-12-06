package org.th.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import org.springframework.web.multipart.MultipartFile;
import org.th.entity.shops.*;
import org.th.repository.ShopRepository;

import org.th.entity.UserActivity;
import org.th.repository.UserActivityRepository;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelImportService {

    private final ShopRepository shopRepository;
    private final UserActivityRepository userActivityRepository;

    /**
     * Import shops from Excel file
     */
    public ImportResult importShopsFromExcel(MultipartFile file) throws IOException {
        ImportResult result = new ImportResult();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet shopsSheet = workbook.getSheet("Shops");
            if (shopsSheet == null) {
                throw new IllegalArgumentException("Excel file must contain a 'Shops' sheet");
            }

            // Import shops
            importShops(shopsSheet, result);

            // Import menu items if sheet exists
            Sheet menuSheet = workbook.getSheet("MenuItems");
            if (menuSheet != null) {
                importMenuItems(menuSheet, result);
            }

            // Import operating hours if sheet exists
            Sheet hoursSheet = workbook.getSheet("OperatingHours");
            if (hoursSheet != null) {
                importOperatingHours(hoursSheet, result);
            }

        } catch (Exception e) {
            log.error("Error importing Excel file", e);
            result.addError("Failed to import: " + e.getMessage());
        }

        return result;
    }

    private void importShops(Sheet sheet, ImportResult result) {
        Iterator<Row> rows = sheet.iterator();

        // Skip header row
        if (rows.hasNext()) {
            rows.next();
        }

        int rowNum = 1;
        while (rows.hasNext()) {
            Row row = rows.next();
            rowNum++;

            try {
                Shop shop = parseShopRow(row);

                // Check for duplicate slug
                if (shopRepository.findBySlug(shop.getSlug()) != null) {
                    result.addError("Row " + rowNum + ": Slug '" + shop.getSlug() + "' already exists");
                    continue;
                }

                shopRepository.save(shop);
                result.incrementSuccess();

            } catch (Exception e) {
                result.addError("Row " + rowNum + ": " + e.getMessage());
            }
        }
    }

    private Shop parseShopRow(Row row) {
        Shop shop = new Shop();

        // Mandatory fields
        shop.setName(getCellValueAsString(row.getCell(0))); // name
        shop.setNameMm(getCellValueAsString(row.getCell(1))); // nameMm
        shop.setSlug(getCellValueAsString(row.getCell(2))); // slug
        shop.setCategory(getCellValueAsString(row.getCell(3))); // category
        shop.setLatitude(new BigDecimal(getCellValueAsString(row.getCell(4)))); // latitude
        shop.setLongitude(new BigDecimal(getCellValueAsString(row.getCell(5)))); // longitude
        shop.setAddress(getCellValueAsString(row.getCell(6))); // address

        // Optional fields
        shop.setNameEn(getCellValueAsString(row.getCell(7)));
        shop.setSubCategory(getCellValueAsString(row.getCell(8)));
        shop.setTownship(getCellValueAsString(row.getCell(9)));
        shop.setCity(getCellValueAsString(row.getCell(10), "Yangon"));
        shop.setPhone(getCellValueAsString(row.getCell(11)));
        shop.setEmail(getCellValueAsString(row.getCell(12)));
        shop.setDescription(getCellValueAsString(row.getCell(13)));
        shop.setDescriptionMm(getCellValueAsString(row.getCell(14)));
        shop.setSpecialties(getCellValueAsString(row.getCell(15)));
        shop.setHasDelivery(getCellValueAsBoolean(row.getCell(16)));
        shop.setHasParking(getCellValueAsBoolean(row.getCell(17)));
        shop.setHasWifi(getCellValueAsBoolean(row.getCell(18)));
        shop.setIsVerified(getCellValueAsBoolean(row.getCell(19)));
        shop.setIsActive(getCellValueAsBoolean(row.getCell(20), true));

        // New Column: primaryPhotoUrl (21)
        String primaryPhotoUrl = getCellValueAsString(row.getCell(21));
        if (primaryPhotoUrl != null && !primaryPhotoUrl.isEmpty()) {
            ShopPhoto photo = new ShopPhoto();
            photo.setUrl(primaryPhotoUrl);
            photo.setThumbnailUrl(primaryPhotoUrl); // Use same for thumb
            photo.setIsPrimary(true);
            photo.setPhotoType("cover");
            photo.setShop(shop);
            shop.getPhotos().add(photo);
        }

        // Initialize defaults
        shop.setRatingAvg(BigDecimal.ZERO);
        shop.setRatingCount(0);
        shop.setViewCount(0);
        shop.setTrendingScore(0.0);

        return shop;
    }

    private void importMenuItems(Sheet sheet, ImportResult result) {
        Iterator<Row> rows = sheet.iterator();
        if (rows.hasNext())
            rows.next(); // Skip header

        Map<String, MenuCategory> categories = new HashMap<>();

        while (rows.hasNext()) {
            Row row = rows.next();
            try {
                String shopSlug = getCellValueAsString(row.getCell(0));
                Shop shop = shopRepository.findBySlug(shopSlug);

                if (shop == null) {
                    result.addError("Menu: Shop with slug '" + shopSlug + "' not found");
                    continue;
                }

                String categoryName = getCellValueAsString(row.getCell(1));
                MenuCategory category = getOrCreateCategory(shop, categoryName, categories);

                MenuItem item = new MenuItem();
                item.setShop(shop); // Set the shop explicitly
                item.setCategory(category);
                item.setName(getCellValueAsString(row.getCell(2)));
                item.setPrice(new BigDecimal(getCellValueAsString(row.getCell(3))));
                item.setCurrency(getCellValueAsString(row.getCell(4), "MMK"));
                item.setIsVegetarian(getCellValueAsBoolean(row.getCell(5)));
                item.setIsSpicy(getCellValueAsBoolean(row.getCell(6)));
                item.setIsPopular(getCellValueAsBoolean(row.getCell(7)));

                // New Column: imageUrl (8)
                item.setImageUrl(getCellValueAsString(row.getCell(8)));

                item.setIsAvailable(true);

                category.getItems().add(item);

            } catch (Exception e) {
                result.addError("Menu row: " + e.getMessage());
            }
        }

        // Save all categories
        categories.values().forEach(cat -> shopRepository.save(cat.getShop()));
    }

    private MenuCategory getOrCreateCategory(Shop shop, String catName, Map<String, MenuCategory> cache) {
        String key = shop.getSlug() + ":" + catName;
        return cache.computeIfAbsent(key, k -> {
            MenuCategory cat = new MenuCategory();
            cat.setShop(shop);
            cat.setName(catName);
            cat.setIsActive(true);
            cat.setDisplayOrder(shop.getMenuCategories().size());
            shop.getMenuCategories().add(cat);
            return cat;
        });
    }

    private void importOperatingHours(Sheet sheet, ImportResult result) {
        Iterator<Row> rows = sheet.iterator();
        if (rows.hasNext())
            rows.next(); // Skip header

        while (rows.hasNext()) {
            Row row = rows.next();
            try {
                String shopSlug = getCellValueAsString(row.getCell(0));
                Shop shop = shopRepository.findBySlug(shopSlug);

                if (shop == null) {
                    result.addError("Hours: Shop with slug '" + shopSlug + "' not found");
                    continue;
                }

                OperatingHour hour = new OperatingHour();
                hour.setShop(shop);
                hour.setDayOfWeek(Integer.parseInt(getCellValueAsString(row.getCell(1))));
                hour.setOpeningTime(
                        LocalTime.parse(getCellValueAsString(row.getCell(2)), DateTimeFormatter.ofPattern("HH:mm")));
                hour.setClosingTime(
                        LocalTime.parse(getCellValueAsString(row.getCell(3)), DateTimeFormatter.ofPattern("HH:mm")));
                hour.setIsClosed(getCellValueAsBoolean(row.getCell(4)));

                shop.getOperatingHours().add(hour);
                shopRepository.save(shop);

            } catch (Exception e) {
                result.addError("Hours row: " + e.getMessage());
            }
        }
    }

    // Helper methods
    private String getCellValueAsString(Cell cell) {
        return getCellValueAsString(cell, null);
    }

    private String getCellValueAsString(Cell cell, String defaultValue) {
        if (cell == null)
            return defaultValue;

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> defaultValue;
        };
    }

    private Boolean getCellValueAsBoolean(Cell cell) {
        return getCellValueAsBoolean(cell, false);
    }

    private Boolean getCellValueAsBoolean(Cell cell, Boolean defaultValue) {
        if (cell == null)
            return defaultValue;

        return switch (cell.getCellType()) {
            case BOOLEAN -> cell.getBooleanCellValue();
            case STRING -> "TRUE".equalsIgnoreCase(cell.getStringCellValue());
            default -> defaultValue;
        };
    }

    // Result class
    public static class ImportResult {
        private int successCount = 0;
        private final List<String> errors = new ArrayList<>();

        public void incrementSuccess() {
            successCount++;
        }

        public void addError(String error) {
            errors.add(error);
        }

        public int getSuccessCount() {
            return successCount;
        }

        public List<String> getErrors() {
            return errors;
        }

        public boolean hasErrors() {
            return !errors.isEmpty();
        }
    }

    // User Activity Import
    public ImportResult importUserActivityFromExcel(MultipartFile file) throws IOException {
        ImportResult result = new ImportResult();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0); // Assume first sheet
            Iterator<Row> rows = sheet.iterator();

            // Skip header
            if (rows.hasNext()) {
                rows.next();
            }

            int rowNum = 1;
            while (rows.hasNext()) {
                Row row = rows.next();
                rowNum++;

                try {
                    String deviceId = getCellValueAsString(row.getCell(0));
                    String activityTypeStr = getCellValueAsString(row.getCell(1));
                    String shopSlug = getCellValueAsString(row.getCell(2));
                    String targetName = getCellValueAsString(row.getCell(3));
                    String timestampStr = getCellValueAsString(row.getCell(4));
                    String metadata = getCellValueAsString(row.getCell(5));

                    if (deviceId == null || activityTypeStr == null) {
                        continue; // Skip invalid rows
                    }

                    UserActivity activity = new UserActivity();
                    activity.setDeviceId(deviceId);
                    activity.setActivityType(org.th.entity.enums.ActivityType.valueOf(activityTypeStr));
                    activity.setTargetName(targetName);
                    activity.setMetadata(metadata);

                    // Parse timestamp if present
                    if (timestampStr != null && !timestampStr.isEmpty()) {
                        try {
                            // Try parsing "yyyy-MM-dd HH:mm:ss"
                            activity.setCreatedAt(java.time.LocalDateTime.parse(timestampStr,
                                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                        } catch (Exception e) {
                            // Fallback to now
                            // activity.onCreate(); // Handled by PrePersist but we might want to override
                        }
                    }

                    // Resolve shop ID if slug provided
                    if (shopSlug != null && !shopSlug.isEmpty()) {
                        Shop shop = shopRepository.findBySlug(shopSlug);
                        if (shop != null) {
                            activity.setTargetId(shop.getId());
                            if (targetName == null || targetName.isEmpty()) {
                                activity.setTargetName(shop.getName());
                            }
                        }
                    }

                    userActivityRepository.save(activity);
                    result.incrementSuccess();

                } catch (Exception e) {
                    result.addError("Row " + rowNum + ": " + e.getMessage());
                }
            }

        } catch (Exception e) {
            log.error("Error importing Activity Excel", e);
            result.addError("Critical error: " + e.getMessage());
        }

        return result;
    }
}
