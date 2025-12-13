package org.th.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;

public class AdminExcelTemplateGenerator {

    public static void main(String[] args) {
        try (Workbook workbook = new XSSFWorkbook()) {

            // ==========================================
            // SHEET 1: Shops
            // ==========================================
            Sheet shopsSheet = workbook.createSheet("Shops");
            createHeader(shopsSheet, new String[] {
                    "Name", "Name (MM)", "Slug", "Category", "Latitude", "Longitude", "Address",
                    "Name (EN)", "Sub Category", "Township", "City", "Phone", "Email",
                    "Description", "Description (MM)", "Specialties", "Delivery", "Parking", "Wifi",
                    "Verified", "Active", "Cover Photo", "Address (MM)", "Price Pref", "Halal",
                    "Vegetarian", "Gallery Photos"
            });

            // Sample Data Row 1 (Burma Bistro)
            createRow(shopsSheet, 1, new Object[] {
                    "Burma Bistro", "မြန်မာဘစ်စထရို", "burma-bistro", "Restaurant", 16.8409, 96.1735,
                    "No. 123, Wayzayandar Rd, Bahan",
                    "Burma Bistro", "Asian Fusion", "Bahan", "Yangon", "09450001122", "info@burmabistro.com",
                    "A modern take on traditional Burmese cuisine.",
                    "ရိုးရာမြန်မာအစားအစာများကို ခေတ်မီပုံစံဖြင့် တည်ခင်းဧည့်ခံပါသည်။", "Mohinga, Tea Leaf Salad",
                    true, true, true, true, true,
                    "https://example.com/cover.jpg", "အမှတ် ၁၂၃, ဝေဇယန္တာလမ်း, ဗဟန်း", "MODERATE", false, false,
                    "https://example.com/gallery1.jpg, https://example.com/gallery2.jpg"
            });

            // Sample Data Row 2 (Tea House)
            createRow(shopsSheet, 2, new Object[] {
                    "Rangoon Tea House", "ရန်ကုန်လက်ဖက်ရည်ဆိုင်", "rangoon-tea-house", "Tea House", 16.7745, 96.1589,
                    "77-79 Pansodan St, Kyauktada",
                    "Rangoon Tea House", "Traditional", "Kyauktada", "Yangon", "09788889999", "contact@rth.com",
                    "Famous tea house serving classic dishes.", "နာမည်ကြီး လက်ဖက်ရည်ဆိုင်။", "Tea, Nan Gyi Thoke",
                    true, false, true, true, true,
                    "https://example.com/rth-cover.jpg", "၇၇-၇၉ ပန်းဆိုးတန်းလမ်း, ကျောက်တံတား", "EXPENSIVE", true, true,
                    "https://example.com/rth1.jpg"
            });

            // Auto-size columns
            for (int i = 0; i < 27; i++) {
                shopsSheet.autoSizeColumn(i);
            }

            // ==========================================
            // SHEET 2: MenuItems
            // ==========================================
            Sheet menuSheet = workbook.createSheet("MenuItems");
            createHeader(menuSheet, new String[] {
                    "Shop Slug", "Category", "Item Name", "Price", "Currency",
                    "Vegetarian", "Spicy", "Popular", "Image URL", "Name (MM)", "Name (EN)"
            });

            // Sample Menu Items
            createRow(menuSheet, 1, new Object[] {
                    "burma-bistro", "Mains", "Tea Leaf Salad", 4500, "MMK",
                    true, false, true, "https://example.com/salad.jpg", "လက်ဖက်သုပ်", "Tea Leaf Salad"
            });
            createRow(menuSheet, 2, new Object[] {
                    "burma-bistro", "Mains", "Mohinga", 3500, "MMK",
                    false, false, true, "https://example.com/mohinga.jpg", "မုန့်ဟင်းခါး", "Mohinga"
            });
            createRow(menuSheet, 3, new Object[] {
                    "rangoon-tea-house", "Drinks", "Burmese Tea", 1500, "MMK",
                    true, false, true, "https://example.com/tea.jpg", "မြန်မာလက်ဖက်ရည်", "Burmese Tea"
            });

            for (int i = 0; i < 11; i++) {
                menuSheet.autoSizeColumn(i);
            }

            // ==========================================
            // SHEET 3: OperatingHours
            // ==========================================
            Sheet hoursSheet = workbook.createSheet("OperatingHours");
            createHeader(hoursSheet, new String[] {
                    "Shop Slug", "Day of Week", "Open Time", "Close Time", "Is Closed"
            });

            // Sample Hours (Mon-Sun for Burma Bistro)
            for (int i = 1; i <= 7; i++) {
                createRow(hoursSheet, i, new Object[] {
                        "burma-bistro", i, "09:00", "22:00", false
                });
            }

            // ==========================================
            // SHEET 4: UserActivity (Sample)
            // ==========================================
            Sheet activitySheet = workbook.createSheet("UserActivity");
            createHeader(activitySheet, new String[] {
                    "Device ID", "Activity Type", "Shop Slug", "Target Name", "Timestamp", "Metadata"
            });
            createRow(activitySheet, 1, new Object[] {
                    "device-123", "VIEW_SHOP", "burma-bistro", "Burma Bistro", "2024-05-20 10:30:00", "{}"
            });

            // Write File
            try (FileOutputStream fileOut = new FileOutputStream("admin_import_sample.xlsx")) {
                workbook.write(fileOut);
                System.out.println("Excel file generated successfully: admin_import_sample.xlsx");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createHeader(Sheet sheet, String[] headers) {
        Row headerRow = sheet.createRow(0);
        CellStyle style = sheet.getWorkbook().createCellStyle();
        Font font = sheet.getWorkbook().createFont();
        font.setBold(true);
        style.setFont(font);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
        }
    }

    private static void createRow(Sheet sheet, int rowNum, Object[] data) {
        Row row = sheet.createRow(rowNum);
        for (int i = 0; i < data.length; i++) {
            Cell cell = row.createCell(i);
            if (data[i] instanceof String) {
                cell.setCellValue((String) data[i]);
            } else if (data[i] instanceof Double) {
                cell.setCellValue((Double) data[i]);
            } else if (data[i] instanceof Integer) {
                cell.setCellValue((Integer) data[i]);
            } else if (data[i] instanceof Boolean) {
                cell.setCellValue((Boolean) data[i]);
            }
        }
    }
}
