package org.th.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Utility to generate Excel import template
 * Updated to include 20 Myanmar shops in Bangkok with full Menu and Hours data
 */
public class ExcelTemplateGenerator {

        public static void main(String[] args) {
                try {
                        createTemplate("shops_template.xlsx");
                        System.out.println(
                                        "✅ Template created: shops_template.xlsx with 20 Myanmar shops and full details");
                } catch (IOException e) {
                        System.err.println("❌ Error creating template: " + e.getMessage());
                        e.printStackTrace();
                }
        }

        public static void createTemplate(String filename) throws IOException {
                Workbook workbook = new XSSFWorkbook();

                // Create Sheets
                createShopsSheet(workbook);
                createMenuItemsSheet(workbook);
                createOperatingHoursSheet(workbook);

                // Write to file
                try (FileOutputStream fileOut = new FileOutputStream(filename)) {
                        workbook.write(fileOut);
                }
                workbook.close();
        }

        // Helper to get image URL
        private static String getImageUrl(String slug, String type) {
                return "https://placehold.co/600x400?text=" + slug + "+" + type;
        }

        private static void createShopsSheet(Workbook workbook) {
                Sheet sheet = workbook.createSheet("Shops");

                // Create header row
                Row headerRow = sheet.createRow(0);
                String[] headers = {
                                "name", "nameMm", "slug", "category", "latitude", "longitude", "address",
                                "nameEn", "subCategory", "district", "city", "phone", "email",
                                "description", "descriptionMm", "specialties",
                                "hasDelivery", "hasParking", "hasWifi", "isVerified", "isActive", "primaryPhotoUrl"
                };

                CellStyle headerStyle = createHeaderStyle(workbook);
                for (int i = 0; i < headers.length; i++) {
                        Cell cell = headerRow.createCell(i);
                        cell.setCellValue(headers[i]);
                        cell.setCellStyle(headerStyle);
                        sheet.setColumnWidth(i, 4000);
                }

                int row = 1;

                // 1. Inle Traditional Food & Shan Noodle
                addShopSample(sheet, row++, "Inle Traditional Food & Shan Noodle", "အင်းလေး ရှမ်းခေါက်ဆွဲ",
                                "inle-traditional-food", "Restaurant", "13.7795", "100.5750",
                                "11/5 Pracha Rat Bamphen 11 Alley, Huai Khwang, Bangkok 10310", "Inle Traditional Food",
                                "Shan Noodle", "Huai Khwang",
                                "Bangkok", "02-123-4567", "",
                                "Authentic Shan noodles and Inle style food",
                                "စစ်မှန်သော ရှမ်းခေါက်ဆွဲ နှင့် အင်းလေးအစားအစာ",
                                "Shan Noodle, Tofu Nway", "TRUE", "FALSE", "TRUE", "TRUE", "TRUE",
                                getImageUrl("inle", "shop"));

                // 2. Shwe Htee Restaurant
                addShopSample(sheet, row++, "Shwe Htee Restaurant", "ရွှေထီး စားသောက်ဆိုင်",
                                "shwe-htee-restaurant", "Restaurant", "13.7420", "100.5620",
                                "Ocean Tower 2, Sukhumvit 21 Soi 3, Watthana, Bangkok 10110", "Shwe Htee", "Burmese",
                                "Watthana",
                                "Bangkok", "02-234-5678", "",
                                "Popular for lunch and traditional curries", "နေ့လည်စာအတွက် လူကြိုက်များသော ဆိုင်",
                                "Curry, Tea Leaf Salad", "TRUE", "TRUE", "TRUE", "TRUE", "TRUE",
                                getImageUrl("shwe-htee", "shop"));

                // 3. AYAR House Bar & Restaurant
                addShopSample(sheet, row++, "AYAR House Bar & Restaurant", "ဧရာ ဟောက်စ်",
                                "ayar-house", "Restaurant", "13.7450", "100.5400",
                                "35, 2 Ratchaprasong Road, Pathum Wan, Bangkok 10330", "AYAR House", "Bar & Grill",
                                "Pathum Wan",
                                "Bangkok", "02-345-6789", "",
                                "Nice atmosphere with Burmese classics", "လေကောင်းလေသန့် ရရှိနိုင်သော ဆိုင်",
                                "BBQ, Beer, Mohinga", "FALSE", "TRUE", "TRUE", "TRUE", "TRUE",
                                getImageUrl("ayar", "shop"));

                // 4. KALYANA Restaurant
                addShopSample(sheet, row++, "KALYANA Restaurant", "ကလျာဏ စားသောက်ဆိုင်",
                                "kalyana-restaurant", "Restaurant", "13.7540", "100.5420",
                                "110 Ratchararob Road, Ratchathewi, Bangkok 10400", "KALYANA", "Fine Dining",
                                "Ratchathewi",
                                "Bangkok", "02-456-7890", "",
                                "Clean and high-quality Burmese food", "သန့်ရှင်းပြီး အရည်အသွေးမြင့် မြန်မာအစားအစာ",
                                "Danbauk, Curry", "TRUE", "FALSE", "TRUE", "TRUE", "TRUE",
                                getImageUrl("kalyana", "shop"));

                // 5. Feel Restaurant (Ratchadamri)
                addShopSample(sheet, row++, "Feel Restaurant", "ဖီးလ် စားသောက်ဆိုင်",
                                "feel-restaurant-bkk", "Restaurant", "13.7500", "100.5390",
                                "Soi Lumphini 1, Ratchadamri Road, Bangkok", "Feel", "Chain Restaurant", "Pathum Wan",
                                "Bangkok", "02-567-8901", "info@feel.com.mm",
                                "Famous chain from Myanmar", "မြန်မာနိုင်ငံမှ နာမည်ကြီး ဆိုင်ခွဲ",
                                "Mohinga, Breakfast", "TRUE", "TRUE", "TRUE", "TRUE", "TRUE",
                                getImageUrl("feel", "shop"));

                // 6. Bagan Myay
                addShopSample(sheet, row++, "Bagan Myay", "ပုဂံမြေ",
                                "bagan-myay", "Restaurant", "13.7510", "100.5380",
                                "Phetchaburi Road, near Pantip Plaza, Bangkok", "Bagan Myay", "Casual Dining",
                                "Ratchathewi",
                                "Bangkok", "081-234-5678", "",
                                "Convenient spot near shopping malls", "ဈေးဝယ်စင်တာများအနီး အဆင်ပြေသော နေရာ",
                                "Fried Rice, Noodles", "FALSE", "FALSE", "TRUE", "FALSE", "TRUE",
                                getImageUrl("bagan", "shop"));

                // 7. Mandalay Food House
                addShopSample(sheet, row++, "Mandalay Food House", "မန္တလေး အစားအစာ",
                                "mandalay-food-house", "Restaurant", "13.7520", "100.5370",
                                "Soi Phetchaburi 15, Bangkok", "Mandalay Food", "Mandalay Style", "Ratchathewi",
                                "Bangkok", "089-876-5432", "",
                                "Specializing in Mandalay dishes", "မန္တလေး အစားအစာများ အထူးပြု",
                                "Mee Shay, Mandalay Noodles", "TRUE", "FALSE", "FALSE", "TRUE", "TRUE",
                                getImageUrl("mandalay", "shop"));

                // 8. 89 Myanmar Foods
                addShopSample(sheet, row++, "89 Myanmar Foods", "၈၉ မြန်မာ အစားအစာ",
                                "89-myanmar-foods", "Grocery", "13.7150", "100.5960",
                                "D-16, 1599 Sukhumvit Rd, Phra Khanong, Bangkok 10110", "89 Foods", "Grocery Store",
                                "Watthana",
                                "Bangkok", "082-345-6789", "",
                                "Wide variety of Myanmar snacks and ingredients",
                                "မြန်မာ မုန့်နှင့် ဟင်းချက်စရာ စုံလင်",
                                "Tea leaves, Pickles", "TRUE", "TRUE", "TRUE", "TRUE", "TRUE",
                                getImageUrl("89", "shop"));

                // 9. A Nyar Tar
                addShopSample(sheet, row++, "A Nyar Tar", "အညာသား",
                                "a-nyar-tar", "Restaurant", "13.7200", "100.5800",
                                "10/16 Soi Si Thana Sakun Chai, Khlong Tan Nuea, Bangkok 10110", "A Nyar Tar",
                                "Upper Myanmar Style", "Watthana",
                                "Bangkok", "083-456-7890", "",
                                "Upper Myanmar cuisine", "အညာဒေသ အစားအစာများ",
                                "Fermented Bean Paste", "TRUE", "FALSE", "TRUE", "FALSE", "TRUE",
                                getImageUrl("anyartar", "shop"));

                // 10. AB Thai Myanmar Restaurant
                addShopSample(sheet, row++, "AB Thai Myanmar Restaurant", "အေဘီ ထိုင်းမြန်မာ",
                                "ab-thai-myanmar", "Restaurant", "13.7130", "100.5940",
                                "8/11-8/12, 71 Sukhumvit Road, Phra Khanong, Bangkok 10110", "AB Thai", "Fusion",
                                "Phra Khanong",
                                "Bangkok", "084-567-8901", "",
                                "Thai and Myanmar dishes", "ထိုင်း နှင့် မြန်မာ အစားအစာ",
                                "Tom Yum, Mohinga", "TRUE", "TRUE", "TRUE", "TRUE", "TRUE",
                                getImageUrl("abthai", "shop"));

                // 11. Delicious Democracy Noodle
                addShopSample(sheet, row++, "Delicious Democracy Noodle", "ဒီမိုကရေစီ ခေါက်ဆွဲ",
                                "delicious-democracy", "Restaurant", "13.7330", "100.5290",
                                "Near Samyan MRT, Bangkok", "Democracy Noodle", "Noodle Shop", "Pathum Wan",
                                "Bangkok", "085-678-9012", "",
                                "Popular spot for students and locals", "ကျောင်းသားများနှင့် ဒေသခံများအကြား နာမည်ကြီး",
                                "Noodles, Salads", "TRUE", "FALSE", "TRUE", "TRUE", "TRUE",
                                getImageUrl("democracy", "shop"));

                // 12. Phra Khanong Market Grocery 1
                addShopSample(sheet, row++, "Phra Khanong Myanmar Store", "ဖရာခနောင် မြန်မာဆိုင် ၁",
                                "phra-khanong-store-1", "Grocery", "13.7142", "100.5951",
                                "Phra Khanong Market, Bangkok", "PK Market 1", "Market Stall", "Phra Khanong",
                                "Bangkok", "090-111-2222", "",
                                "Fresh vegetables and ingredients from Myanmar",
                                "မြန်မာပြည်မှ လတ်ဆတ်သော ဟင်းသီးဟင်းရွက်များ",
                                "Pennywort, Roselle", "FALSE", "FALSE", "FALSE", "FALSE", "TRUE",
                                getImageUrl("pk1", "shop"));

                // 13. Phra Khanong Market Grocery 2
                addShopSample(sheet, row++, "Daw Hla Grocery", "ဒေါ်လှ ကုန်စုံဆိုင်",
                                "daw-hla-grocery", "Grocery", "13.7143", "100.5952",
                                "Phra Khanong Market, Bangkok", "Daw Hla", "Market Stall", "Phra Khanong",
                                "Bangkok", "090-222-3333", "",
                                "Spices and dried goods", "ဟင်းခတ်အမွှေးအကြိုင် နှင့် အခြောက်အခြမ်း",
                                "Dried Fish, Shrimp Paste", "FALSE", "FALSE", "FALSE", "FALSE", "TRUE",
                                getImageUrl("dawhla", "shop"));

                // 14. Bang Bon Market Store 1
                addShopSample(sheet, row++, "Bang Bon Myanmar Mart", "ဘန်ဘွန် မြန်မာ မတ်",
                                "bang-bon-mart", "Grocery", "13.6706", "100.4183",
                                "Bang Bon Market, Ekkachai Road, Bangkok", "Bang Bon Mart", "Market Stall", "Bang Bon",
                                "Bangkok", "091-333-4444", "",
                                "Everything from Myanmar", "မြန်မာပြည်မှ ကုန်ပစ္စည်းစုံ",
                                "Thanaka, Longyi", "FALSE", "TRUE", "FALSE", "FALSE", "TRUE",
                                getImageUrl("bangbon", "shop"));

                // 15. Bang Bon Market Store 2
                addShopSample(sheet, row++, "Ko Thein Shop", "ကိုသိန်း ဆိုင်",
                                "ko-thein-shop", "Grocery", "13.6707", "100.4184",
                                "Bang Bon Market, Ekkachai Road, Bangkok", "Ko Thein", "Market Stall", "Bang Bon",
                                "Bangkok", "091-444-5555", "",
                                "Burmese tea and snacks", "လက်ဖက်ရည် နှင့် မုန့်များ",
                                "Tea Mix, Lahpet", "FALSE", "TRUE", "FALSE", "FALSE", "TRUE",
                                getImageUrl("kothein", "shop"));

                // 16. Little Myanmar Shop
                addShopSample(sheet, row++, "Little Myanmar Shop", "လစ်တဲလ် မြန်မာ",
                                "little-myanmar-shop", "Grocery", "13.7160", "100.5970",
                                "Sukhumvit 71, Phra Khanong, Bangkok", "Little Myanmar", "Convenience Store",
                                "Phra Khanong",
                                "Bangkok", "092-555-6666", "",
                                "Convenient store for Myanmar items", "မြန်မာပစ္စည်းများ ဝယ်ယူရန် အဆင်ပြေသော ဆိုင်",
                                "Cosmetics, Medicine", "TRUE", "FALSE", "TRUE", "FALSE", "TRUE",
                                getImageUrl("little", "shop"));

                // 17. Shwe Myanmar Grocery
                addShopSample(sheet, row++, "Shwe Myanmar Grocery", "ရွှေမြန်မာ ကုန်စုံဆိုင်",
                                "shwe-myanmar-grocery", "Grocery", "13.7550", "100.5450",
                                "Pratunam Area, Bangkok", "Shwe Myanmar", "Grocery", "Ratchathewi",
                                "Bangkok", "093-666-7777", "",
                                "Imported goods from Yangon", "ရန်ကုန်မှ တိုက်ရိုက်တင်သွင်းသော ပစ္စည်းများ",
                                "Clothes, Food", "TRUE", "FALSE", "TRUE", "TRUE", "TRUE",
                                getImageUrl("shwemyanmar", "shop"));

                // 18. Yangon Store Bangkok
                addShopSample(sheet, row++, "Yangon Store", "ရန်ကုန် စတိုး",
                                "yangon-store-bkk", "Grocery", "13.7560", "100.5460",
                                "Ratchaprarop Soi 8, Bangkok", "Yangon Store", "General Store", "Ratchathewi",
                                "Bangkok", "094-777-8888", "",
                                "General goods", "အထွေထွေ ကုန်ပစ္စည်း",
                                "Sim cards, Top up", "FALSE", "FALSE", "TRUE", "FALSE", "TRUE",
                                getImageUrl("yangon", "shop"));

                // 19. Mahachai Seafood Market Stall
                addShopSample(sheet, row++, "Mahachai Myanmar Seafood", "မဟာချိုင် ပင်လယ်စာ",
                                "mahachai-seafood", "Grocery", "13.5475", "100.2736",
                                "Mahachai Market, Samut Sakhon (Greater Bangkok)", "Mahachai", "Seafood",
                                "Samut Sakhon",
                                "Samut Sakhon", "095-888-9999", "",
                                "Fresh seafood market popular with Myanmar workers",
                                "မြန်မာ အလုပ်သမားများကြား ရေပန်းစားသော ပင်လယ်စာဈေး",
                                "Fresh Fish, Crab", "TRUE", "TRUE", "FALSE", "FALSE", "TRUE",
                                getImageUrl("mahachai", "shop"));

                // 20. Ramkhamhaeng Myanmar Food
                addShopSample(sheet, row++, "Ramkhamhaeng Myanmar Food", "ရာမ်ခမ်းဟိုင် မြန်မာ အစားအစာ",
                                "ramkhamhaeng-food", "Restaurant", "13.7600", "100.6200",
                                "Ramkhamhaeng Soi 24, Bangkok", "Ramkhamhaeng Food", "Street Food", "Bang Kapi",
                                "Bangkok", "096-999-0000", "",
                                "Student favorite", "ကျောင်းသား ကြိုက်",
                                "Mohinga, Ohn No Khao Swe", "TRUE", "FALSE", "TRUE", "FALSE", "TRUE",
                                getImageUrl("ram", "shop"));
        }

