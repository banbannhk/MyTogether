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

---

## 5. 🎯 Personalized Feed Algorithm

### Overview
The **Personalized Feed** feature delivers contextually relevant shop recommendations by analyzing user behavior patterns, time context, and location proximity. It combines multiple algorithms to generate four distinct feed sections.

### Endpoint
- **URL**: `GET /api/feed/personalized`
- **Authentication**: Required (JWT)
- **Parameters**:
  - `latitude` (optional): User's current latitude
  - `longitude` (optional): User's current longitude
  - `radiusKm` (optional, default: 5.0): Search radius in kilometers

### Feed Sections

#### 5.1 For You Now
**Purpose**: Time and context-aware recommendations

**Algorithm**:
```
1. Detect current time context:
   - BREAKFAST (6-10 AM) → Cafes, Bakeries, Coffee Shops
   - LUNCH (11 AM-2 PM) → Restaurants, Fast Food, Noodle Shops
   - DINNER (5-9 PM) → Fine Dining, BBQ, Seafood
   - LATE_NIGHT (9 PM-2 AM) → Bars, 24/7, Street Food
   - ANYTIME (other hours) → Cafes, Desserts, Snacks

2. If location provided:
   - Query nearby shops within radius
   - Filter by time-relevant categories
   - Sort by distance
   
3. If no location:
   - Query shops by time-relevant categories
   - Sort by trending score and rating
   
4. Limit to top 10 results
```

**Implementation**: `TimeContextService.java`, `PersonalizedFeedService.buildForYouNowSection()`

#### 5.2 Trending Nearby
**Purpose**: Location-based popularity

**Algorithm**:
```
1. If location provided:
   - Calculate distance using Haversine formula:
     a = sin²(Δlat/2) + cos(lat1) × cos(lat2) × sin²(Δlon/2)
     c = 2 × atan2(√a, √(1-a))
     distance = 6371 × c  (in km)
   
   - Filter shops within radius
   - Sort by trending_score DESC, distance ASC
   
2. If no location:
   - Return top 10 shops by trending_score globally
   
3. Limit to top 10 results
```

**Implementation**: `ShopRepository.findNearbyTrendingShops()`, `PersonalizedFeedService.buildTrendingNearbySection()`

#### 5.3 Based on Favorites
**Purpose**: Category preference learning

**Algorithm**:
```
1. Get user's favorite shops
2. Extract preferred categories from favorites
3. Create exclusion list (already favorited shop IDs)
4. Query shops:
   - WHERE category IN (preferred_categories)
   - AND id NOT IN (exclusion_list)
   - ORDER BY trending_score DESC, rating_avg DESC
   
5. If no favorites:
   - Fallback to top trending shops
   
6. Limit to top 10 results
```

**Implementation**: `PersonalizedFeedService.buildBasedOnFavoritesSection()`

#### 5.4 New Shops
**Purpose**: Discovery of recently added shops

**Algorithm**:
```
1. Define "new" as created within last 30 days
2. Get user's preferred categories (from favorites)
3. Query shops:
   - WHERE created_at >= (now - 30 days)
   - AND category IN (preferred_categories) [if available]
   - ORDER BY created_at DESC
   
4. If no preferences:
   - Return all new shops sorted by creation date
   
5. Limit to top 10 results
```

**Implementation**: `ShopRepository.findRecentShopsByCategories()`, `PersonalizedFeedService.buildNewShopsSection()`

---

### 5.5 User Segmentation (RFM Model)

**Purpose**: Classify users by engagement level

**Algorithm**:
```
1. Calculate metrics:
   - daysSinceRegistration = now - user.created_at
   - totalActivities = count(user_activities)
   - totalFavorites = count(favorites)
   - totalReviews = count(reviews)
   - recentActivities = count(activities in last 30 days)

2. Classification rules:
   IF daysSinceRegistration <= 7:
     RETURN NEW_USER
   
   ELSE IF recentActivities == 0 AND totalActivities > 0:
     RETURN DORMANT
   
   ELSE:
     engagementScore = calculateEngagementScore()
     IF engagementScore >= 50:
       RETURN POWER_USER
     ELSE:
       RETURN CASUAL

3. Engagement Score Formula (0-100):
   activityScore = min(totalActivities / 100, 1.0) × 40
   favoriteScore = min(totalFavorites / 20, 1.0) × 30
   reviewScore = min(totalReviews / 10, 1.0) × 30
   
   engagementScore = activityScore + favoriteScore + reviewScore
```

**Segments**:
- **NEW_USER**: Registered within 7 days
- **CASUAL**: Low engagement (score < 50)
- **POWER_USER**: High engagement (score ≥ 50)
- **DORMANT**: No activity in 30 days

**Implementation**: `UserSegmentationService.classifyUser()`

---

### 5.6 Dynamic Badge Assignment

**Purpose**: Highlight special shop characteristics

**Algorithm**:
```
badges = []

// TRENDING_NOW: High recent activity
IF shop.trending_score > 50:
  badges.add(TRENDING_NOW)

// NEW: Recently added
IF days_since_creation <= 30:
  badges.add(NEW)

// HIDDEN_GEM: High quality, low visibility
IF shop.rating_avg >= 4.0 AND shop.view_count < 100:
  badges.add(HIDDEN_GEM)

// CROWD_FAVORITE: Many reviews
IF shop.rating_count > 50:
  badges.add(CROWD_FAVORITE)

// RISING_STAR: New shop gaining traction
IF days_since_creation <= 60 AND shop.trending_score > 30:
  badges.add(RISING_STAR)

RETURN badges
```

