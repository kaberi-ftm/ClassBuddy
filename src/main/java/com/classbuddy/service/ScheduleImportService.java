package com.classbuddy.service;

import com.classbuddy.model.Routine;
import com.classbuddy.util.DatabaseUtil;
import com.classbuddy.util.TimeOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ScheduleImportService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final List<String> DAYS = List.of("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");

    public static ImportResult importSchedule(int classroomId, int userId, Path file) throws IOException {
        ImportResult result = new ImportResult();
        List<ParsedRow> rows = parseFile(file, result.errors);
        if (!result.errors.isEmpty()) {
            return result;
        }

        Map<String, List<RoutineSlot>> existingRoutines = new HashMap<>();
        Map<String, List<RoutineSlot>> pendingRoutines = new HashMap<>();

        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement insertRoutine = conn.prepareStatement(
                         "INSERT INTO routine (classroom_id, day, applicable_days, period_number, course_name, teacher_name, room, time_start, time_end) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
                 PreparedStatement insertExam = conn.prepareStatement(
                         "INSERT INTO exam (classroom_id, course_name, exam_type, exam_date, exam_time, room) VALUES (?, ?, ?, ?, ?, ?)");
                 PreparedStatement insertCT = conn.prepareStatement(
                         "INSERT INTO ct_quiz (classroom_id, name, syllabus, deadline) VALUES (?, ?, ?, ?)");
                 PreparedStatement insertLab = conn.prepareStatement(
                         "INSERT INTO lab_test (classroom_id, test_date, experiment_number, teacher_name, evaluation_criteria) VALUES (?, ?, ?, ?, ?)");
                 PreparedStatement insertNotice = conn.prepareStatement(
                         "INSERT INTO notice (classroom_id, title, content, category, created_by, created_at, is_pinned) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?)")) {

                for (ParsedRow row : rows) {
                    switch (row.type) {
                        case ROUTINE -> {
                            if (isRoutineConflict(classroomId, row, existingRoutines, pendingRoutines)) {
                                result.conflicts.add("Routine conflict on " + row.day + " " + row.timeStart + "-" + row.timeEnd + " (line " + row.lineNumber + ")");
                                continue;
                            }

                            insertRoutine.setInt(1, classroomId);
                            insertRoutine.setString(2, row.day);
                            insertRoutine.setString(3, "[\"" + row.day + "\"]");
                            insertRoutine.setInt(4, row.period);
                            insertRoutine.setString(5, row.course);
                            insertRoutine.setString(6, row.teacher);
                            insertRoutine.setString(7, row.room);
                            insertRoutine.setTime(8, java.sql.Time.valueOf(row.timeStart));
                            insertRoutine.setTime(9, java.sql.Time.valueOf(row.timeEnd));
                            insertRoutine.addBatch();

                            pendingRoutines.computeIfAbsent(row.day, k -> new ArrayList<>())
                                    .add(new RoutineSlot(row.period, row.timeStart, row.timeEnd));
                            result.routinesImported++;
                        }
                        case EXAM -> {
                            insertExam.setInt(1, classroomId);
                            insertExam.setString(2, row.course);
                            insertExam.setString(3, row.metadata.getOrDefault("exam_type", "Exam"));
                            insertExam.setDate(4, java.sql.Date.valueOf(row.date));
                            insertExam.setTime(5, java.sql.Time.valueOf(row.timeStart));
                            insertExam.setString(6, row.room);
                            insertExam.addBatch();
                            result.examsImported++;
                        }
                        case CT -> {
                            insertCT.setInt(1, classroomId);
                            insertCT.setString(2, row.metadata.getOrDefault("name", row.course == null ? "CT" : row.course));
                            insertCT.setString(3, row.course == null ? "" : row.course);
                            insertCT.setDate(4, java.sql.Date.valueOf(row.date));
                            insertCT.addBatch();
                            result.ctImported++;
                        }
                        case LAB -> {
                            insertLab.setInt(1, classroomId);
                            insertLab.setDate(2, java.sql.Date.valueOf(row.date));
                            insertLab.setString(3, row.metadata.getOrDefault("experiment", ""));
                            insertLab.setString(4, row.metadata.getOrDefault("teacher", row.teacher));
                            insertLab.setString(5, row.metadata.getOrDefault("criteria", ""));
                            insertLab.addBatch();
                            result.labImported++;
                        }
                        case NOTICE -> {
                            insertNotice.setInt(1, classroomId);
                            insertNotice.setString(2, row.course == null ? "Notice" : row.course);
                            insertNotice.setString(3, "Imported from CSV");
                            insertNotice.setString(4, row.metadata.getOrDefault("category", "General"));
                            insertNotice.setInt(5, userId);
                            insertNotice.setBoolean(6, Boolean.parseBoolean(row.metadata.getOrDefault("pinned", "false")));
                            insertNotice.addBatch();
                            result.noticesImported++;
                        }
                        default -> {
                        }
                    }
                }

                if (!result.conflicts.isEmpty()) {
                    conn.rollback();
                    return result;
                }

                insertRoutine.executeBatch();
                insertExam.executeBatch();
                insertCT.executeBatch();
                insertLab.executeBatch();
                insertNotice.executeBatch();
                conn.commit();
                result.success = result.errors.isEmpty();
                return result;
            } catch (SQLException e) {
                conn.rollback();
                result.errors.add(e.getMessage());
                return result;
            }

        } catch (SQLException e) {
            result.errors.add(e.getMessage());
            return result;
        }
    }

    private static boolean isRoutineConflict(int classroomId, ParsedRow row,
                                             Map<String, List<RoutineSlot>> existing,
                                             Map<String, List<RoutineSlot>> pending) {
        if (row.timeStart == null || row.timeEnd == null) return true;
        if (!existing.containsKey(row.day)) {
            List<Routine> fetched = RoutineService.getDayRoutine(classroomId, row.day);
            List<RoutineSlot> slots = new ArrayList<>();
            for (Routine r : fetched) {
                slots.add(new RoutineSlot(r.getPeriodNumber(), r.getTimeStart(), r.getTimeEnd()));
            }
            existing.put(row.day, slots);
        }

        List<RoutineSlot> collisions = new ArrayList<>();
        collisions.addAll(existing.getOrDefault(row.day, List.of()));
        collisions.addAll(pending.getOrDefault(row.day, List.of()));

        for (RoutineSlot slot : collisions) {
            if (overlaps(slot.start, slot.end, row.timeStart, row.timeEnd)) {
                return true;
            }
            if (slot.period > 0 && row.period > 0 && slot.period == row.period) {
                return true;
            }
        }
        return false;
    }

    private static boolean overlaps(LocalTime s1, LocalTime e1, LocalTime s2, LocalTime e2) {
        if (s1 == null || e1 == null || s2 == null || e2 == null) return true;
        return s1.isBefore(e2) && s2.isBefore(e1);
    }

    private static List<ParsedRow> parseFile(Path file, List<String> errors) throws IOException {
        List<ParsedRow> rows = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String line;
            int lineNo = 0;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;

                List<String> cells = parseCsvLine(trimmed);
                if (cells.size() < 9) {
                    errors.add("Line " + lineNo + ": expected 9 columns, found " + cells.size());
                    continue;
                }

                ParsedRow row = parseRow(cells, lineNo, errors);
                if (row != null) rows.add(row);
            }
        }
        return rows;
    }

    private static ParsedRow parseRow(List<String> cells, int lineNo, List<String> errors) {
        String typeRaw = cells.get(0).trim().toUpperCase(Locale.ENGLISH);
        RowType type;
        try {
            type = RowType.valueOf(typeRaw);
        } catch (IllegalArgumentException ex) {
            errors.add("Line " + lineNo + ": unknown Type '" + typeRaw + "'");
            return null;
        }

        ParsedRow row = new ParsedRow();
        row.type = type;
        row.lineNumber = lineNo;

        try {
            switch (type) {
                case ROUTINE -> parseRoutine(cells, row);
                case EXAM -> parseExam(cells, row);
                case CT -> parseCT(cells, row);
                case LAB -> parseLab(cells, row);
                case NOTICE -> parseNotice(cells, row);
                default -> {
                }
            }
        } catch (IllegalArgumentException ex) {
            errors.add("Line " + lineNo + ": " + ex.getMessage());
            return null;
        }

        return row;
    }

    private static void parseRoutine(List<String> cells, ParsedRow row) {
        String day = normalizeDay(cells.get(1));
        if (day == null) throw new IllegalArgumentException("Invalid day: " + cells.get(1));

        row.day = day;
        row.period = safeInt(cells.get(2));
        row.course = cells.get(3);
        row.teacher = cells.get(4);
        row.room = cells.get(5);
        row.timeStart = TimeOptions.parseHHmm(cells.get(6));
        row.timeEnd = TimeOptions.parseHHmm(cells.get(7));
        if (!row.timeStart.isBefore(row.timeEnd)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
        row.metadata = parseMetadata(cells.get(8));
    }

    private static void parseExam(List<String> cells, ParsedRow row) {
        row.date = LocalDate.parse(cells.get(1), DATE_FORMAT);
        row.course = cells.get(3);
        row.room = cells.get(5);
        row.timeStart = TimeOptions.parseHHmm(cells.get(6));
        row.metadata = parseMetadata(cells.get(8));
    }

    private static void parseCT(List<String> cells, ParsedRow row) {
        row.date = LocalDate.parse(cells.get(1), DATE_FORMAT);
        row.course = cells.get(3);
        row.metadata = parseMetadata(cells.get(8));
    }

    private static void parseLab(List<String> cells, ParsedRow row) {
        row.date = LocalDate.parse(cells.get(1), DATE_FORMAT);
        row.teacher = cells.get(4);
        row.metadata = parseMetadata(cells.get(8));
    }

    private static void parseNotice(List<String> cells, ParsedRow row) {
        row.date = LocalDate.parse(cells.get(1), DATE_FORMAT);
        row.course = cells.get(3);
        row.metadata = parseMetadata(cells.get(8));
    }

    private static Map<String, String> parseMetadata(String raw) {
        Map<String, String> map = new HashMap<>();
        if (raw == null || raw.isBlank()) return map;

        String[] parts = raw.split("[;]");
        for (String part : parts) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2) {
                map.put(kv[0].trim().toLowerCase(Locale.ENGLISH), kv[1].trim());
            }
        }
        return map;
    }

    private static List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        values.add(current.toString());
        return values;
    }

    private static String normalizeDay(String raw) {
        if (raw == null) return null;
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) return null;
        for (String d : DAYS) {
            if (d.equalsIgnoreCase(trimmed)) {
                return d;
            }
        }
        return null;
    }

    private static int safeInt(String value) {
        if (value == null || value.isBlank()) return 0;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private enum RowType { ROUTINE, EXAM, CT, LAB, NOTICE }

    private static class ParsedRow {
        RowType type;
        int lineNumber;
        String day;
        LocalDate date;
        int period;
        String course;
        String teacher;
        String room;
        LocalTime timeStart;
        LocalTime timeEnd;
        Map<String, String> metadata = new HashMap<>();
    }

    private record RoutineSlot(int period, LocalTime start, LocalTime end) {}

    public static class ImportResult {
        public boolean success;
        public int routinesImported;
        public int examsImported;
        public int ctImported;
        public int labImported;
        public int noticesImported;
        public final List<String> conflicts = new ArrayList<>();
        public final List<String> errors = new ArrayList<>();

        @Override
        public String toString() {
            return "Imported routines=" + routinesImported + ", exams=" + examsImported +
                    ", ct=" + ctImported + ", labs=" + labImported + ", notices=" + noticesImported;
        }
    }
}
