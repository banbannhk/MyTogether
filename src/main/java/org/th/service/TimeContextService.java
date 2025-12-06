package org.th.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.th.entity.enums.TimeContext;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

/**
 * Service for time-based recommendation logic
 */
@Service
@RequiredArgsConstructor
public class TimeContextService {

    /**
     * Determine current time context based on hour of day
     */
    public TimeContext getCurrentTimeContext() {
        LocalTime now = LocalTime.now();
        int hour = now.getHour();

        if (hour >= 6 && hour < 10) {
            return TimeContext.BREAKFAST;
        } else if (hour >= 11 && hour < 14) {
            return TimeContext.LUNCH;
        } else if (hour >= 17 && hour < 21) {
            return TimeContext.DINNER;
        } else if (hour >= 21 || hour < 2) {
            return TimeContext.LATE_NIGHT;
        } else {
            return TimeContext.ANYTIME;
        }
    }

    /**
     * Get relevant shop categories for the current time context
     */
    public List<String> getRelevantCategoriesForTime(TimeContext context) {
        return switch (context) {
            case BREAKFAST -> Arrays.asList(
                    "Cafe", "Coffee Shop", "Bakery", "Breakfast", "Tea Shop");
            case LUNCH -> Arrays.asList(
                    "Restaurant", "Fast Food", "Noodle Shop", "Rice Shop",
                    "Cafe", "Food Court", "Lunch");
            case DINNER -> Arrays.asList(
                    "Restaurant", "Fine Dining", "BBQ", "Hot Pot",
                    "Seafood", "Steakhouse", "Dinner");
            case LATE_NIGHT -> Arrays.asList(
                    "Bar", "Pub", "Night Club", "Late Night Eatery",
                    "24/7", "Street Food");
            case ANYTIME -> Arrays.asList(
                    "Cafe", "Restaurant", "Fast Food", "Dessert",
                    "Ice Cream", "Snacks");
        };
    }

    /**
     * Check if a category is typically open during business hours
     */
    public boolean isBusinessHoursForCategory(String category, TimeContext context) {
        // Most restaurants are open during meal times
        if (context == TimeContext.BREAKFAST || context == TimeContext.LUNCH || context == TimeContext.DINNER) {
            return !category.equalsIgnoreCase("Bar") && !category.equalsIgnoreCase("Night Club");
        }

        // Late night - bars and night clubs are preferred
        if (context == TimeContext.LATE_NIGHT) {
            return category.equalsIgnoreCase("Bar") ||
                    category.equalsIgnoreCase("Night Club") ||
                    category.contains("24") ||
                    category.equalsIgnoreCase("Street Food");
        }

        return true;
    }

    /**
     * Get a human-readable description for the time context
     */
    public String getTimeContextDescription(TimeContext context) {
        return switch (context) {
            case BREAKFAST -> "Perfect for breakfast";
            case LUNCH -> "Great lunch spots";
            case DINNER -> "Dinner recommendations";
            case LATE_NIGHT -> "Open late night";
            case ANYTIME -> "Available anytime";
        };
    }
}