**Badge Types**:
- 🔥 **TRENDING_NOW**: High trending score (> 50)
- ✨ **NEW**: Created within 30 days
- 💎 **HIDDEN_GEM**: Rating ≥ 4.0, views < 100
- ❤️ **CROWD_FAVORITE**: Review count > 50
- 🌟 **RISING_STAR**: Age ≤ 60 days, trending > 30

**Implementation**: `PersonalizedFeedService.calculateShopBadges()`

---

### 5.7 Relevance Score

**Purpose**: Quantify recommendation quality (0-100)

**Algorithm**:
```
score = 0

// Rating contribution (40%)
IF shop.rating_avg exists:
  score += (shop.rating_avg / 5.0) × 40

// Trending contribution (40%)
IF shop.trending_score exists:
  score += min(shop.trending_score / 100.0, 1.0) × 40

// Review count contribution (20%)
IF shop.rating_count exists:
  score += min(shop.rating_count / 50.0, 1.0) × 20

RETURN min(score, 100.0)
```

**Weights**:
- Rating quality: 40%
- Trending momentum: 40%
- Review volume: 20%

**Implementation**: `PersonalizedFeedService.calculateRelevanceScore()`

---

### Response Example

```json
{
  "success": true,
  "data": {
    "forYouNow": {
      "title": "For You Now",
      "description": "Perfect for breakfast",
      "shops": [
        {
          "id": 1,
          "name": "Morning Brew Cafe",
          "category": "Cafe",
          "ratingAvg": 4.5,
          "distanceKm": 1.2,
          "badges": ["TRENDING_NOW", "CROWD_FAVORITE"],
          "relevanceScore": 85.5,
          "matchReason": "Perfect for breakfast"
        }
      ]
    },
    "trendingNearby": { ... },
    "basedOnFavorites": { ... },
    "newShops": { ... },
    "metadata": {
      "userSegment": "POWER_USER",
      "timeContext": "BREAKFAST",
      "locationUsed": true
    }
  }
}
```

---

### Files Involved

**Services**:
- `PersonalizedFeedService.java` - Main orchestration
- `TimeContextService.java` - Time-based logic
- `UserSegmentationService.java` - User classification

**Controller**:
- `PersonalizedFeedController.java` - REST endpoints

**DTOs**:
- `PersonalizedFeedDTO.java`, `FeedSectionDTO.java`, `ShopFeedItemDTO.java`, `FeedMetadataDTO.java`

**Enums**:
- `FeedSectionType.java`, `UserSegment.java`, `TimeContext.java`, `ShopBadge.java`

---

## 6. Shop ETA Calculation (Thailand Context)
**Goal:** Provide a safe, realistic time range for travel in heavy traffic conditions (Bangkok context) without incurring external API costs.

**Algorithm: "Heavy Traffic Baseline"**
Instead of using standard average speeds (which are too optimistic for Bangkok), we assume a "Traffic Jam" baseline.

**Formula:**
- **Base Speed:** `15 km/h` (Average speed in heavy city traffic).
- **Time Calculation:** `Time = (Distance / 15) * 60` minutes.
- **Range Variance:** `+/- 15%` to account for red light luck or minor flow improvements.

**Logic:**
1.  **Min ETA:** `Base Time * 0.85`
2.  **Max ETA:** `Base Time * 1.15`
3.  **Safety:** Ensure `Max > Min` by at least 3 minutes.

**Examples:**
- **2 km:** ~8 mins baseline -> **7 - 9 min**
- **5 km:** ~20 mins baseline -> **17 - 23 min**
- **10 km:** ~40 mins baseline -> **34 - 46 min**

*Note: This logic is intentionally pessimistic to ensure users are not disappointed by "10 min" promises that take 40 mins.*

---

## 7. 📖 How it Works (Simplified for End Users)

### The "Smart Feed" Explained
The home screen is not just a random list. It is a smart mix of 4 sections designed to give you exactly what you need, when you need it.

#### 1. For You Now (Context)
*   **"The Right Food at the Right Time"**
*   **How it works:** Uses the **Clock**.
*   **Example:** If you open the app at 8 AM, we show you Coffee & Breakfast spots. If it's 7 PM, we show you Dinner & Drinks. It changes throughout the day to match your life.

#### 2. Trending Nearby (Social)
*   **"What Everyone Else Likes"**
*   **How it works:** Uses **Location + Popularity**.
*   **Example:** This shows you the "hot spots" in your area right now. If a new bubble tea shop becomes famous nearby, you will see it here immediately.

#### 3. New Shops (Discovery)
*   **"Discover Fresh Spots"**
*   **How it works:** Uses **Date Added + Location**.
*   **Example:** This ignores your past history and shows you **everything new** within 5km. This ensures you are never stuck in a "bubble" and always see new businesses opening around you.

#### 4. Based on Favorites (Personal)
*   **"Things You'll Love"**
*   **How it works:** Uses **Your History**.
*   **Example:** If you often like Spicy Noodle shops, this section will find other similar Spicy Noodle shops you haven't tried yet.
*   **Note:** If you are a new user, this section automatically shows the **Top 10 Best Shops** overall, so it's never empty.

---

### FAQ

**Q: Will I only see food I've liked before?**
**A: No.** Only section #4 is strictly based on your history. Sections #2 (Trending) and #3 (New Shops) are designed to show you things you *haven't* seen before, ensuring you always discover new places.

**Q: Why does the order change?**
**A: Because the city changes.** A quiet cafe might be #1 in the morning, but a busy bar will be #1 at night. The app breathes with the city.
