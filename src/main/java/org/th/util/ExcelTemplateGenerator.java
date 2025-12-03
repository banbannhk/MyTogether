package org.th.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Utility to generate Excel import template
 * Run this class to create shops_template.xlsx
 */
public class ExcelTemplateGenerator {

    public static void main(String[] args) {
        try {
            createTemplate("shops_template.xlsx");
            System.out.println("✅ Template created: shops_template.xlsx");
        } catch (IOException e) {
            System.err.println("❌ Error creating template: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void createTemplate(String filename) throws IOException {
        Workbook workbook = new XSSFWorkbook();

        // Create Shops sheet
        createShopsSheet(workbook);

        // Create MenuItems sheet
        createMenuItemsSheet(workbook);

        // Create OperatingHours sheet
        createOperatingHoursSheet(workbook);

        // Write to file
        try (FileOutputStream fileOut = new FileOutputStream(filename)) {
            workbook.write(fileOut);
        }
        workbook.close();
    }

    private static void createShopsSheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Shops");

        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {
                "name", "nameMm", "slug", "category", "latitude", "longitude", "address",
                "nameEn", "subCategory", "township", "city", "phone", "email",
                "description", "descriptionMm", "specialties",
                "hasDelivery", "hasParking", "hasWifi", "isVerified", "isActive"
        };

        CellStyle headerStyle = createHeaderStyle(workbook);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, 4000);
        }

        // Add sample data rows
        addShopSample(sheet, 1, "Golden Palace Restaurant", "ရွှေနန်းတော် စားသောက်ဆိုင်",
                "golden-palace-restaurant", "Restaurant", "16.7967", "96.1610",
                "123 Main Street, Yangon", "Golden Palace", "Fine Dining", "Sanchaung",
                "Yangon", "+95-1-234567", "info@goldenpalace.com",
                "Best restaurant in Yangon", "ရန်ကုန်တွင် အကောင်းဆုံး စားသောက်ဆိုင်",
                "Seafood, BBQ, Traditional", "TRUE", "TRUE", "TRUE", "TRUE", "TRUE");

        addShopSample(sheet, 2, "Shwe Myanmar Cafe", "ရွှေမြန်မာ ကော်ဖီဆိုင်",
                "shwe-myanmar-cafe", "Cafe", "16.8051", "96.1537",
                "456 Coffee Lane, Yangon", "Shwe Myanmar", "Coffee Shop", "Kamaryut",
                "Yangon", "+95-1-345678", "contact@shwemyanmar.com",
                "Cozy cafe with great coffee", "ကော်ဖီကောင်းတဲ့ နွေးထွေးတဲ့ဆိုင်",
                "Coffee, Pastries, Breakfast", "FALSE", "TRUE", "TRUE", "FALSE", "TRUE");
    }

    private static void addShopSample(Sheet sheet, int rowNum, String... values) {
        Row row = sheet.createRow(rowNum);
        for (int i = 0; i < values.length; i++) {
            Cell cell = row.createCell(i);
            // Check if it's a boolean or number
            if (i >= 16 && i <= 20) { // Boolean columns
                cell.setCellValue(values[i]);
            } else if (i == 4 || i == 5) { // Lat/Lng columns
                cell.setCellValue(values[i]);
            } else {
                cell.setCellValue(values[i]);
            }
        }
    }

    private static void createMenuItemsSheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet("MenuItems");

        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {
                "shopSlug", "categoryName", "itemName", "price", "currency",
                "isVegetarian", "isSpicy", "isPopular"
        };

        CellStyle headerStyle = createHeaderStyle(workbook);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, 4000);
        }

        // Add sample data
        addMenuSample(sheet, 1, "golden-palace-restaurant", "Main Dishes", "Grilled Fish", "15000", "MMK", "FALSE",
                "TRUE", "TRUE");
        addMenuSample(sheet, 2, "golden-palace-restaurant", "Main Dishes", "Chicken Curry", "12000", "MMK", "FALSE",
                "TRUE", "TRUE");
        addMenuSample(sheet, 3, "golden-palace-restaurant", "Appetizers", "Spring Rolls", "5000", "MMK", "TRUE",
                "FALSE", "TRUE");
        addMenuSample(sheet, 4, "shwe-myanmar-cafe", "Beverages", "Cappuccino", "4000", "MMK", "TRUE", "FALSE", "TRUE");
        addMenuSample(sheet, 5, "shwe-myanmar-cafe", "Pastries", "Croissant", "3000", "MMK", "TRUE", "FALSE", "FALSE");
    }

    private static void addMenuSample(Sheet sheet, int rowNum, String... values) {
        Row row = sheet.createRow(rowNum);
        for (int i = 0; i < values.length; i++) {
            row.createCell(i).setCellValue(values[i]);
        }
    }

    private static void createOperatingHoursSheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet("OperatingHours");

        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {
                "shopSlug", "dayOfWeek", "openingTime", "closingTime", "isClosed"
        };

        CellStyle headerStyle = createHeaderStyle(workbook);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, 4000);
        }

        // Add sample data (Monday-Friday for golden-palace-restaurant)
        String slug = "golden-palace-restaurant";
        for (int day = 1; day <= 5; day++) {
            Row row = sheet.createRow(day);
            row.createCell(0).setCellValue(slug);
            row.createCell(1).setCellValue(day);
            row.createCell(2).setCellValue("09:00");
            row.createCell(3).setCellValue("21:00");
            row.createCell(4).setCellValue("FALSE");
        }

        // Add Sunday (closed)
        Row sundayRow = sheet.createRow(6);
        sundayRow.createCell(0).setCellValue(slug);
        sundayRow.createCell(1).setCellValue("0");
        sundayRow.createCell(2).setCellValue("00:00");
        sundayRow.createCell(3).setCellValue("00:00");
        sundayRow.createCell(4).setCellValue("TRUE");
    }

    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
}
