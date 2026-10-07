package com.example.carrental.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class DateUtils {

    public static long calculateRentalDays(LocalDate pickupDate, LocalDate returnDate) {
        if (pickupDate == null || returnDate == null) {
            return 1;
        }
        long days = ChronoUnit.DAYS.between(pickupDate, returnDate);
        return days > 0 ? days : 1;
    }

    /**
     * Checks if two date intervals overlap.
     * Overlap occurs if start1 <= end2 AND end1 >= start2.
     */
    public static boolean isOverlap(LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2) {
        if (start1 == null || end1 == null || start2 == null || end2 == null) {
            return false;
        }
        return !start1.isAfter(end2) && !end1.isBefore(start2);
    }
}
