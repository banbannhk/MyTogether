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
     * User requested directions
     */
    CLICK_DIRECTIONS,

    /**
     * User clicked call button
     */
    CLICK_CALL,

    /**
     * User clicked website button
     */
    CLICK_WEBSITE,

    /**
     * User clicked share button
     */
    CLICK_SHARE
}
