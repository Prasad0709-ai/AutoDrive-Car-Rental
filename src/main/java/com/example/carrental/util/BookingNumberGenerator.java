package com.example.carrental.util;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class BookingNumberGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    public static String generate() {
        String datePart = LocalDate.now().format(DATE_FORMATTER);
        int randomPart = 10000 + RANDOM.nextInt(90000);
        return "BK-" + datePart + "-" + randomPart;
    }
}
