-- Optimizing Guest Personalization Queries
CREATE INDEX IF NOT EXISTS idx_activity_device_type ON user_activities(device_id, activity_type);
CREATE INDEX IF NOT EXISTS idx_activity_device_search ON user_activities(device_id, search_query) WHERE search_query IS NOT NULL;
