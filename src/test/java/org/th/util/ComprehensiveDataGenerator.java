package org.th.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Generates a comprehensive database seed with 100 Myanmar restaurants
 * Fixes all format issues:
 * - Proper district matching with Locations sheet
 * - Integer day numbers for OperatingHours
 * - Correct enum values for UserActivity
 */
public class ComprehensiveDataGenerator {

    private static final String[] BANGKOK_DISTRICTS = {
            "Watthana", "Phra Khanong", "Huai Khwang", "Pathum Wan", "Phaya Thai",
            "Bang Bon", "Ratchathewi", "Din Daeng", "Khlong Toei", "Chatuchak"
    };

    private static final String[] CATEGORIES = { "Restaurant", "Grocery", "Cafe" };
    private static final String[] RESTAURANT_TYPES = {
            "Burmese", "Shan Noodle", "Upper Myanmar Style", "Mandalay Style",
            "Fusion", "Street Food", "Fine Dining", "Casual Dining"
    };

    public static void main(String[] args) throws IOException {
        Workbook workbook = new XSSFWorkbook();

        // Create all sheets
        Sheet locationsSheet = workbook.createSheet("Locations");
        Sheet usersSheet = workbook.createSheet("Users");
        Sheet shopsSheet = workbook.createSheet("Shops");
        Sheet menuItemsSheet = workbook.createSheet("MenuItems");
        Sheet operatingHoursSheet = workbook.createSheet("OperatingHours");
        Sheet reviewsSheet = workbook.createSheet("Reviews");
        Sheet favoritesSheet = workbook.createSheet("Favorites");
        Sheet userActivitySheet = workbook.createSheet("UserActivity");

        // Generate data
        createLocationsSheet(locationsSheet);
        createUsersSheet(usersSheet);
        createShopsSheet(shopsSheet);
        createMenuItemsSheet(menuItemsSheet);
        createOperatingHoursSheet(operatingHoursSheet);
        createReviewsSheet(reviewsSheet);
        createFavoritesSheet(favoritesSheet);
        createUserActivitySheet(userActivitySheet);

        // Write to file
        try (FileOutputStream fileOut = new FileOutputStream("database_seed.xlsx")) {
            workbook.write(fileOut);
        }
        workbook.close();

        System.out.println("✅ Generated database_seed.xlsx with 100 shops!");
    }

