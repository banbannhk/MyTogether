package org.th.entity.enums;

/**
 * Dynamic badges for shops in the feed
 */
public enum ShopBadge {
    /**
     * High recent activity (views, favorites, reviews)
     */
    TRENDING_NOW,

    /**
     * New shop with rapid growth in popularity
     */
    RISING_STAR,

    /**
     * High ratings but relatively low view count
     */
    HIDDEN_GEM,

    /**
     * Recently added shop (within last 30 days)
     */
    NEW,

    /**
     * High number of favorites and reviews
     */
    CROWD_FAVORITE
}
