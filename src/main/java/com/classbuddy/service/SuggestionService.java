package com.classbuddy.service;

import com.classbuddy.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class SuggestionService {

    public enum FieldType {
        COURSE_NAME,
        TEACHER_NAME,
        SUBJECT_CODE,
        ROOM,
        DEPARTMENT,
        SECTION,
        NOTICE_CATEGORY,
        NOTICE_TITLE,
        LABTEST_NAME,
        TEST_NAME,
        CLASS_NAME
    }

    private static final String CREATE_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS suggestions (" +
            " id INTEGER PRIMARY KEY AUTOINCREMENT," +
            " field_type TEXT NOT NULL," +
            " value TEXT NOT NULL," +
            " usage_count INTEGER NOT NULL DEFAULT 1," +
            " last_used_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
            " UNIQUE(field_type, value)" +
            ")";

    static {
        ensureTable();
    }

    private static void ensureTable() {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(CREATE_TABLE_SQL)) {
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<String> getSuggestions(FieldType type, String prefix, int limit) {
        List<String> list = new ArrayList<>();
        if (type == null || prefix == null) return list;
        String norm = normalize(prefix);
        if (norm.length() < 2) return list;

        // Fuzzy: contains match with usage/recency priority
        String sql = "SELECT value FROM suggestions WHERE field_type = ? AND LOWER(value) LIKE ? " +
            " ORDER BY usage_count DESC, last_used_at DESC, LENGTH(value) ASC LIMIT ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, type.name());
            ps.setString(2, "%" + norm + "%");
            ps.setInt(3, Math.max(1, limit));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getString("value"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void recordValue(FieldType type, String value) {
        if (type == null || value == null) return;
        String cleaned = canonical(value);
        if (cleaned.isEmpty()) return;

        String insertSql = "INSERT OR IGNORE INTO suggestions(field_type, value, usage_count, last_used_at) VALUES(?, ?, 1, ?)";
        String updateSql = "UPDATE suggestions SET usage_count = usage_count + 1, last_used_at = ? WHERE field_type = ? AND value = ?";
        Timestamp now = Timestamp.from(Instant.now());
        try (Connection conn = DatabaseUtil.getConnection()) {
            try (PreparedStatement ins = conn.prepareStatement(insertSql)) {
                ins.setString(1, type.name());
                ins.setString(2, cleaned);
                ins.setTimestamp(3, now);
                ins.executeUpdate();
            }
            try (PreparedStatement upd = conn.prepareStatement(updateSql)) {
                upd.setTimestamp(1, now);
                upd.setString(2, type.name());
                upd.setString(3, cleaned);
                upd.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String normalize(String s) {
        return s.toLowerCase().trim().replaceAll("\\s+", " ");
    }

    private static String canonical(String s) {
        return s == null ? "" : s.trim().replaceAll("\\s+", " ");
    }
}
