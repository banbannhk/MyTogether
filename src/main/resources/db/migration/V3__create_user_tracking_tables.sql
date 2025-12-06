-- Migration script for user tracking features
-- Creates tables for favorites, preferences, locations, and visits

-- ========================================
-- USER_FAVORITES TABLE
-- ========================================
CREATE TABLE IF NOT EXISTS user_favorites (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    shop_id BIGINT NOT NULL REFERENCES shops(id) ON DELETE CASCADE,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, shop_id)
);

CREATE INDEX idx_favorite_user ON user_favorites(user_id);
CREATE INDEX idx_favorite_shop ON user_favorites(shop_id);
CREATE INDEX idx_favorite_created ON user_favorites(created_at);

-- ========================================
-- USER_PREFERENCES TABLE
-- ========================================
CREATE TABLE IF NOT EXISTS user_preferences (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    favorite_categories VARCHAR(500),
    favorite_cuisines VARCHAR(500),
    price_range_min INTEGER,
    price_range_max INTEGER,
    dietary_restrictions VARCHAR(500),
    preferred_radius_km DECIMAL(5,2) DEFAULT 5.0,
    preferred_language VARCHAR(10) DEFAULT 'en',
    receive_notifications BOOLEAN DEFAULT TRUE,
    notification_distance_km DECIMAL(5,2) DEFAULT 1.0,
    notify_new_shops BOOLEAN DEFAULT TRUE,
    notify_favorite_updates BOOLEAN DEFAULT TRUE,
    notify_special_offers BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_preferences_user ON user_preferences(user_id);

-- ========================================
-- USER_LOCATIONS TABLE
-- ========================================
CREATE TABLE IF NOT EXISTS user_locations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    location_name VARCHAR(255),
    location_type VARCHAR(50),
    visit_count INTEGER DEFAULT 1,
    last_visited_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_location_user ON user_locations(user_id);
CREATE INDEX idx_location_type ON user_locations(location_type);

-- ========================================
-- SHOP_VISITS TABLE
-- ========================================
CREATE TABLE IF NOT EXISTS shop_visits (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    shop_id BIGINT NOT NULL REFERENCES shops(id) ON DELETE CASCADE,
    visited_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    visit_duration_minutes INTEGER,
    was_helpful BOOLEAN,
    notes TEXT,
    check_in_latitude DECIMAL(10, 8),
    check_in_longitude DECIMAL(11, 8)
);

CREATE INDEX idx_visit_user ON shop_visits(user_id);
CREATE INDEX idx_visit_shop ON shop_visits(shop_id);
CREATE INDEX idx_visit_date ON shop_visits(visited_at);

-- ========================================
-- UPDATE USER_ACTIVITIES TABLE
-- Add new columns if they don't exist
-- ========================================
DO $$
BEGIN
    -- Add shop_id column if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name = 'user_activities' AND column_name = 'shop_id') THEN
        ALTER TABLE user_activities ADD COLUMN shop_id BIGINT REFERENCES shops(id) ON DELETE SET NULL;
        CREATE INDEX idx_activity_shop ON user_activities(shop_id);
    END IF;

    -- Add category column if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name = 'user_activities' AND column_name = 'category') THEN
        ALTER TABLE user_activities ADD COLUMN category VARCHAR(100);
    END IF;

    -- Add session_id column if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name = 'user_activities' AND column_name = 'session_id') THEN
        ALTER TABLE user_activities ADD COLUMN session_id VARCHAR(255);
        CREATE INDEX idx_activity_session ON user_activities(session_id);
    END IF;
END $$;

-- ========================================
-- COMMENTS FOR DOCUMENTATION
-- ========================================
COMMENT ON TABLE user_favorites IS 'Stores user bookmarked/favorite shops';
COMMENT ON TABLE user_preferences IS 'Stores user preferences and settings';
COMMENT ON TABLE user_locations IS 'Tracks user frequent locations (home, work, etc.)';
COMMENT ON TABLE shop_visits IS 'Records user check-ins/visits to shops';

COMMENT ON COLUMN user_favorites.notes IS 'Personal notes about why shop was favorited';
COMMENT ON COLUMN user_preferences.favorite_categories IS 'Comma-separated list of favorite categories';
COMMENT ON COLUMN user_preferences.favorite_cuisines IS 'Comma-separated list of favorite cuisines';
COMMENT ON COLUMN user_preferences.dietary_restrictions IS 'Comma-separated list (Vegetarian, Halal, Vegan, etc.)';
COMMENT ON COLUMN user_locations.location_type IS 'HOME, WORK, FREQUENT, or OTHER';
COMMENT ON COLUMN shop_visits.was_helpful IS 'User feedback if the shop visit was helpful';
