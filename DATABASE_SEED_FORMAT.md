# Database Seed Excel Format Guide

## ⚠️ Entity Relationships

The **Shop** entity has these location fields:
- `district` (String) - Simple text field for district name
- `city` (String) - Simple text field for city name (default: "Yangon")
- `districtObj` (ManyToOne) - Relationship to District entity (set automatically if district matches)

**The import service will:**
1. Set `shop.district` from the Excel column
2. Set `shop.city` from the Excel column  
3. Automatically look up and set `shop.districtObj` if a matching District entity exists

## Required Sheets (in order)

### 1. Locations
Columns: `cityNameEn`, `cityNameMm`, `districtNameEn`, `districtNameMm`, `latitude`, `longitude`

### 2. Users
Columns: `username`, `email`, `password`, `fullName`, `role`

### 3. Shops
Columns: `name`, `nameMm`, `slug`, `category`, `latitude`, `longitude`, `address`, `nameEn`, `subCategory`, `district`, `city`, `phone`, `email`, `description`, `descriptionMm`, `specialties`, `hasDelivery`, `hasParking`, `hasWifi`, `isVerified`, `isActive`, `primaryPhotoUrl`

**Note**: Both `district` and `city` are simple text fields

### 4. MenuItems ⭐ NOW INCLUDED
Columns: `shopSlug`, `categoryName`, `itemName`, `price`, `currency`, `isVegetarian`, `isSpicy`, `isPopular`, `imageUrl`, `nameMm`, `nameEn`, `photos`

**Important**: 
- Column 11 (`photos`) = comma-separated URLs for menu item photos
- Example: `https://photo1.jpg,https://photo2.jpg`
- MenuCategories are created automatically from the `categoryName` column
- Menu item photos are embedded in this sheet (no separate sheet needed)

### 5. OperatingHours
Columns: `shopSlug`, `dayOfWeek`, `openingTime`, `closingTime`, `isClosed`

### 6. Reviews ⭐ PHOTOS EMBEDDED
Columns: `shopSlug`, `username`, `rating`, `comment`, `photos`

**Important**: Column 4 (`photos`) = comma-separated URLs
- Example: `https://photo1.jpg,https://photo2.jpg,https://photo3.jpg`
- Review photos are created automatically when the review is saved

### 7. Favorites
Columns: `username`, `shopSlug`

### 8. UserActivity
Columns: `username`, `shopSlug`, `activityType`, `searchQuery`, `targetName`

## What Gets Imported

✅ **Shops** - with district and city text fields  
✅ **MenuItems** - with categories auto-created  
✅ **Menu Item Photos** - embedded in MenuItems sheet (column 11)  
✅ **MenuCategories** - automatically created from menu items  
✅ **Operating Hours**  
✅ **Reviews** - including review photos (embedded in column 4)  
✅ **Review Photos** - embedded in Reviews sheet  
✅ **Favorites**  
✅ **User Activities**  
✅ **Shop Photos** - primary photo URL in Shops sheet  

## ❌ Common Mistakes

1. **Creating separate photo sheets** - Photos should be comma-separated in the main sheets
2. **Wrong district/city format** - These are simple text fields, not IDs
3. **Missing MenuItems sheet** - Previously not imported, now included
4. **Review photos foreign key errors** - Don't use a separate ReviewPhotos sheet

## Quick Checklist

- [ ] Locations sheet with cities and districts
- [ ] Users sheet with accounts
- [ ] Shops sheet with `district` and `city` as text
- [ ] **MenuItems sheet with photos in column 11**
- [ ] Operating hours for each shop
- [ ] Reviews with photos embedded in column 4
- [ ] Favorites (optional)
- [ ] User activities (optional)
