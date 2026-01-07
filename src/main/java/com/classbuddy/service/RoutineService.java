package com.classbuddy.service;

import com.classbuddy.model.Routine;
import com.classbuddy.util.DatabaseUtil;
import java.sql.*;
import java.time.LocalTime;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for managing class routines/schedules
 */
public class RoutineService {

    private static final List<String> DAY_ORDER = List.of(
            "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
    );

    private static final Pattern JSON_STRING_PATTERN = Pattern.compile("\"([^\"]+)\"");

    /**
     * ADMIN: Add routine entry
     */
    public static boolean addRoutine(int classroomId, String day,
                                     int periodNumber, String courseName,
                                     String teacherName, String room,
                                     LocalTime timeStart, LocalTime timeEnd) {
        return addRoutine(classroomId, List.of(day), periodNumber, courseName, teacherName, room, timeStart, timeEnd);
    }

    /**
     * ADMIN: Add routine entry (multi-day)
     */
    public static boolean addRoutine(int classroomId, List<String> applicableDays,
                                     int periodNumber, String courseName,
                                     String teacherName, String room,
                                     LocalTime timeStart, LocalTime timeEnd) {
        List<String> normalizedDays = normalizeDays(applicableDays);
        if (normalizedDays.isEmpty()) {
            System.err.println("Error adding routine: no applicable days provided");
            return false;
        }

        String primaryDay = normalizedDays.get(0);
        String applicableDaysJson = toJsonArray(normalizedDays);

        String sql = "INSERT INTO routine " +
                "(classroom_id, day, applicable_days, period_number, course_name, " +
                "teacher_name, room, time_start, time_end) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, classroomId);
            pstmt.setString(2, primaryDay);
            pstmt.setString(3, applicableDaysJson);
            pstmt.setInt(4, periodNumber);
            pstmt.setString(5, courseName);
            pstmt.setString(6, teacherName);
            pstmt.setString(7, room);
            pstmt.setTime(8, java.sql.Time.valueOf(timeStart));
            pstmt.setTime(9, java.sql.Time.valueOf(timeEnd));

            pstmt.executeUpdate();
            System.out.println("Routine added");
            return true;

        } catch (SQLException e) {
            System.err.println("Error adding routine: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get routine for a specific day
     */
    public static List<Routine> getDayRoutine(int classroomId, String day) {
        List<Routine> routines = new ArrayList<>();
        String sql = "SELECT * FROM routine WHERE classroom_id = ? ORDER BY period_number ASC";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, classroomId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    List<String> applicableDays = parseApplicableDays(rs.getString("applicable_days"), rs.getString("day"));
                    Routine routine = new Routine(
                            rs.getInt("id"),
                            rs.getInt("classroom_id"),
                            rs.getString("day"),
                            applicableDays,
                            rs.getInt("period_number"),
                            rs.getString("course_name"),
                            rs.getString("teacher_name"),
                            rs.getString("room"),
                            rs.getTime("time_start").toLocalTime(),
                            rs.getTime("time_end").toLocalTime(),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    );
                    routines.add(routine);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching routine: " + e.getMessage());
        }

        return expandAndFilterByDay(routines, day);
    }

    /**
     * Get complete weekly routine
     */
    public static List<Routine> getWeeklyRoutine(int classroomId) {
        List<Routine> routines = new ArrayList<>();
        String sql = "SELECT * FROM routine WHERE classroom_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, classroomId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    List<String> applicableDays = parseApplicableDays(rs.getString("applicable_days"), rs.getString("day"));
                    Routine routine = new Routine(
                            rs.getInt("id"),
                            rs.getInt("classroom_id"),
                            rs.getString("day"),
                            applicableDays,
                            rs.getInt("period_number"),
                            rs.getString("course_name"),
                            rs.getString("teacher_name"),
                            rs.getString("room"),
                            rs.getTime("time_start").toLocalTime(),
                            rs.getTime("time_end").toLocalTime(),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    );
                    routines.add(routine);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching weekly routine: " + e.getMessage());
        }

        List<Routine> expanded = expandMultiDayRoutines(routines);
        expanded.sort(routineComparator());
        return expanded;
    }

    /**
     * Query helper: returns the schedule entries that apply to the given date.
     */
    public static List<Routine> getScheduleForDate(int classroomId, LocalDate date) {
        String dayName = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        return getDayRoutine(classroomId, dayName);
    }

    /**
     * Takes routines with multi-day applicableDays and returns a flat list with one routine per day.
     */
    public static List<Routine> expandMultiDayRoutines(List<Routine> routines) {
        List<Routine> expanded = new ArrayList<>();
        if (routines == null) {
            return expanded;
        }

        for (Routine routine : routines) {
            List<String> days = routine.getApplicableDays();
            if (days == null || days.isEmpty()) {
                expanded.add(routine);
                continue;
            }

            for (String day : days) {
                expanded.add(new Routine(
                        routine.getId(),
                        routine.getClassroomId(),
                        day,
                        List.of(day),
                        routine.getPeriodNumber(),
                        routine.getCourseName(),
                        routine.getTeacherName(),
                        routine.getRoom(),
                        routine.getTimeStart(),
                        routine.getTimeEnd(),
                        null
                ));
            }
        }

        return expanded;
    }

