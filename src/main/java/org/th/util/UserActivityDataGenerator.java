package org.th.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * Utility to generate User Activity Excel file for testing algorithms
 */
public class UserActivityDataGenerator {

    public static void main(String[] args) {
        try {
            createActivityFile("user_activity_template.xlsx");
            System.out.println("✅ Activity file created: user_activity_template.xlsx");
        } catch (IOException e) {
            System.err.println("❌ Error creating file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void createActivityFile(String filename) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("UserActivities");

        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {
                "deviceId", "activityType", "shopSlug", "targetName", "timestamp", "metadata"
        };

        CellStyle headerStyle = createHeaderStyle(workbook);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, 5000);
        }

        int row = 1;

        // Define some realistic device IDs
        String[] devices = {
                "device_spicy_lover", // Likes spicy food
                "device_noodle_fan", // Likes noodles
                "device_grocery_shopper", // Buys ingredients
                "device_tourist", // Looks at everything
                "device_local_student" // Cheap eats
        };

        // Define shops by "category" roughly
        String[] spicyShops = { "shwe-htee-restaurant", "a-nyar-tar", "ab-thai-myanmar", "inle-traditional-food" };
        String[] noodleShops = { "inle-traditional-food", "mandalay-food-house", "delicious-democracy",
                "ramkhamhaeng-food" };
        String[] groceryShops = { "bang-bon-mart", "phra-khanong-store-1", "daw-hla-grocery", "shwe-myanmar-grocery",
                "mahachai-seafood" };
        String[] allShops = {
                "inle-traditional-food", "shwe-htee-restaurant", "ayar-house", "kalyana-restaurant",
                "feel-restaurant-bkk", "bagan-myay", "mandalay-food-house", "89-myanmar-foods",
                "a-nyar-tar", "ab-thai-myanmar", "delicious-democracy", "phra-khanong-store-1",
                "daw-hla-grocery", "bang-bon-mart", "ko-thein-shop", "little-myanmar-shop",
                "shwe-myanmar-grocery", "yangon-store-bkk", "mahachai-seafood", "ramkhamhaeng-food"
        };

        // Generate data for each device profile

        // 1. Spicy Lover (Views and clicks spicy shops)
        for (String shop : spicyShops) {
            addActivity(sheet, row++, "device_spicy_lover", "VIEW_SHOP", shop, shop, "2023-10-01 12:00:00",
                    "Simulated View");
            addActivity(sheet, row++, "device_spicy_lover", "CLICK_DIRECTIONS", shop, shop, "2023-10-01 12:05:00",
                    "Simulated Click");
        }
        addActivity(sheet, row++, "device_spicy_lover", "SEARCH_QUERY", "", "Spicy Curry", "2023-10-01 11:50:00",
                "Search for spicy");
        addActivity(sheet, row++, "device_spicy_lover", "VIEW_CATEGORY", "", "Restaurant", "2023-10-01 11:55:00",
                "Browse Category");

        // 2. Noodle Fan
        for (String shop : noodleShops) {
            addActivity(sheet, row++, "device_noodle_fan", "VIEW_SHOP", shop, shop, "2023-10-02 13:00:00",
                    "Simulated View");
            // Re-visits
            addActivity(sheet, row++, "device_noodle_fan", "VIEW_SHOP", shop, shop, "2023-10-03 13:00:00",
                    "Simulated Re-View");
        }
        addActivity(sheet, row++, "device_noodle_fan", "VIEW_CATEGORY", "", "Noodles", "2023-10-02 12:50:00",
                "Browse Category");

        // 3. Grocery Shopper
        for (String shop : groceryShops) {
            addActivity(sheet, row++, "device_grocery_shopper", "VIEW_SHOP", shop, shop, "2023-10-04 10:00:00",
                    "Simulated View");
            addActivity(sheet, row++, "device_grocery_shopper", "CLICK_CALL", shop, shop, "2023-10-04 10:05:00",
                    "Call shop");
        }
        addActivity(sheet, row++, "device_grocery_shopper", "VIEW_CATEGORY", "", "Grocery", "2023-10-04 09:50:00",
                "Browse Category");

        // 4. Tourist (Random browsing)
        Random rand = new Random();
        for (int i = 0; i < 10; i++) {
            String shop = allShops[rand.nextInt(allShops.length)];
            addActivity(sheet, row++, "device_tourist", "VIEW_SHOP", shop, shop, "2023-10-05 14:00:00",
                    "Simulated Random View");
        }

        // 5. Local Student (Cheap eats - mostly noodles and snacks)
        addActivity(sheet, row++, "device_local_student", "VIEW_SHOP", "delicious-democracy", "delicious-democracy",
                "2023-10-06 16:00:00", "View");
        addActivity(sheet, row++, "device_local_student", "VIEW_SHOP", "ramkhamhaeng-food", "ramkhamhaeng-food",
                "2023-10-06 16:10:00", "View");
        addActivity(sheet, row++, "device_local_student", "CLICK_SHARE", "delicious-democracy", "delicious-democracy",
                "2023-10-06 16:15:00", "Share with friends");

        // Write to file
        try (FileOutputStream fileOut = new FileOutputStream(filename)) {
            workbook.write(fileOut);
        }
        workbook.close();
    }

    private static void addActivity(Sheet sheet, int rowNum, String... values) {
        Row row = sheet.createRow(rowNum);
        for (int i = 0; i < values.length; i++) {
            row.createCell(i).setCellValue(values[i]);
        }
    }

    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
}
