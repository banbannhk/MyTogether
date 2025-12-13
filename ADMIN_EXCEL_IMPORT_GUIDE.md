# Admin Excel Data Import Format

Use this guide to structure your Excel (`.xlsx`) files for data import. The system expects specific sheet names and column orders.

## Sheet 1: `Shops`
This sheet contains the main shop information.

| Column Index | Header Name | Data Type | Required | Description | Example |
| :--- | :--- | :--- | :--- | :--- | :--- |
| 0 | Name | String | **Yes** | Shop Name (Default/Myanmar) | `Burma Bistro` |
| 1 | Name (MM) | String | **Yes** | Shop Name in Myanmar | `မြန်မာဘစ်စထရို` |
| 2 | Slug | String | **Yes** | Unique URL identifier | `burma-bistro` |
| 3 | Category | String | **Yes** | Main category | `Restaurant` |
| 4 | Latitude | Decimal | **Yes** | Location Latitude | `16.8409` |
| 5 | Longitude | Decimal | **Yes** | Location Longitude | `96.1735` |
| 6 | Address | String | **Yes** | Full Address (English) | `No. 123, Wayzayandar Rd...` |
| 7 | Name (EN) | String | No | Shop Name in English | `Burma Bistro` |
| 8 | Sub Category | String | No | Secondary category | `Asian Fusion` |
| 9 | Township | String | No | Township name | `Bahan` |
| 10 | City | String | No | City name (Default: Yangon) | `Yangon` |
| 11 | Phone | String | No | Contact number | `09450001122` |
| 12 | Email | String | No | Contact email | `info@burmabistro.com` |
| 13 | Description | String | No | English Description | `A modern take on traditional...` |
| 14 | Description (MM)| String | No | Myanmar Description | `ရိုးရာအစားအစာများကို...` |
| 15 | Specialties | String | No | Signature dishes | `Mohinga, Tea Leaf Salad` |
| 16 | Delivery | Boolean | No | Has Delivery? | `TRUE` |
| 17 | Parking | Boolean | No | Has Parking? | `TRUE` |
| 18 | Wifi | Boolean | No | Has Wifi? | `TRUE` |
| 19 | Verified | Boolean | No | Is Verified Shop? | `TRUE` |
| 20 | Active | Boolean | No | Is Shop Active? | `TRUE` |
| 21 | Cover Photo | String | No | URL for cover photo | `https://example.com/cover.jpg` |
| 22 | Address (MM) | String | No | Address in Myanmar | `အမှတ် ၁၂၃, ...` |
| 23 | Price Pref | String | No | `BUDGET`, `MODERATE`, `EXPENSIVE`, `LUXURY` | `MODERATE` |
| 24 | Halal | Boolean | No | Is Halal? | `FALSE` |
| 25 | Vegetarian | Boolean | No | Is Vegetarian Friendly? | `TRUE` |
| 26 | Gallery Photos | String | No | Comma-separated URLs | `https://site.com/1.jpg, https://site.com/2.jpg` |

## Sheet 2: `MenuItems`
This sheet links menu items to shops via the Shop Slug.

| Col | Header Name | Data Type | Required | Description | Example |
| :--- | :--- | :--- | :--- | :--- | :--- |
| 0 | Shop Slug | String | **Yes** | Must match a slug in `Shops` | `burma-bistro` |
| 1 | Category | String | **Yes** | Menu Category (e.g. Mains) | `Main Courses` |
| 2 | Item Name | String | **Yes** | Name of the dish | `Tea Leaf Salad` |
| 3 | Price | Decimal | **Yes** | Price amount | `4500` |
| 4 | Currency | String | No | Default: MMK | `MMK` |
| 5 | Vegetarian | Boolean | No | Is Vegetarian? | `TRUE` |
| 6 | Spicy | Boolean | No | Is Spicy? | `FALSE` |
| 7 | Popular | Boolean | No | Is Popular item? | `TRUE` |
| 8 | Image URL | String | No | Photo of the dish | `https://site.com/salad.jpg` |
| 9 | Name (MM) | String | No | Name in Myanmar | `လက်ဖက်သုပ်` |
| 10 | Name (EN) | String | No | Name in English | `Tea Leaf Salad` |

## Sheet 3: `OperatingHours`
This sheet defines opening hours.

| Col | Header Name | Data Type | Required | Description | Example |
| :--- | :--- | :--- | :--- | :--- | :--- |
| 0 | Shop Slug | String | **Yes** | Must match a slug in `Shops` | `burma-bistro` |
| 1 | Day of Week | Integer | **Yes** | 1=Mon, 2=Tue, ..., 7=Sun | `1` |
| 2 | Open Time | Time | **Yes** | HH:mm (24-hour) | `09:00` |
| 3 | Close Time | Time | **Yes** | HH:mm (24-hour) | `21:00` |
| 4 | Is Closed | Boolean | No | Is closed all day? | `FALSE` |

## Sheet: `UserActivity` (Separate Import)
Used for importing activity logs for testing.

| Col | Header | Data Type | Description |
| :--- | :--- | :--- | :--- |
| 0 | Device ID | String | Unique device identifier |
| 1 | Activity Type| String | Enum (e.g., `VIEW_SHOP`, `SEARCH_QUERY`) |
| 2 | Shop Slug | String | Optional: Link to shop |
| 3 | Target Name | String | Optional: Name of item/shop viewed |
| 4 | Timestamp | String | `yyyy-MM-dd HH:mm:ss` |
| 5 | Metadata | String | Additional JSON or text info |
