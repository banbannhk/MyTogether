-- Performance Optimization Indexes
-- Run this SQL script to add critical indexes

-- shops table indexes
CREATE INDEX IF NOT EXISTS idx_shops_category_active ON shops(category, is_active);
CREATE INDEX IF NOT EXISTS idx_shops_trending_active ON shops(trending_score DESC, is_active) WHERE is_active = true;
CREATE INDEX IF NOT EXISTS idx_shops_created_active ON shops(created_at DESC, is_active) WHERE is_active = true;

-- favorites table
CREATE INDEX IF NOT EXISTS idx_favorites_user_created ON favorites(user_id, created_at DESC);

-- user_activities table
CREATE INDEX IF NOT EXISTS idx_activities_user_created ON user_activities(user_id, created_at DESC);

-- feed_interactions table
CREATE INDEX IF NOT EXISTS idx_feed_section_timestamp ON feed_interactions(section_type, timestamp);
CREATE INDEX IF NOT EXISTS idx_feed_action_section ON feed_interactions(action, section_type);

-- user_sessions table
CREATE INDEX IF NOT EXISTS idx_sessions_start_between ON user_sessions(session_start);

-- Composite index for common queries
CREATE INDEX IF NOT EXISTS idx_shops_active_trending ON shops(is_active, trending_score DESC) WHERE is_active = true;
