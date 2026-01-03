package com.classbuddy.util;

import java.io.IOException;
import java.sql.*;
import java.sql.*;
import java.io.*;
import java.nio.file.*;

public class DatabaseUtil {

    private static final String DB_DIR =
            Paths.get(System.getProperty("user.dir"), "classbuddy_data").toString();
    private static final String DB_URL =
            "jdbc:sqlite:" + DB_DIR + "/classbuddy.db";

    static {
        try {
            Files.createDirectories(Paths.get(DB_DIR));
        } catch (IOException e) {
            throw new RuntimeException("Failed to create DB directory", e);
        }
    }

    // Get DB connection
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    // Initialize database (run schema.sql)
    public static void initializeDatabase() {
        try (Connection conn = getConnection()) {

            InputStream is = DatabaseUtil.class
                    .getResourceAsStream("/schema.sql");

            if (is == null) {
                throw new RuntimeException("schema.sql not found");
            }

            String sql = new String(is.readAllBytes());

            try (Statement stmt = conn.createStatement()) {
                for (String s : sql.split(";")) {
                    if (!s.trim().isEmpty()) {
                        stmt.execute(s.trim());
                    }
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Database init failed", e);
        }
    }
}
