package org.th.entity.enums;

/**
 * Time-based contexts for recommendations
 */
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Time-based contexts for recommendations
 */
@Getter
@RequiredArgsConstructor
public enum TimeContext {
    /**
     * Early morning (6 AM - 10 AM) - breakfast time
     */
    BREAKFAST("နံနက်စာ"),

    /**
     * Midday (11 AM - 2 PM) - lunch time
     */
    LUNCH("နေ့လည်စာ"),

    /**
     * Evening (5 PM - 9 PM) - dinner time
     */
    DINNER("ညစာ"),

    /**
     * Late night (9 PM - 2 AM) - late night snacks/drinks
     */
    LATE_NIGHT("ညဉ့်နက်စာ"),

    /**
     * Other times or categories that work anytime
     */
    ANYTIME("အချိန်မရွေး");

    private final String labelMm;
}
