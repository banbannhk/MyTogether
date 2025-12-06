package org.th.entity.enums;

/**
 * Entry points for user sessions
 */
public enum SessionEntryPoint {
    /**
     * Direct app launch
     */
    DIRECT,

    /**
     * From search engine or web search
     */
    SEARCH,

    /**
     * From push notification
     */
    NOTIFICATION,

    /**
     * Deep link from external source
     */
    DEEP_LINK,

    /**
     * From social media share
     */
    SOCIAL_SHARE,

    /**
     * Unknown or not tracked
     */
    UNKNOWN
}
