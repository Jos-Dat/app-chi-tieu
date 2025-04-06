package com.example.campusexpensemanagement.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateTimeUtils {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    private static final SimpleDateFormat DATE_SHORT_FORMAT = new SimpleDateFormat("dd/MM", Locale.getDefault());
    private static final SimpleDateFormat MONTH_YEAR_FORMAT = new SimpleDateFormat("MM/yyyy", Locale.getDefault());

    /**
     * Format date as DD/MM/YYYY
     */
    public static String formatDate(long timestamp) {
        return DATE_FORMAT.format(new Date(timestamp));
    }

    /**
     * Format date as DD/MM/YYYY HH:MM
     */
    public static String formatDateTime(long timestamp) {
        return DATE_TIME_FORMAT.format(new Date(timestamp));
    }

    /**
     * Format date as DD/MM
     */
    public static String formatDateShort(long timestamp) {
        return DATE_SHORT_FORMAT.format(new Date(timestamp));
    }

    /**
     * Format date as MM/YYYY
     */
    public static String formatMonthYear(long timestamp) {
        return MONTH_YEAR_FORMAT.format(new Date(timestamp));
    }

    /**
     * Get relative time span as text (e.g., "Just now", "1 hour ago")
     */
    public static String getRelativeTimeSpan(long timestamp) {
        long currentTime = System.currentTimeMillis();
        long timeDiff = currentTime - timestamp;

        // Convert to seconds
        long diffSeconds = timeDiff / 1000;

        if (diffSeconds < 60) {
            return "Just finished";
        }

        // Convert to minutes
        long diffMinutes = diffSeconds / 60;

        if (diffMinutes < 60) {
            return diffMinutes + " minutes ago";
        }

        // Convert to hours
        long diffHours = diffMinutes / 60;

        if (diffHours < 24) {
            return diffHours + " hours ago";
        }

        // Convert to days
        long diffDays = diffHours / 24;

        if (diffDays < 7) {
            return diffDays + " days ago";
        }

        // If more than a week, just show the date
        return formatDate(timestamp);
    }
}