package com.classbuddy.util;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class TimetableSlots {

    private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter H_MM_A = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);

    public record Slot(LocalTime start, LocalTime end) {
        public String label() {
            return start.format(H_MM_A) + "–" + end.format(H_MM_A);
        }

        public long minutes() {
            return Duration.between(start, end).toMinutes();
        }
    }

    private TimetableSlots() {
    }

    /**
     * Default slots taken from the provided cycle routine markdown.
     */
    public static List<Slot> defaultSlots() {
        return List.of(
                new Slot(LocalTime.of(8, 50), LocalTime.of(9, 40)),
                new Slot(LocalTime.of(9, 40), LocalTime.of(10, 30)),
                new Slot(LocalTime.of(10, 40), LocalTime.of(11, 30)),
                new Slot(LocalTime.of(11, 30), LocalTime.of(12, 20)),
                new Slot(LocalTime.of(12, 20), LocalTime.of(13, 10)),
                new Slot(LocalTime.of(13, 10), LocalTime.of(14, 30)),
                new Slot(LocalTime.of(14, 30), LocalTime.of(17, 0))
        );
    }

    public static List<Slot> mergeWithDefaults(List<Slot> discovered) {
        Set<Slot> merged = new LinkedHashSet<>();
        merged.addAll(defaultSlots());
        if (discovered != null) merged.addAll(discovered);

        List<Slot> result = new ArrayList<>(merged);
        result.sort(Comparator.comparing(Slot::start).thenComparing(Slot::end));
        return result;
    }

    /**
     * Legacy helper that identifies “typical” break windows.
     *
     * Breaks are now opt-in per-classroom; this method is not used unless
     * explicitly wired back in.
     */
    public static boolean isDefaultBreak(Slot slot) {
        if (slot == null) return false;

        // From the provided routine: midday break windows.
        return (slot.start.equals(LocalTime.of(12, 20)) && slot.end.equals(LocalTime.of(13, 10)))
                || (slot.start.equals(LocalTime.of(13, 10)) && slot.end.equals(LocalTime.of(14, 30)));
    }

    public static boolean isBreak(int classroomId, Slot slot) {
        if (slot == null) return false;
        Set<String> custom = loadBreakKeys(classroomId);
        return custom != null && custom.contains(toKey(slot));
    }

    public static void setBreak(int classroomId, Slot slot, boolean isBreak) {
        if (slot == null) return;
        Set<String> keys = loadBreakKeys(classroomId);
        if (keys == null) keys = new java.util.LinkedHashSet<>();

        String key = toKey(slot);
        if (isBreak) {
            keys.add(key);
        } else {
            keys.remove(key);
        }

        saveBreakKeys(classroomId, keys);
    }

    private static String toKey(Slot slot) {
        return slot.start.format(HH_MM) + "-" + slot.end.format(HH_MM);
    }

    private static Path breakFile(int classroomId) {
        return Paths.get("classbuddy_data", "breaks", "breaks_" + classroomId + ".txt");
    }

    private static Set<String> loadBreakKeys(int classroomId) {
        Path file = breakFile(classroomId);
        if (!Files.exists(file)) {
            return null;
        }
        try {
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            Set<String> keys = new java.util.LinkedHashSet<>();
            for (String line : lines) {
                if (line == null) continue;
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
                keys.add(trimmed);
            }
            return keys;
        } catch (IOException e) {
            return null;
        }
    }

    private static void saveBreakKeys(int classroomId, Set<String> keys) {
        Path file = breakFile(classroomId);
        try {
            Files.createDirectories(file.getParent());
            List<String> lines = new ArrayList<>();
            lines.add("# Break slots for classroom " + classroomId + " (HH:mm-HH:mm)");
            if (keys != null) {
                lines.addAll(keys);
            }
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }
}
