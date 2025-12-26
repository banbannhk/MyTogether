package org.th.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class SeedDataGeneratorTest {

    // Bangkok Districts: English, Thai (Local Name)
    private static final String[][] LOCATIONS = {
            { "Pathum Wan", "ปทุมวัน" },
            { "Watthana", "วัฒนา" },
            { "Bang Rak", "บางรัก" },
            { "Khlong Toei", "คลองเตย" },
            { "Phra Nakhon", "พระนคร" },
            { "Sathon", "สาทร" },
            { "Chatuchak", "จตุจักร" },
            { "Ratchathewi", "ราชเทวี" },
            { "Huai Khwang", "ห้วยขวาง" },
            { "Din Daeng", "ดินแดง" },
            { "Phaya Thai", "พญาไท" },
            { "Dusit", "ดุสิต" }
    };

    private static final String[] USERS = { "admin", "user1", "user2", "foodie_mm", "yangon_eats", "su_su", "mg_mg",
            "thida" };

    // Helper for Menu Items
    static class MenuItemTemplate {
        String nameEn;
        String nameMm;
        int price;
        String desc;
        String category; // Menu Category (e.g., "Mains", "Drinks")
        String subCategory; // Menu SubCategory (e.g., "Fried", "Soup")

        public MenuItemTemplate(String nameEn, String nameMm, int price, String desc, String category,
                String subCategory) {
            this.nameEn = nameEn;
            this.nameMm = nameMm;
            this.price = price;
            this.desc = desc;
            this.category = category;
            this.subCategory = subCategory;
        }
    }

    // Shop Templates by Category
    static class ShopTemplate {
        String shopCategory; // Shop Category (e.g., "Restaurant")
        MenuItemTemplate[] menuItems;
        String[] images;

        public ShopTemplate(String shopCategory, MenuItemTemplate[] menuItems, String[] images) {
            this.shopCategory = shopCategory;
            this.menuItems = menuItems;
            this.images = images;
        }
    }

    private Map<String, ShopTemplate> templates = new HashMap<>();

    public SeedDataGeneratorTest() {
        templates.put("Noodle", new ShopTemplate("Food",
                new MenuItemTemplate[] {
                        new MenuItemTemplate("Shan Noodle", "ရှမ်းခေါက်ဆွဲ", 120, "Sticky rice noodles with chicken",
                                "Mains", "Soup"),
                        new MenuItemTemplate("Meeshay", "မြီးရှည်", 150, "Mandalay style noodle", "Mains", "Dry"),
                        new MenuItemTemplate("Tofu Nway", "တို့ဟူးနွေး", 100, "Warm tofu with noodles", "Mains",
                                "Soup"),
                        new MenuItemTemplate("Dumplings", "ဖက်ထုပ်", 180, "Steamed pork dumplings", "Sides", "Steamed"),
                        new MenuItemTemplate("Fried Shan Tofu", "ရှမ်းတို့ဟူးကြော်", 80, "Crispy fried tofu", "Sides",
                                "Fried")
                },
                new String[] { "https://th-api.s3.ap-southeast-1.amazonaws.com/shops/shan1.jpg",
                        "https://th-api.s3.ap-southeast-1.amazonaws.com/shops/shan2.jpg" }));

        templates.put("TeaHouse", new ShopTemplate("Cafe",
                new MenuItemTemplate[] {
                        new MenuItemTemplate("Burmese Tea", "မြန်မာလက်ဖက်ရည်", 60, "Sweet creamy tea", "Drinks", "Hot"),
                        new MenuItemTemplate("Mohinga", "မုန့်ဟင်းခါး", 120, "Fish soup noodle", "Mains", "Soup"),
                        new MenuItemTemplate("Samosa", "စမူဆာ", 50, "Fried pastry", "Sides", "Fried"),
                        new MenuItemTemplate("Nan Gyi Thoke", "နန်းကြီးသုပ်", 150, "Thick rice noodle salad", "Mains",
                                "Dry"),
                        new MenuItemTemplate("E Kya Kway", "အီကြာကွေး", 40, "Fried dough stick", "Sides", "Fried")
                },
                new String[] { "https://th-api.s3.ap-southeast-1.amazonaws.com/shops/rth1.jpg",
                        "https://th-api.s3.ap-southeast-1.amazonaws.com/shops/rth2.jpg" }));

        templates.put("FineDining", new ShopTemplate("Restaurant",
                new MenuItemTemplate[] {
                        new MenuItemTemplate("River Prawn Curry", "ပုဇွန်ထုတ်ဟင်း", 800, "Giant river prawn with curry",
                                "Mains", "Curry"),
                        new MenuItemTemplate("Tea Leaf Salad", "လက်ဖက်သုပ်", 200, "Fermented tea leaves", "Appetizers",
                                "Salad"),
                        new MenuItemTemplate("Lobster Thermidor", "ကျောက်ပုဇွန်ဟင်း", 2500, "Classic french dish",
                                "Mains", "Seafood"),
                        new MenuItemTemplate("Wagyu Steak", "အမဲသားကင်", 4000, "A5 Wagyu beef", "Mains", "Grill"),
                        new MenuItemTemplate("Truffle Pasta", "ထရပ်ဖယ် ခေါက်ဆွဲ", 1200, "Pasta with black truffle",
                                "Mains", "Pasta")
                },
                new String[] { "https://th-api.s3.ap-southeast-1.amazonaws.com/shops/feel1.jpg",
                        "https://th-api.s3.ap-southeast-1.amazonaws.com/shops/sharky1.jpg" }));

        templates.put("BBQ", new ShopTemplate("Restaurant",
                new MenuItemTemplate[] {
                        new MenuItemTemplate("Pork Stick", "ဝက်သားကင်", 30, "Grilled pork skewer", "BBQ", "Pork"),
                        new MenuItemTemplate("Chicken Wings", "ကြက်တောင်ပံကင်", 40, "Grilled wings", "BBQ", "Chicken"),
                        new MenuItemTemplate("Enoki Mushroom", "မှိုကင်", 20, "Grilled mushroom", "BBQ", "Vegetable"),
                        new MenuItemTemplate("Pork Ribs", "ဝက်နံရိုးကင်", 350, "BBQ pork ribs", "Mains", "Pork"),
                        new MenuItemTemplate("Grilled Fish", "ငါးကင်", 400, "Salt grilled tilapia", "Mains", "Seafood")
                },
                new String[] { "https://th-api.s3.ap-southeast-1.amazonaws.com/shops/bbq1.jpg",
                        "https://th-api.s3.ap-southeast-1.amazonaws.com/shops/bbq2.jpg" }));

        templates.put("Cafe", new ShopTemplate("Cafe",
                new MenuItemTemplate[] {
                        new MenuItemTemplate("Latte", "လတ်တေး", 120, "Espresso with milk", "Coffee", "Hot"),
                        new MenuItemTemplate("Cappuccino", "ကပ္ပချီနို", 120, "Foamy coffee", "Coffee", "Hot"),
                        new MenuItemTemplate("Cheesecake", "ချိစ်ကိတ်", 180, "New York style cheesecake", "Dessert",
                                "Cake"),
                        new MenuItemTemplate("Croissant", "ကရိုဆွန့်", 100, "Butter croissant", "Bakery", "Pastry"),
                        new MenuItemTemplate("Iced Americano", "အမေရိကါနို", 110, "Black coffee on ice", "Coffee",
                                "Cold")
                },
                new String[] { "https://th-api.s3.ap-southeast-1.amazonaws.com/shops/cafe1.jpg",
                        "https://th-api.s3.ap-southeast-1.amazonaws.com/shops/cafe2.jpg" }));

        templates.put("Bar", new ShopTemplate("Bar",
                new MenuItemTemplate[] {
                        new MenuItemTemplate("Myanmar Beer", "မြန်မာဘီယာ", 150, "Draught beer", "Alcohol", "Beer"),
                        new MenuItemTemplate("Mojito", "မော်ဂျီတို", 300, "Rum, mint, lime", "Alcohol", "Cocktail"),
                        new MenuItemTemplate("French Fries", "အာလူးချောင်းကြော်", 120, "Crispy fries", "Snacks",
                                "Fried"),
                        new MenuItemTemplate("Chicken Popcorn", "ကြက်သားကြော်", 180, "Fried chicken bites", "Snacks",
                                "Fried"),
                        new MenuItemTemplate("Whisky Sour", "ဝီစကီ", 350, "Classic cocktail", "Alcohol", "Cocktail")
                },
                new String[] { "https://th-api.s3.ap-southeast-1.amazonaws.com/shops/bar1.jpg",
                        "https://th-api.s3.ap-southeast-1.amazonaws.com/shops/bar2.jpg" }));
    }

    // List of 30+ Real Shops: {English Name, Burmese Name, Type}
    private static final String[][] SHOP_LIST = {
            { "999 Shan Noodle", "၉၉၉ ရှမ်းခေါက်ဆွဲ", "Noodle" },
            { "Rangoon Tea House", "ရန်ကုန်လက်ဖက်ရည်ဆိုင်", "TeaHouse" },
            { "Feel Myanmar Food", "ဖီးလ်မြန်မာ", "FineDining" },
            { "Sharky's", "ရှားကီး", "FineDining" },
            { "YKKO", "ဝိုင်ကေကေအို", "Noodle" },
            { "Shwe Paline", "ရွှေပလ္လင်", "TeaHouse" },
            { "Min Lan Seafood", "မင်းလမ်း ရခိုင်မုန့်တီ", "FineDining" },
            { "Beauty in the Pot", "ဟော့ပေါ့", "FineDining" },
            { "Harry's Bar", "ဟယ်ရီဘား", "Bar" },
            { "Gekko", "ဂက္ကို", "Bar" },
            { "Le Planteur", "လ ပလန်တာ", "FineDining" },
            { "Seeds", "ဆိ", "FineDining" },
            { "Shan Yoe Yar", "ရှမ်းရိုးရာ", "Noodle" },
            { "Green Gallery", "ဂရင်း ဂယ်လာရီ", "FineDining" },
            { "Oishii Sushi", "အိုရှီ ဆူရှီ", "FineDining" },
            { "Fuji Coffee House", "ဖူဂျီ ကော်ဖီ", "Cafe" },
            { "Potato Break", "အာလူး ဘရိတ်", "FineDining" },
            { "Gloria Jean's", "ဂလိုရီယာ ဂျင်း", "Cafe" },
            { "Tom N Toms", "တွမ် အင် တွမ်", "Cafe" },
            { "Season Bakery", "ရာသီ မုန့်တိုက်", "Cafe" },
            { "SP Bakery", "အက်စ်ပီ မုန့်တိုက်", "Cafe" },
            { "Love Di", "လပ်ဖ် ဒီ", "FineDining" },
            { "Vista Bar", "ဗစ်စတာ ဘား", "Bar" },
            { "Atlas Rooftop Bar", "အက်တလပ်စ်", "Bar" },
            { "Yangon Yangon", "ရန်ကုန် ရန်ကုန်", "Bar" },
            { "The Pansodan", "ပန်းဆိုးတန်း", "FineDining" },
            { "1920 Tea House", "၁၉၂၀ လက်ဖက်ရည်ဆိုင်", "TeaHouse" },
            { "Lucky Seven", "လဒ်ကီး ဆဲဗင်း", "TeaHouse" },
            { "Aung Mingalar Shan Noodle", "အောင်မင်္ဂလာ ရှမ်းခေါက်ဆွဲ", "Noodle" },
            { "Golden Duck", "ရွှေဘဲ", "FineDining" },
            { "Minn Lan Mon", "မင်းလမ်း (မွန်)", "FineDining" },
            { "KFC", "ကြက်ကြော်", "FineDining" },
            { "Lotteria", "လိုတီးရီးယား", "FineDining" },
            { "Pizza Hut", "ပီဇာဟတ်", "FineDining" }
    };

    @Test
    public void generateSeedData() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            createLocationsSheet(workbook);
            createUsersSheet(workbook);

            // Create data lists to hold generated rows for cross-referencing
            List<String[]> shopData = new ArrayList<>();
            List<String[]> categoryData = new ArrayList<>();
            List<String[]> subCategoryData = new ArrayList<>();
            List<String[]> menuData = new ArrayList<>();
            List<String[]> reviewData = new ArrayList<>();
            List<String[]> favoriteData = new ArrayList<>();
            List<String[]> activityData = new ArrayList<>();

            generateShopRelatedData(shopData, categoryData, subCategoryData, menuData, reviewData, favoriteData,
                    activityData);

            createShopsSheet(workbook, shopData);
            createMenuCategoriesSheet(workbook, categoryData);
            createMenuSubCategoriesSheet(workbook, subCategoryData);
            createMenuItemsSheet(workbook, menuData);
            createReviewsSheet(workbook, reviewData);
            createFavoritesSheet(workbook, favoriteData);
            createUserActivitySheet(workbook, activityData);

            try (FileOutputStream fileOut = new FileOutputStream("database_seed.xlsx")) {
                workbook.write(fileOut);
            }
        }
        System.out.println("Seed data generated: database_seed.xlsx (30+ shops, Bangkok Localized, SubCategories)");
    }

    private void generateShopRelatedData(List<String[]> shopData, List<String[]> categoryData,
            List<String[]> subCategoryData, List<String[]> menuData,
            List<String[]> reviewData, List<String[]> favoriteData,
            List<String[]> activityData) {

        Random rand = new Random();
        int locationIndex = 0;

        for (String[] shopInfo : SHOP_LIST) {
            String nameEn = shopInfo[0];
            String nameMm = shopInfo[1];
            String type = shopInfo[2];
            String slug = nameEn.toLowerCase().replace(" ", "-").replace("'", "");
            ShopTemplate template = templates.getOrDefault(type, templates.get("FineDining"));

            String city = "Bangkok";
            String[] loc = LOCATIONS[locationIndex % LOCATIONS.length];
            String districtEn = loc[0];
            locationIndex++;

            // 1. Shop Row
            String[] sRow = new String[30];
            sRow[0] = nameEn;
            sRow[1] = nameMm;
            sRow[2] = slug;
            sRow[3] = template.shopCategory;
            sRow[4] = String.format("13.%03d", 700 + rand.nextInt(150));
            sRow[5] = String.format("100.%03d", 450 + rand.nextInt(200));
            sRow[6] = rand.nextInt(100) + " Sukhumvit Soi " + rand.nextInt(50) + ", " + districtEn;
            sRow[7] = "02-" + (1000000 + rand.nextInt(9000000));
            sRow[9] = "Experience the best " + nameEn + " in Bangkok.";
            sRow[10] = "ဘန်ကောက်ရှိ အကောင်းဆုံး " + nameMm + " ကို မြည်းစမ်းကြည့်ပါ။";
            sRow[11] = "09:00 - 22:00";
            sRow[12] = rand.nextBoolean() ? "$$" : "$$$";
            sRow[13] = String.format("%.1f", 3.5 + (rand.nextDouble() * 1.5));
            sRow[14] = String.valueOf(10 + rand.nextInt(500));
            sRow[15] = "true";
            sRow[16] = String.valueOf(rand.nextInt(10) > 8);
            sRow[17] = city;
            sRow[18] = districtEn;
            sRow[21] = template.images[0];
            sRow[26] = String.join(",", template.images);

            shopData.add(sRow);

            // Track categories and subcategories for this shop to avoid duplicates locally
            Set<String> processedCategories = new HashSet<>();
            Set<String> processedSubCategories = new HashSet<>();

            int catOrder = 0;
            int subCatOrder = 0;

            // 2. Menu Items + Categories + SubCategories
            for (MenuItemTemplate item : template.menuItems) {
                // Category
                if (processedCategories.add(item.category)) {
                    String[] cRow = new String[5];
                    cRow[0] = slug; // Shop Slug
                    cRow[1] = item.category; // Name
                    cRow[2] = item.category; // NameMM (placeholder)
                    cRow[3] = item.category; // NameEn
                    cRow[4] = String.valueOf(catOrder++); // DisplayOrder
                    categoryData.add(cRow);
                }

                // SubCategory
                String subCatKey = item.category + ":" + item.subCategory;
                if (processedSubCategories.add(subCatKey)) {
                    String[] scRow = new String[6];
                    scRow[0] = slug; // Shop Slug
                    scRow[1] = item.category; // Parent Category Name
                    scRow[2] = item.subCategory; // Name
                    scRow[3] = item.subCategory; // NameMM
                    scRow[4] = item.subCategory; // NameEn
                    scRow[5] = String.valueOf(subCatOrder++); // DisplayOrder
                    subCategoryData.add(scRow);
                }

                // Menu Item
                // Headers: 0:ShopSlug, 1:CategoryName, 2:Name, 3:Price, 4:Currency, 5:IsVeg,
                // 6:IsSpicy, 7:IsPop, 8:ImgUrl, 9:NameMm, 10:NameEn, 11:Photos, 12:SubCategory
                String[] mRow = new String[15];
                mRow[0] = slug;
                mRow[1] = item.category;
                mRow[2] = item.nameEn; // Name
                mRow[3] = String.valueOf(item.price);
                mRow[4] = "THB";
                mRow[5] = "false";
                mRow[6] = "false";
                mRow[7] = "true";
                mRow[8] = "https://placehold.co/100x100";
                mRow[9] = item.nameMm;
                mRow[10] = item.nameEn;
                mRow[11] = "https://placehold.co/200x200";
                mRow[12] = item.subCategory; // INDEX 12: SubCategory Name

                menuData.add(mRow);
            }

            // 3. Reviews (3-5 per shop)
            int reviewCount = 3 + rand.nextInt(3);
            for (int i = 0; i < reviewCount; i++) {
                String[] rRow = new String[5];
                rRow[0] = slug;
                rRow[1] = USERS[rand.nextInt(USERS.length)];
                rRow[2] = String.valueOf(3 + rand.nextInt(3));
                rRow[3] = "Great place in BKK! " + template.menuItems[0].nameEn + " was good.";
                rRow[4] = rand.nextBoolean() ? "https://placehold.co/400x400" : "";
                reviewData.add(rRow);
            }

            // 4. Favorites
            if (rand.nextBoolean()) {
                String[] fRow = new String[3];
                fRow[0] = USERS[rand.nextInt(USERS.length)];
                fRow[1] = slug;
                fRow[2] = "My favorite BKK spot!";
                favoriteData.add(fRow);
            }

            // 5. Activity
            if (rand.nextBoolean()) {
                String[] aRow = new String[6];
                aRow[0] = "device_" + rand.nextInt(1000);
                aRow[1] = "VIEW_SHOP";
                aRow[2] = slug;
                aRow[3] = nameEn;
                aRow[4] = "2023-12-14 12:00:00";
                aRow[5] = "{}";
                activityData.add(aRow);
            }
        }
    }

    private void createLocationsSheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Locations");
        String[] headers = { "City Name", "District Name" };
        createHeader(sheet, headers);

        int rowNum = 1;
        for (String[] loc : LOCATIONS) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue("Bangkok");
            row.createCell(1).setCellValue(loc[0]);
        }
    }

    private void createUsersSheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Users");
        String[] headers = { "Username", "Email", "Password", "Role" };
        createHeader(sheet, headers);

        int rowNum = 1;
        for (String user : USERS) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(user);
            row.createCell(1).setCellValue(user + "@test.com");
            row.createCell(2).setCellValue("password");
            row.createCell(3).setCellValue(user.equals("admin") ? "ADMIN" : "USER");
        }
    }

    private void createShopsSheet(Workbook workbook, List<String[]> data) {
        Sheet sheet = workbook.createSheet("Shops");
        String[] headers = new String[30];
        headers[0] = "Name";
        headers[1] = "Name MM";
        headers[2] = "Slug";
        headers[3] = "Category";
        headers[4] = "Latitude";
        headers[5] = "Longitude";
        headers[6] = "Address";
        headers[7] = "Phone";
        headers[8] = "Website";
        headers[9] = "Description";
        headers[10] = "Description MM";
        headers[11] = "Opening Hours";
        headers[12] = "Price Range";
        headers[13] = "Rating";
        headers[14] = "Review Count";
        headers[15] = "Active";
        headers[16] = "Promoted";
        headers[17] = "City";
        headers[18] = "District";
        headers[21] = "Primary Photo";
        headers[26] = "Photos (Comma Separated)";

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            if (headers[i] != null)
                headerRow.createCell(i).setCellValue(headers[i]);
        }

        populateSheet(sheet, data);
    }

    private void createMenuCategoriesSheet(Workbook workbook, List<String[]> data) {
        Sheet sheet = workbook.createSheet("MenuCategories");
        String[] headers = { "Shop Slug", "Name", "Name MM", "Name En", "Display Order" };
        createHeader(sheet, headers);
        populateSheet(sheet, data);
    }

    private void createMenuSubCategoriesSheet(Workbook workbook, List<String[]> data) {
        Sheet sheet = workbook.createSheet("MenuSubCategories");
        String[] headers = { "Shop Slug", "Category Name", "Name", "Name MM", "Name En", "Display Order" };
        createHeader(sheet, headers);
        populateSheet(sheet, data);
    }

    private void createMenuItemsSheet(Workbook workbook, List<String[]> data) {
        Sheet sheet = workbook.createSheet("MenuItems");
        String[] headers = new String[15];
        headers[0] = "Shop Slug";
        headers[1] = "Category Name";
        headers[2] = "Name";
        headers[3] = "Price";
        headers[4] = "Currency";
        headers[5] = "IsVegetarian";
        headers[6] = "IsSpicy";
        headers[7] = "IsPopular";
        headers[8] = "Image URL";
        headers[9] = "Name MM";
        headers[10] = "Name En";
        headers[11] = "Photos";
        headers[12] = "SubCategory Name";
        createHeader(sheet, headers);
        populateSheet(sheet, data);
    }

    private void createReviewsSheet(Workbook workbook, List<String[]> data) {
        Sheet sheet = workbook.createSheet("Reviews");
        String[] headers = { "Shop Slug", "Username", "Rating", "Comment", "Photos" };
        createHeader(sheet, headers);
        populateSheet(sheet, data);
    }

    private void createFavoritesSheet(Workbook workbook, List<String[]> data) {
        Sheet sheet = workbook.createSheet("Favorites");
        String[] headers = { "Username", "Shop Slug", "Notes" };
        createHeader(sheet, headers);
        populateSheet(sheet, data);
    }

    private void createUserActivitySheet(Workbook workbook, List<String[]> data) {
        Sheet sheet = workbook.createSheet("User Activity");
        String[] headers = { "DeviceId", "Type", "Shop Slug", "Target Name", "Timestamp", "Metadata" };
        createHeader(sheet, headers);
        populateSheet(sheet, data);
    }

    private void createHeader(Sheet sheet, String[] headers) {
        Row row = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            if (headers[i] != null)
                row.createCell(i).setCellValue(headers[i]);
        }
    }

    private void populateSheet(Sheet sheet, List<String[]> data) {
        int rowNum = 1;
        for (String[] rowData : data) {
            Row row = sheet.createRow(rowNum++);
            for (int i = 0; i < rowData.length; i++) {
                if (rowData[i] != null) {
                    row.createCell(i).setCellValue(rowData[i]);
                }
            }
        }
    }
}