    private static void createLocationsSheet(Sheet sheet) {
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("cityNameEn");
        header.createCell(1).setCellValue("districtNameEn");

        int rowNum = 1;
        for (String district : BANGKOK_DISTRICTS) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue("Bangkok");
            row.createCell(1).setCellValue(district);
        }
    }

    private static void createUsersSheet(Sheet sheet) {
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("username");
        header.createCell(1).setCellValue("email");
        header.createCell(2).setCellValue("password");
        header.createCell(3).setCellValue("role");

        // Admin user
        Row row1 = sheet.createRow(1);
        row1.createCell(0).setCellValue("admin");
        row1.createCell(1).setCellValue("admin@mytogether.com");
        row1.createCell(2).setCellValue("admin123");
        row1.createCell(3).setCellValue("ADMIN");

        // Regular users
        for (int i = 1; i <= 10; i++) {
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue("user" + i);
            row.createCell(1).setCellValue("user" + i + "@example.com");
            row.createCell(2).setCellValue("password123");
            row.createCell(3).setCellValue("USER");
        }
    }

    private static void createShopsSheet(Sheet sheet) {
        Row header = sheet.createRow(0);
        String[] headers = { "name", "nameMm", "nameEn", "slug", "category", "subCategory",
                "latitude", "longitude", "address", "shortName", "subCategoryDetail", "district", "city",
                "phone", "email", "description", "descriptionMm", "specialties",
                "hasDelivery", "hasParking", "hasWifi", "isVerified", "isHalal", "primaryPhotoUrl" };

        for (int i = 0; i < headers.length; i++) {
            header.createCell(i).setCellValue(headers[i]);
        }

        Random random = new Random(42);
        int rowNum = 1;

        for (int i = 1; i <= 100; i++) {
            Row row = sheet.createRow(rowNum++);
            String district = BANGKOK_DISTRICTS[i % BANGKOK_DISTRICTS.length];
            String category = i <= 85 ? "Restaurant" : (i <= 95 ? "Grocery" : "Cafe");
            String subCategory = category.equals("Restaurant")
                    ? RESTAURANT_TYPES[random.nextInt(RESTAURANT_TYPES.length)]
                    : (category.equals("Grocery") ? "Market Stall" : "Coffee Shop");

            // Simple consistent naming
            String name = generateShopName(i, subCategory);
            String slug = "myanmar-shop-" + i; // SIMPLE AND CONSISTENT

            double baseLat = 13.7563;
            double baseLng = 100.5018;
            double lat = baseLat + (random.nextDouble() - 0.5) * 0.1;
            double lng = baseLng + (random.nextDouble() - 0.5) * 0.1;

            row.createCell(0).setCellValue(name);
            row.createCell(1).setCellValue("မြန်မာစာ " + i);
            row.createCell(2).setCellValue(name);
            row.createCell(3).setCellValue(slug);
            row.createCell(4).setCellValue(category);
            row.createCell(5).setCellValue(subCategory);
            row.createCell(6).setCellValue(lat);
            row.createCell(7).setCellValue(lng);
            row.createCell(8).setCellValue("Bangkok Address " + i);
            row.createCell(9).setCellValue("Shop" + i);
            row.createCell(10).setCellValue(subCategory + " specialist");
            row.createCell(11).setCellValue(district);
            row.createCell(12).setCellValue("Bangkok");
            row.createCell(13).setCellValue("08" + String.format("%08d", i));
            row.createCell(14).setCellValue("");
            row.createCell(15).setCellValue("Authentic Myanmar " + subCategory);
            row.createCell(16).setCellValue("ဖော်ပြချက်");
            row.createCell(17).setCellValue("Mohinga, Tea Leaf Salad");
            row.createCell(18).setCellValue(random.nextBoolean() ? "TRUE" : "FALSE");
            row.createCell(19).setCellValue(random.nextBoolean() ? "TRUE" : "FALSE");
            row.createCell(20).setCellValue("TRUE");
            row.createCell(21).setCellValue("TRUE");
            row.createCell(22).setCellValue("FALSE");
            row.createCell(23).setCellValue("https://placehold.co/600x400?text=shop" + i);
        }
    }

    private static String generateShopName(int index, String subCategory) {
        String[] prefixes = { "Golden", "Royal", "Shwe", "Myanmar", "Yangon", "Mandalay", "Inle", "Bagan" };
        return prefixes[index % prefixes.length] + " " + subCategory + " House " + index;
    }

    private static void createMenuItemsSheet(Sheet sheet) {
        Row header = sheet.createRow(0);
        String[] headers = { "shopSlug", "categoryName", "itemName", "price", "currency",
                "isVegetarian", "isSpicy", "isPopular", "imageUrl", "nameMm", "nameEn", "photos" };

        for (int i = 0; i < headers.length; i++) {
            header.createCell(i).setCellValue(headers[i]);
        }

        String[] dishes = { "Mohinga", "Tea Leaf Salad", "Shan Noodles", "Coconut Rice",
                "Pork Curry", "Chicken Curry", "Samosa", "Spring Rolls" };
        String[] categories = { "Main Course", "Appetizers", "Noodles", "Rice Dishes", "Salads" };

        int rowNum = 1;
        Random random = new Random(42);

        for (int shopId = 1; shopId <= 100; shopId++) {
            String shopSlug = "myanmar-shop-" + shopId; // MATCHES SHOPS SHEET

            int itemCount = 3 + random.nextInt(3);
            for (int j = 0; j < itemCount; j++) {
                Row row = sheet.createRow(rowNum++);
                String dish = dishes[random.nextInt(dishes.length)];
                int price = 60 + random.nextInt(200);

                row.createCell(0).setCellValue(shopSlug);
                row.createCell(1).setCellValue(categories[random.nextInt(categories.length)]);
                row.createCell(2).setCellValue(dish + " " + (j + 1));
                row.createCell(3).setCellValue(price);
                row.createCell(4).setCellValue("THB");
                row.createCell(5).setCellValue(random.nextBoolean() ? "TRUE" : "FALSE");
                row.createCell(6).setCellValue(random.nextBoolean() ? "TRUE" : "FALSE");
                row.createCell(7).setCellValue(j == 0 ? "TRUE" : "FALSE");
                row.createCell(8).setCellValue("https://placehold.co/400x300?text=" + dish);
                row.createCell(9).setCellValue("မြန်မာ " + dish); // nameMm
                row.createCell(10).setCellValue(dish); // nameEn
                row.createCell(11).setCellValue(""); // photos
            }
        }
    }

    private static void createOperatingHoursSheet(Sheet sheet) {
        Row header = sheet.createRow(0);
        String[] headers = { "shopSlug", "dayOfWeek", "openTime", "closeTime", "isClosed" };

        for (int i = 0; i < headers.length; i++) {
            header.createCell(i).setCellValue(headers[i]);
        }

        int rowNum = 1;

        for (int shopId = 1; shopId <= 100; shopId++) {
            String shopSlug = "myanmar-shop-" + shopId; // MATCHES SHOPS SHEET

            for (int day = 1; day <= 7; day++) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(shopSlug);
                row.createCell(1).setCellValue(day);
                row.createCell(2).setCellValue("09:00");
                row.createCell(3).setCellValue("21:00");
                row.createCell(4).setCellValue("FALSE");
            }
        }
    }

    private static void createReviewsSheet(Sheet sheet) {
        Row header = sheet.createRow(0);
        String[] headers = { "shopSlug", "username", "rating", "comment", "photos", "createdAt" };

        for (int i = 0; i < headers.length; i++) {
            header.createCell(i).setCellValue(headers[i]);
        }

        String[] comments = {
                "Excellent authentic Myanmar food!",
                "Great taste and friendly service",
                "Best Mohinga in Bangkok",
                "Highly recommended!",
                "Love the tea leaf salad"
        };

        int rowNum = 1;
        Random random = new Random(42);

        for (int i = 0; i < 150; i++) {
            int shopId = 1 + random.nextInt(100);
            String shopSlug = "myanmar-shop-" + shopId; // MATCHES SHOPS SHEET

            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(shopSlug);
            row.createCell(1).setCellValue("user" + (1 + random.nextInt(10)));
            row.createCell(2).setCellValue(4 + random.nextInt(2));
            row.createCell(3).setCellValue(comments[random.nextInt(comments.length)]);
            // Photos (Column 4)
            row.createCell(4).setCellValue("https://placehold.co/100x100?text=review" + i);
            // CreatedAt (Column 5)
            row.createCell(5).setCellValue(LocalDateTime.now().minusDays(random.nextInt(30))
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
    }

    private static void createFavoritesSheet(Sheet sheet) {
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("username");
        header.createCell(1).setCellValue("shopSlug");

        int rowNum = 1;
        Random random = new Random(42);

        for (int i = 0; i < 100; i++) {
            int shopId = 1 + random.nextInt(100);
            String shopSlug = "myanmar-shop-" + shopId; // MATCHES SHOPS SHEET

            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue("user" + (1 + random.nextInt(10)));
            row.createCell(1).setCellValue(shopSlug);
        }
    }

    private static void createUserActivitySheet(Sheet sheet) {
        Row header = sheet.createRow(0);
        String[] headers = { "username", "activityType", "targetId", "metadata", "createdAt" };

        for (int i = 0; i < headers.length; i++) {
            header.createCell(i).setCellValue(headers[i]);
        }

        // FIXED: Use actual enum values from ActivityType.java
        String[] activityTypes = { "VIEW_SHOP", "SEARCH_QUERY", "VIEW_CATEGORY", "VIEW_NEARBY" };
        int rowNum = 1;
        Random random = new Random(42);

        for (int i = 0; i < 200; i++) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue("user" + (1 + random.nextInt(10)));
            row.createCell(1).setCellValue(activityTypes[random.nextInt(activityTypes.length)]);
            row.createCell(2).setCellValue(String.valueOf(1 + random.nextInt(100)));
            row.createCell(3).setCellValue("{}");
            row.createCell(4).setCellValue(LocalDateTime.now().minusDays(random.nextInt(30))
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
    }
}
