# Platform Algorithm Specifications

## Overview
This document details the technical implementation of the core algorithms and data features that power the platform's discovery and personalization engines.

---

## 1. 🔥 Trending Shops Algorithm
**Goal**: Highlight shops that are currently popular based on recent user activity.

### Logic
The "Trending Score" is a weighted sum of user interactions within specific time windows.
*   **Update Frequency**: Hourly (via `@Scheduled` task).
*   **Storage**: `Shop.trendingScore` (Indexed for fast sorting).

### Scoring Weights
| Action | Weight | Time Window | Rationale |
| :--- | :--- | :--- | :--- |
| **View Shop** | 1 point | Last 24 Hours | Passive interest, high volume. |
| **Favorite** | 5 points | Last 7 Days | Strong intent, longer-term signal. |
| **Review** | 10 points | Last 7 Days | Highest effort, strongest endorsement. |

### Implementation
*   **Service**: `TrendingService.java`
*   **API**: `GET /api/shops/trending` (Returns top 10 sorted by score).

---

## 2. 🎯 Conversion Events (High Intent)
**Goal**: Track specific user actions that strongly correlate with real-world shop visits to measure "Real World Popularity".

### Data Model
*   **Entity**: `UserActivity`
*   **Activity Types**:
    *   `CLICK_DIRECTIONS`: User clicked "Get Directions".
    *   `CLICK_CALL`: User clicked the phone number.
    *   `CLICK_WEBSITE`: User visited the shop's external site.
    *   `CLICK_SHARE`: User shared the shop link.

### API
*   **Endpoint**: `POST /api/shops/{id}/track?action={ACTION_TYPE}`
*   **Usage**: Frontend calls this endpoint when the respective UI element is clicked.

---

## 3. 🌶️ User Taste Profile
**Goal**: Store explicit user preferences to power the "For You" recommendation engine.

### Data Model
*   **Entity**: `User`
*   **Fields**:
    *   `isVegetarian` (Boolean): Filters out non-veg options.
    *   `isHalal` (Boolean): Filters for Halal-certified shops.
    *   `pricePreference` (Enum: LOW, MEDIUM, HIGH): Weights recommendations by price range.
    *   `spicinessPreference` (Enum: MILD, MEDIUM, SPICY): Used for dish recommendations.

### API
*   **Endpoint**: `GET /api/user/profile` - Retrieve current preferences.
*   **Endpoint**: `PUT /api/user/profile` - Update preferences.

---

## 4. 🍲 Dish-Level Ratings
**Goal**: Enable granular ratings for specific menu items to rank the "Best [Dish Name]" across the platform.

### Data Model
*   **Entity**: `MenuItemReview`
*   **Fields**:
    *   `user`: The reviewer.
    *   `menuItem`: The specific dish being rated.
    *   `rating`: 1-5 star rating.
    *   `comment`: Optional text review.

### API
*   **Endpoint**: `POST /api/reviews/menu/{itemId}` - Submit a rating.
*   **Endpoint**: `GET /api/reviews/menu/{itemId}` - Get reviews for a dish.

---

## Future Enhancements
1.  **"Best Mohinga" Leaderboard**: Query `MenuItemReview` for highest average rating where `menuItem.name` LIKE 'Mohinga'.
2.  **Dietary Safety Net**: If `User.isVegetarian` is true, automatically hide or flag non-vegetarian items/shops.
3.  **Real Popularity Score**: Update `TrendingService` to weight `CLICK_DIRECTIONS` (e.g., 50 points) much higher than `VIEW_SHOP` (1 point).
