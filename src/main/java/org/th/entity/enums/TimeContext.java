package org.th.entity.enums;

/**
 * Time-based contexts for recommendations
 */
public enum TimeContext {
    /**
     * Early morning (6 AM - 10 AM) - breakfast time
     */
    BREAKFAST,

    /**
     * Midday (11 AM - 2 PM) - lunch time
     */
    LUNCH,

    /**
     * Evening (5 PM - 9 PM) - dinner time
     */
    DINNER,

    /**
     * Late night (9 PM - 2 AM) - late night snacks/drinks
     */
    LATE_NIGHT,

    /**
     * Other times or categories that work anytime
     */
    ANYTIME
}
