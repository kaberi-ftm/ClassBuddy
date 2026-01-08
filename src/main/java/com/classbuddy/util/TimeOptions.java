package com.classbuddy.util;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;

public final class TimeOptions {

    private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter H_MM_A = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);
    private static final DateTimeFormatter FLEX_12H = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern("h:mm")
            .optionalStart()
            .appendLiteral(' ')
            .optionalEnd()
            .appendPattern("a")
            .toFormatter(Locale.ENGLISH);

    private TimeOptions() {
    }

    public static List<String> defaultTimes() {
        // 10-minute increments keep common times (e.g., 08:50) selectable.
        LocalTime start = LocalTime.of(6, 0);
        LocalTime end = LocalTime.of(22, 0);

        List<String> items = new ArrayList<>();
        for (LocalTime t = start; !t.isAfter(end); t = t.plusMinutes(10)) {
            items.add(t.format(H_MM_A));
        }
        return items;
    }

    public static String format12h(LocalTime time) {
        if (time == null) return "";
        return time.format(H_MM_A);
    }

    public static LocalTime parseHHmm(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Time is required");
        }

        String trimmed = value.trim();

        // Accept both 12h (e.g., 2:00 PM) and 24h (e.g., 14:00) inputs.
        try {
            if (trimmed.toLowerCase(Locale.ENGLISH).contains("am") || trimmed.toLowerCase(Locale.ENGLISH).contains("pm")) {
                return LocalTime.parse(trimmed.replaceAll("\\s+", " "), FLEX_12H);
            }

            if (!trimmed.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$")) {
                throw new IllegalArgumentException("Invalid time format. Use h:mm AM/PM (e.g., 2:00 PM)");
            }

            // Normalize to HH:mm for strict parsing
            String normalized = trimmed.length() == 4 ? ("0" + trimmed) : trimmed;
            return LocalTime.parse(normalized, HH_MM);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid time format. Use h:mm AM/PM (e.g., 2:00 PM)");
        }
    }
}
