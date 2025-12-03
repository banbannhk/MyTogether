package org.th.entity.enums;

/**
 * Types of user activities to track
 */
public enum ActivityType {
    /**
     * User searched for text
     */
    SEARCH_QUERY,

    /**
     * User viewed shop details
     */
    VIEW_SHOP,

    /**
     * User filtered by category
     */
    VIEW_CATEGORY,

    /**
     * User used "nearby" feature
     */
    VIEW_NEARBY,

    /**
     * User requested directions (future use)
     */
    CLICK_DIRECTIONS,

    /**
     * User clicked to call (future use)
     */
    /**
     * User clicked to call (future use)
     */
    CLICK_CALL,

    // Conversion Events (High Intent)
    CLICK_WEBSITE,
    CLICK_SHARE
}
