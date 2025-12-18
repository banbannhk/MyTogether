package org.th.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Adds Locations, Users, Reviews, Favorites, and UserActivity sheets to
 * database_seed.xlsx
 */
public class DatabaseSeedCompleter {

    public static void main(String[] args) {
        try {
            String filename = "database_seed.xlsx";

            // Load existing workbook
            Workbook workbook;
            try (FileInputStream fis = new FileInputStream(filename)) {
                workbook = new XSSFWorkbook(fis);
            }

            // Add missing sheets
            addLocationsSheet(workbook);
            addUsersSheet(workbook);
            addReviewsSheet(workbook);
            addFavoritesSheet(workbook);
            addUserActivitySheet(workbook);

            // Save
            try (FileOutputStream fileOut = new FileOutputStream(filename)) {
                workbook.write(fileOut);
            }
            workbook.close();

            System.out.println("✅ Database seed completed with all sheets!");
        } catch (IOException e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void addLocationsSheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Locations");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("cityNameEn");
        header.createCell(1).setCellValue("cityNameMm");
        header.createCell(2).setCellValue("districtNameEn");
        header.createCell(3).setCellValue("districtNameMm");
        header.createCell(4).setCellValue("latitude");
        header.createCell(5).setCellValue("longitude");

        String[][] districts = {
                { "Bangkok", "ဘန်ကောက်", "Watthana", "ဝသ်ထနာ", "13.7400", "100.5700" },
                { "Bangkok", "ဘန်ကောက်", "Khlong Toei", "ခလုံးတိုင်", "13.7200", "100.5900" },
                { "Bangkok", "ဘန်ကောက်", "Phra Khanong", "ဖရာခနောင်", "13.7018", "100.6015" },
                { "Bangkok", "ဘန်ကောက်", "Ratchathewi", "ရာဇသေဝီ", "13.7500", "100.5300" },
                { "Bangkok", "ဘန်ကောက်", "Pathum Wan", "ပသုမ်ဝမ်", "13.7450", "100.5350" },
                { "Bangkok", "ဘန်ကောက်", "Huai Khwang", "ဟွိုင်ခွမ်", "13.7780", "100.5750" },
                { "Bangkok", "ဘန်ကောက်", "Phaya Thai", "ဖယာထိုင်", "13.7810", "100.5410" },
                { "Bangkok", "ဘန်ကောက်", "Bang Rak", "ဘန်ရက်", "13.7240", "100.5210" },
                { "Bangkok", "ဘန်ကောက်", "Sathorn", "စထောန်", "13.7200", "100.5340" },
                { "Bangkok", "ဘန်ကောက်", "Silom", "စစလုံ", "13.7264", "100.5278" }
        };

        int row = 1;
        for (String[] d : districts) {
            Row r = sheet.createRow(row++);
            for (int i = 0; i < d.length; i++) {
                r.createCell(i).setCellValue(d[i]);
            }
        }
    }

    private static void addUsersSheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Users");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("username");
        header.createCell(1).setCellValue("email");
        header.createCell(2).setCellValue("password");
        header.createCell(3).setCellValue("fullName");
        header.createCell(4).setCellValue("role");

        String[][] users = {
                { "admin", "admin@mytogether.com", "admin123", "Admin User", "ADMIN" },
                { "aung_kyaw", "aung@example.com", "password123", "Aung Kyaw", "USER" },
                { "zaw_min", "zaw@example.com", "password123", "Zaw Min", "USER" },
                { "thida_san", "thida@example.com", "password123", "Thida San", "USER" },
                { "myat_thu", "myat@example.com", "password123", "Myat Thu", "USER" }
        };

        int row = 1;
        for (String[] u : users) {
            Row r = sheet.createRow(row++);
            for (int i = 0; i < u.length; i++) {
                r.createCell(i).setCellValue(u[i]);
            }
        }
    }

    private static void addReviewsSheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Reviews");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("shopSlug");
        header.createCell(1).setCellValue("username");
        header.createCell(2).setCellValue("rating");
        header.createCell(3).setCellValue("comment");
        header.createCell(4).setCellValue("photos");

        String[][] reviews = {
                { "inle-traditional-food", "aung_kyaw", "5", "Authentic Shan noodles! Best in Bangkok!", "" },
                { "shwe-htee-restaurant", "zaw_min", "5", "Love the tea leaf salad and curries", "" },
                { "ayar-house", "thida_san", "4", "Great atmosphere, good food", "" },
                { "kalyana-restaurant", "myat_thu", "5", "Weekly live music is amazing!", "" },
                { "inle-traditional-food", "thida_san", "4", "Good tofu nway, a bit pricey", "" }
        };

        int row = 1;
        for (String[] rev : reviews) {
            Row r = sheet.createRow(row++);
            for (int i = 0; i < rev.length; i++) {
                r.createCell(i).setCellValue(rev[i]);
            }
        }
    }

    private static void addFavoritesSheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Favorites");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("username");
        header.createCell(1).setCellValue("shopSlug");

        String[][] favs = {
                { "aung_kyaw", "inle-traditional-food" },
                { "aung_kyaw", "shwe-htee-restaurant" },
                { "zaw_min", "kalyana-restaurant" },
                { "thida_san", "ayar-house" },
                { "myat_thu", "shwe-htee-restaurant" }
        };

        int row = 1;
        for (String[] fav : favs) {
            Row r = sheet.createRow(row++);
            r.createCell(0).setCellValue(fav[0]);
            r.createCell(1).setCellValue(fav[1]);
        }
    }

    private static void addUserActivitySheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet("UserActivity");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("username");
        header.createCell(1).setCellValue("shopSlug");
        header.createCell(2).setCellValue("activityType");
        header.createCell(3).setCellValue("searchQuery");
        header.createCell(4).setCellValue("targetName");

        String[][] activities = {
                { "aung_kyaw", "inle-traditional-food", "VIEW_SHOP", "", "" },
                { "aung_kyaw", "shwe-htee-restaurant", "VIEW_SHOP", "", "" },
                { "zaw_min", "", "SEARCH_QUERY", "myanmar food", "" },
                { "thida_san", "ayar-house", "VIEW_SHOP", "", "" },
                { "myat_thu", "kalyana-restaurant", "VIEW_SHOP", "", "" }
        };

        int row = 1;
        for (String[] act : activities) {
            Row r = sheet.createRow(row++);
            for (int i = 0; i < act.length; i++) {
                r.createCell(i).setCellValue(act[i]);
            }
        }
    }
}
