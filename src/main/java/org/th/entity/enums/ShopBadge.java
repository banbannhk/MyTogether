package org.th.entity.enums;

/**
 * Dynamic badges for shops in the feed
 */
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Dynamic badges for shops in the feed
 */
@Getter
@RequiredArgsConstructor
public enum ShopBadge {
    /**
     * High recent activity (views, favorites, reviews)
     */
    TRENDING_NOW("ရေပန်းစားနေသည်"),

    /**
     * New shop with rapid growth in popularity
     */
    RISING_STAR("အလားအလာရှိ"),

    /**
     * High ratings but relatively low view count
     */
    HIDDEN_GEM("ရတနာသိုက်"),

    /**
     * Recently added shop (within last 30 days)
     */
    NEW("အသစ်"),

    /**
     * High number of favorites and reviews
     */
    CROWD_FAVORITE("လူကြိုက်များ");

    private final String labelMm;
}