    /**
     * Delete routine entry
     */
    public static boolean deleteRoutine(int routineId) {
        String sql = "DELETE FROM routine WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, routineId);
            pstmt.executeUpdate();
            System.out.println("Routine deleted");
            return true;

        } catch (SQLException e) {
            System.err.println("Error deleting routine: " + e.getMessage());
            return false;
        }
    }
    /**
     * Update routine entry
     */
    public static boolean updateRoutine(int routineId, String day,
                                        int periodNumber, String courseName,
                                        String teacherName, String room,
                                        LocalTime timeStart, LocalTime timeEnd) {
        String sql = "UPDATE routine SET day=?, period_number=?, " +
                "course_name=?, teacher_name=?, room=?, " +
                "time_start=?, time_end=? WHERE id=?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, day);
            pstmt.setInt(2, periodNumber);
            pstmt.setString(3, courseName);
            pstmt.setString(4, teacherName);
            pstmt.setString(5, room);
            pstmt.setTime(6, java.sql.Time.valueOf(timeStart));
            pstmt.setTime(7, java.sql.Time.valueOf(timeEnd));
            pstmt.setInt(8, routineId);

            pstmt.executeUpdate();
            System.out.println("Routine updated");
            return true;

        } catch (SQLException e) {
            System.err.println("Error updating routine: " + e.getMessage());
            return false;
        }
    }

    private static List<String> normalizeDays(List<String> applicableDays) {
        if (applicableDays == null) {
            return List.of();
        }

        List<String> normalized = new ArrayList<>();
        for (String raw : applicableDays) {
            if (raw == null) {
                continue;
            }
            String trimmed = raw.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            String normalizedDay = normalizeDayName(trimmed);
            if (normalizedDay == null) {
                continue;
            }
            if (!normalized.contains(normalizedDay)) {
                normalized.add(normalizedDay);
            }
        }

        normalized.sort(Comparator.comparingInt(RoutineService::dayIndex));
        return normalized;
    }

    private static String normalizeDayName(String day) {
        for (String canonical : DAY_ORDER) {
            if (canonical.equalsIgnoreCase(day)) {
                return canonical;
            }
        }
        return null;
    }

    private static int dayIndex(String day) {
        if (day == null) {
            return 999;
        }
        for (int i = 0; i < DAY_ORDER.size(); i++) {
            if (DAY_ORDER.get(i).equalsIgnoreCase(day)) {
                return i;
            }
        }
        return 999;
    }

    private static Comparator<Routine> routineComparator() {
        return Comparator
                .comparingInt((Routine r) -> dayIndex(r.getDay()))
                .thenComparingInt(Routine::getPeriodNumber)
                .thenComparing(Routine::getTimeStart);
    }

    private static List<Routine> expandAndFilterByDay(List<Routine> routines, String day) {
        List<Routine> expanded = expandMultiDayRoutines(routines);
        List<Routine> dayFiltered = new ArrayList<>();
        for (Routine routine : expanded) {
            if (routine.getDay() != null && routine.getDay().equalsIgnoreCase(day)) {
                dayFiltered.add(routine);
            }
        }
        dayFiltered.sort(Comparator.comparing(Routine::getTimeStart));
        return dayFiltered;
    }

    private static List<String> parseApplicableDays(String applicableDaysRaw, String fallbackDay) {
        String raw = applicableDaysRaw == null ? "" : applicableDaysRaw.trim();
        if (raw.isEmpty()) {
            return normalizeDays(fallbackDay == null ? List.of() : List.of(fallbackDay));
        }

        // JSON array support: ["Monday","Tuesday"]
        if (raw.startsWith("[")) {
            Matcher matcher = JSON_STRING_PATTERN.matcher(raw);
            List<String> extracted = new ArrayList<>();
            while (matcher.find()) {
                extracted.add(matcher.group(1));
            }
            if (!extracted.isEmpty()) {
                return normalizeDays(extracted);
            }
        }

        // Comma-separated support: Monday,Tuesday
        if (raw.contains(",")) {
            return normalizeDays(Arrays.asList(raw.split(",")));
        }

        // Legacy support: single day string (migration backfill sets this)
        return normalizeDays(List.of(raw));
    }

    private static String toJsonArray(List<String> values) {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        boolean first = true;
        for (String value : values) {
            if (value == null) {
                continue;
            }
            if (!first) {
                sb.append(',');
            }
            first = false;
            sb.append('"').append(escapeJson(value)).append('"');
        }
        sb.append(']');
        return sb.toString();
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}