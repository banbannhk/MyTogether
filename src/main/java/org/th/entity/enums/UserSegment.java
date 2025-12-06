package org.th.entity.enums;

/**
 * User engagement segments based on activity patterns
 */
public enum UserSegment {
    /**
     * User registered within last 7 days
     */
    NEW_USER,

    /**
     * User with low activity (occasional usage)
     */
    CASUAL,

    /**
     * User with high engagement (frequent usage, reviews, favorites)
     */
    POWER_USER,

    /**
     * User inactive for more than 30 days
     */
    DORMANT
}
