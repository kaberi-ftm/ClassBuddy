package com.classbuddy.util;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.List;

public class MigrationRunner {

    private static final List<String> MIGRATIONS = List.of(
            "/migrations/001_roll_classid_enrollment.sql",
            "/migrations/002_expand_exam_type.sql",
            "/migrations/003_assessment_results.sql"
    );

    public static void run(Connection conn) {
        ensureMigrationsTable(conn);

        for (String path : MIGRATIONS) {
            if (isApplied(conn, path)) continue;

            String sql = readResource(path);
            if (sql == null || sql.trim().isEmpty()) {
                markApplied(conn, path);
                continue;
            }

            applySql(conn, sql);
            markApplied(conn, path);
        }
    }

    private static void ensureMigrationsTable(Connection conn) {
        String sql = "CREATE TABLE IF NOT EXISTS schema_migrations (" +
                "id TEXT PRIMARY KEY, " +
                "applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to init schema_migrations", e);
        }
    }

    private static boolean isApplied(Connection conn, String id) {
        String sql = "SELECT 1 FROM schema_migrations WHERE id = ? LIMIT 1";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Migration lookup failed: " + e.getMessage());
            return false;
        }
    }

    private static void markApplied(Connection conn, String id) {
        String sql = "INSERT OR IGNORE INTO schema_migrations (id) VALUES (?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to mark migration applied: " + e.getMessage());
        }
    }

    private static void applySql(Connection conn, String sql) {
        try (Statement stmt = conn.createStatement()) {
            for (String s : sql.split(";")) {
                String part = s.trim();
                if (part.isEmpty()) continue;

                try {
                    stmt.execute(part);
                } catch (SQLException e) {
                    String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";

                    if (msg.contains("duplicate column")
                            || msg.contains("already exists")
                            || msg.contains("duplicate")
                            || msg.contains("no such table")) {
                        // ignore - migration is best-effort / idempotent-ish
                        continue;
                    }

                    throw e;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Migration failed", e);
        }
    }

    private static String readResource(String path) {
        try (InputStream is = MigrationRunner.class.getResourceAsStream(path)) {
            if (is == null) return null;
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read migration: " + path, e);
        }
    }
}