        private static void addShopSample(Sheet sheet, int rowNum, String... values) {
                Row row = sheet.createRow(rowNum);
                for (int i = 0; i < values.length; i++) {
                        Cell cell = row.createCell(i);
                        if (i >= 16 && i <= 20) { // Boolean columns
                                cell.setCellValue(values[i]);
                        } else if (i == 4 || i == 5) { // Lat/Lng columns
                                try {
                                        cell.setCellValue(Double.parseDouble(values[i]));
                                } catch (NumberFormatException e) {
                                        cell.setCellValue(values[i]);
                                }
                        } else {
                                cell.setCellValue(values[i]);
                        }
                }
        }

        private static void createMenuItemsSheet(Workbook workbook) {
                Sheet sheet = workbook.createSheet("MenuItems");

                Row headerRow = sheet.createRow(0);
                String[] headers = {
                                "shopSlug", "categoryName", "itemName", "price", "currency",
                                "isVegetarian", "isSpicy", "isPopular", "imageUrl"
                };

                CellStyle headerStyle = createHeaderStyle(workbook);
                for (int i = 0; i < headers.length; i++) {
                        Cell cell = headerRow.createCell(i);
                        cell.setCellValue(headers[i]);
                        cell.setCellStyle(headerStyle);
                        sheet.setColumnWidth(i, 4000);
                }

                int row = 1;

                // 1. inle-traditional-food
                addMenuSample(sheet, row++, "inle-traditional-food", "Noodles", "Shan Noodle (Soup)", "80", "THB",
                                "FALSE", "FALSE", "TRUE", getImageUrl("shan-soup", "food"));
                addMenuSample(sheet, row++, "inle-traditional-food", "Noodles", "Shan Noodle (Dry)", "80", "THB",
                                "FALSE", "FALSE", "TRUE", getImageUrl("shan-dry", "food"));
                addMenuSample(sheet, row++, "inle-traditional-food", "Appetizers", "Fried Tofu", "60", "THB", "TRUE",
                                "FALSE", "TRUE", getImageUrl("tofu", "food"));

                // 2. shwe-htee-restaurant
                addMenuSample(sheet, row++, "shwe-htee-restaurant", "Main Course", "Pork Curry", "150", "THB", "FALSE",
                                "TRUE", "TRUE", getImageUrl("pork-curry", "food"));
                addMenuSample(sheet, row++, "shwe-htee-restaurant", "Main Course", "Chicken Curry", "150", "THB",
                                "FALSE", "TRUE", "TRUE", getImageUrl("chicken-curry", "food"));
                addMenuSample(sheet, row++, "shwe-htee-restaurant", "Salads", "Tea Leaf Salad", "100", "THB", "TRUE",
                                "TRUE", "TRUE", getImageUrl("tea-leaf", "food"));

                // 3. ayar-house
                addMenuSample(sheet, row++, "ayar-house", "Snacks", "Samosa", "80", "THB", "TRUE", "FALSE", "TRUE",
                                getImageUrl("samosa", "food"));
                addMenuSample(sheet, row++, "ayar-house", "Mains", "Mohinga", "120", "THB", "FALSE", "FALSE", "TRUE",
                                getImageUrl("mohinga", "food"));
                addMenuSample(sheet, row++, "ayar-house", "Beer Snacks", "Grilled Pork Neck", "180", "THB", "FALSE",
                                "FALSE", "TRUE", getImageUrl("pork-neck", "food"));

                // 4. kalyana-restaurant
                addMenuSample(sheet, row++, "kalyana-restaurant", "Rice Dishes", "Chicken Danbauk", "250", "THB",
                                "FALSE", "FALSE", "TRUE", getImageUrl("danbauk", "food"));
                addMenuSample(sheet, row++, "kalyana-restaurant", "Curries", "Mutton Curry", "280", "THB", "FALSE",
                                "TRUE", "TRUE", getImageUrl("mutton", "food"));

                // 5. feel-restaurant-bkk
                addMenuSample(sheet, row++, "feel-restaurant-bkk", "Breakfast", "Mohinga", "150", "THB", "FALSE",
                                "FALSE", "TRUE", getImageUrl("feel-mohinga", "food"));
                addMenuSample(sheet, row++, "feel-restaurant-bkk", "Breakfast", "Nan Gyi Thoke", "140", "THB", "FALSE",
                                "TRUE", "TRUE", getImageUrl("nangyi", "food"));
                addMenuSample(sheet, row++, "feel-restaurant-bkk", "Dessert", "Shwe Yin Aye", "80", "THB", "TRUE",
                                "FALSE", "TRUE", getImageUrl("dessert", "food"));

                // 6. bagan-myay
                addMenuSample(sheet, row++, "bagan-myay", "Rice", "Fried Rice with Egg", "80", "THB", "FALSE", "FALSE",
                                "TRUE", getImageUrl("fried-rice", "food"));
                addMenuSample(sheet, row++, "bagan-myay", "Noodles", "Kyar Zan Chat", "90", "THB", "FALSE", "FALSE",
                                "TRUE", getImageUrl("kyarzan", "food"));

                // 7. mandalay-food-house
                addMenuSample(sheet, row++, "mandalay-food-house", "Noodles", "Mandalay Mee Shay", "100", "THB",
                                "FALSE", "TRUE", "TRUE", getImageUrl("meeshay", "food"));
                addMenuSample(sheet, row++, "mandalay-food-house", "Noodles", "Mandalay Meeshay (Soup)", "100", "THB",
                                "FALSE", "FALSE", "TRUE", getImageUrl("meeshay-soup", "food"));

                // 8. 89-myanmar-foods
                addMenuSample(sheet, row++, "89-myanmar-foods", "Condiments", "Tea Leaf Pack (Spicy)", "50", "THB",
                                "TRUE", "TRUE", "TRUE", getImageUrl("tealeaf", "food"));
                addMenuSample(sheet, row++, "89-myanmar-foods", "Condiments", "Pickled Mango", "60", "THB", "TRUE",
                                "FALSE", "TRUE", getImageUrl("mango", "food"));
                addMenuSample(sheet, row++, "89-myanmar-foods", "Beverages", "Royal Myanmar Tea Mix", "150", "THB",
                                "TRUE", "FALSE", "TRUE", getImageUrl("teamix", "food"));

                // 9. a-nyar-tar
                addMenuSample(sheet, row++, "a-nyar-tar", "Salads", "Pennywort Salad", "80", "THB", "TRUE", "TRUE",
                                "TRUE", getImageUrl("pennywort", "food"));
                addMenuSample(sheet, row++, "a-nyar-tar", "Mains", "Pone Yay Gyi (Bean Paste)", "100", "THB", "TRUE",
                                "FALSE", "TRUE", getImageUrl("poneyaygyi", "food"));

                // 10. ab-thai-myanmar
                addMenuSample(sheet, row++, "ab-thai-myanmar", "Thai", "Tom Yum Kung", "150", "THB", "FALSE", "TRUE",
                                "TRUE", getImageUrl("tomyum", "food"));
                addMenuSample(sheet, row++, "ab-thai-myanmar", "Myanmar", "Mohinga", "80", "THB", "FALSE", "FALSE",
                                "TRUE", getImageUrl("mohinga2", "food"));

                // 11. delicious-democracy
                addMenuSample(sheet, row++, "delicious-democracy", "Noodles", "Coconut Noodle", "90", "THB", "FALSE",
                                "FALSE", "TRUE", getImageUrl("coconut", "food"));
                addMenuSample(sheet, row++, "delicious-democracy", "Salads", "Ginger Salad", "80", "THB", "TRUE",
                                "FALSE", "TRUE", getImageUrl("ginger", "food"));

                // 12. phra-khanong-store-1
                addMenuSample(sheet, row++, "phra-khanong-store-1", "Vegetables", "Roselle Leaves (Bundle)", "20",
                                "THB", "TRUE", "FALSE", "TRUE", getImageUrl("roselle", "food"));
                addMenuSample(sheet, row++, "phra-khanong-store-1", "Vegetables", "Sour Bamboo Shoot", "40", "THB",
                                "TRUE", "FALSE", "TRUE", getImageUrl("bamboo", "food"));

                // 13. daw-hla-grocery
                addMenuSample(sheet, row++, "daw-hla-grocery", "Dry Goods", "Dried Fish (small)", "100", "THB", "FALSE",
                                "FALSE", "TRUE", getImageUrl("dried-fish", "food"));
                addMenuSample(sheet, row++, "daw-hla-grocery", "Spices", "Masala Powder", "30", "THB", "TRUE", "FALSE",
                                "FALSE", getImageUrl("masala", "food"));

                // 14. bang-bon-mart
                addMenuSample(sheet, row++, "bang-bon-mart", "Cosmetics", "Thanaka Paste", "50", "THB", "TRUE", "FALSE",
                                "TRUE", getImageUrl("thanaka", "food"));
                addMenuSample(sheet, row++, "bang-bon-mart", "Clothing", "Longyi (Men)", "300", "THB", "TRUE", "FALSE",
                                "TRUE", getImageUrl("longyi", "food"));

                // 15. ko-thein-shop
                addMenuSample(sheet, row++, "ko-thein-shop", "Snacks", "Sunflower Seeds", "20", "THB", "TRUE", "FALSE",
                                "TRUE", getImageUrl("sunflower", "food"));
                addMenuSample(sheet, row++, "ko-thein-shop", "Tea", "Myanmar Tea (Ready Made)", "25", "THB", "TRUE",
                                "FALSE", "TRUE", getImageUrl("tea", "food"));

                // 16. little-myanmar-shop
                addMenuSample(sheet, row++, "little-myanmar-shop", "Medicine", "Burmese Balm", "40", "THB", "TRUE",
                                "FALSE", "TRUE", getImageUrl("balm", "food"));
                addMenuSample(sheet, row++, "little-myanmar-shop", "Snacks", "Fried Beans", "35", "THB", "TRUE",
                                "FALSE", "TRUE", getImageUrl("beans", "food"));

                // 17. shwe-myanmar-grocery
                addMenuSample(sheet, row++, "shwe-myanmar-grocery", "Food", "Fish Paste (Ngapi)", "80", "THB", "FALSE",
                                "FALSE", "TRUE", getImageUrl("ngapi", "food"));
                addMenuSample(sheet, row++, "shwe-myanmar-grocery", "Food", "Rice Vermicelli", "40", "THB", "TRUE",
                                "FALSE", "TRUE", getImageUrl("verm", "food"));

                // 18. yangon-store-bkk
                addMenuSample(sheet, row++, "yangon-store-bkk", "Service", "Sim Card Topup", "100", "THB", "FALSE",
                                "FALSE", "TRUE", getImageUrl("sim", "food"));
                addMenuSample(sheet, row++, "yangon-store-bkk", "Service", "Money Transfer", "0", "THB", "FALSE",
                                "FALSE", "TRUE", getImageUrl("money", "food"));

                // 19. mahachai-seafood
                addMenuSample(sheet, row++, "mahachai-seafood", "Seafood", "Fresh Mackerel (kg)", "120", "THB", "FALSE",
                                "FALSE", "TRUE", getImageUrl("fish", "food"));
                addMenuSample(sheet, row++, "mahachai-seafood", "Seafood", "Blue Crab (kg)", "350", "THB", "FALSE",
                                "FALSE", "TRUE", getImageUrl("crab", "food"));

                // 20. ramkhamhaeng-food
                addMenuSample(sheet, row++, "ramkhamhaeng-food", "Street Food", "Mohinga", "60", "THB", "FALSE",
                                "FALSE", "TRUE", getImageUrl("mohinga3", "food"));
                addMenuSample(sheet, row++, "ramkhamhaeng-food", "Street Food", "Fried Gourd", "10", "THB", "TRUE",
                                "FALSE", "TRUE", getImageUrl("gourd", "food"));
        }

