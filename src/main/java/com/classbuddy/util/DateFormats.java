package com.classbuddy.util;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class DateFormats {

    private DateFormats() {
    }

    private static final Locale LOCALE = Locale.ENGLISH;

    private static final DateTimeFormatter TIME_12H = DateTimeFormatter.ofPattern("hh:mm a", LOCALE);
    private static final DateTimeFormatter DATE_ISO = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DATE_LONG = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", LOCALE);
    private static final DateTimeFormatter DATE_MED = DateTimeFormatter.ofPattern("MMM d, yyyy", LOCALE);
    private static final DateTimeFormatter DATE_SHORT = DateTimeFormatter.ofPattern("MMM d", LOCALE);
    private static final DateTimeFormatter WEEK_HEADER = DateTimeFormatter.ofPattern("EEE M/d", LOCALE);

    public static String time(LocalTime t) {
        if (t == null) return "";
        return t.format(TIME_12H);
    }

    public static String dateIso(LocalDate d) {
        if (d == null) return "";
        return d.format(DATE_ISO);
    }

    public static String dateLong(LocalDate d) {
        if (d == null) return "";
        return d.format(DATE_LONG);
    }

    public static String dateMed(LocalDate d) {
        if (d == null) return "";
        return d.format(DATE_MED);
    }

    public static String dateShort(LocalDate d) {
        if (d == null) return "";
        return d.format(DATE_SHORT);
    }

    public static String weekHeader(LocalDate d) {
        if (d == null) return "";
        return d.format(WEEK_HEADER);
    }

    public static String relativeTime(LocalDateTime dateTime) {
        if (dateTime == null) return "Unknown";

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(dateTime, now);

        if (duration.toMinutes() < 1) return "Just now";
        if (duration.toMinutes() < 60) return duration.toMinutes() + " min ago";
        if (duration.toHours() < 24) return duration.toHours() + " hours ago";
        if (duration.toDays() < 7) return duration.toDays() + " days ago";

        return dateMed(dateTime.toLocalDate());
    }
}
