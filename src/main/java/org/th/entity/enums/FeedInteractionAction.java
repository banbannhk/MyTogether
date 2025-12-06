package org.th.entity.enums;

/**
 * Types of interactions with feed items
 */
public enum FeedInteractionAction {
    /**
     * Feed section was displayed to user
     */
    VIEWED,

    /**
     * User clicked on a shop in the feed
     */
    CLICKED,

    /**
     * User scrolled past without clicking
     */
    SCROLLED_PAST,

    /**
     * Shop was shown but user didn't interact
     */
    IGNORED
}