        private static void addMenuSample(Sheet sheet, int rowNum, String... values) {
                Row row = sheet.createRow(rowNum);
                for (int i = 0; i < values.length; i++) {
                        Cell cell = row.createCell(i);
                        // Price is at index 3
                        if (i == 3) {
                                try {
                                        cell.setCellValue(Double.parseDouble(values[i]));
                                } catch (NumberFormatException e) {
                                        cell.setCellValue(values[i]);
                                }
                        } else {
                                cell.setCellValue(values[i]);
                        }
                }
        }

        private static void createOperatingHoursSheet(Workbook workbook) {
                Sheet sheet = workbook.createSheet("OperatingHours");
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

                int row = 1;

                // Define standard hours for all shops (1-7, Mon-Sun)
                // Adjust some for variety
                // 1-7 (Mon-Sun)
                String[] allSlugs = {
                                "inle-traditional-food", "shwe-htee-restaurant", "ayar-house", "kalyana-restaurant",
                                "feel-restaurant-bkk", "bagan-myay", "mandalay-food-house", "89-myanmar-foods",
                                "a-nyar-tar", "ab-thai-myanmar", "delicious-democracy", "phra-khanong-store-1",
                                "daw-hla-grocery", "bang-bon-mart", "ko-thein-shop", "little-myanmar-shop",
                                "shwe-myanmar-grocery", "yangon-store-bkk", "mahachai-seafood", "ramkhamhaeng-food"
                };

                for (String slug : allSlugs) {
                        // Monday to Saturday
                        for (int day = 1; day <= 6; day++) {
                                addHoursSample(sheet, row++, slug, String.valueOf(day), "09:00", "21:00", "FALSE");
                        }
                        // Sunday (some closed, some open)
                        boolean isClosedSunday = slug.contains("store") || slug.contains("grocery")
                                        || slug.contains("mart");
                        if (isClosedSunday) {
                                addHoursSample(sheet, row++, slug, "7", "00:00", "00:00", "TRUE");
                        } else {
                                addHoursSample(sheet, row++, slug, "7", "09:00", "21:00", "FALSE");
                        }
                }
        }

        private static void addHoursSample(Sheet sheet, int rowNum, String... values) {
                Row row = sheet.createRow(rowNum);
                for (int i = 0; i < values.length; i++) {
                        Cell cell = row.createCell(i);
                        if (i == 1) { // DayOfWeek is integer
                                try {
                                        cell.setCellValue(Integer.parseInt(values[i]));
                                } catch (NumberFormatException e) {
                                        cell.setCellValue(values[i]);
                                }
                        } else {
                                cell.setCellValue(values[i]);
                        }
                }
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
