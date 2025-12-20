package com.classbuddy.util;

import java.io.IOException;
import java.sql.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io. InputStream;
import java.nio.file. Paths;

public class DatabaseUtil {
    // Store DB in project root folder (where pom.xml is)
    private static final String DB_PATH = Paths.get(System.getProperty("user.dir"), "classbuddy_data").toString();
    private static final String DB_URL = "jdbc:sqlite:" + DB_PATH + "/classbuddy.db";
    private static boolean initialized = false;

    static {
        // Create directory if it doesn't exist
        java.nio.file.Path path = Paths.get(DB_PATH);
        try {
            if (!java.nio.file.Files.exists(path)) {
                java.nio.file.Files. createDirectories(path);
                System.out.println("✅ Created database directory:  " + DB_PATH);
            }
        } catch (Exception e) {
            System.err.println("❌ Error creating database directory: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get database connection
     */
    public static Connection getConnection() throws SQLException {
        // SQLite JDBC driver auto-loads in Java 8+, no need for Class.forName()
        return DriverManager.getConnection(DB_URL);
    }

    /**
     * Initialize database (create tables if they don't exist)
     */
    public static void initializeDatabase() {
        if (initialized) {
            System.out.println("✅ Database already initialized");
            return;
        }

        System.out.println("\n📍 Database URL: " + DB_URL);
        System.out.println("📍 Database Path: " + getDatabasePath());

        try (Connection conn = getConnection()) {
            System.out.println("✅ Database connection established!");

            // Read schema. sql from resources
            InputStream inputStream = DatabaseUtil.class.getResourceAsStream("/schema.sql");
            if (inputStream == null) {
                System.err.println("❌ schema.sql not found in resources!");
                System.err.println("   Make sure schema.sql is in src/main/resources/");
                return;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sqlBuilder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty() && !line.trim().startsWith("--")) {
                    sqlBuilder.append(line).append("\n");
                }
            }
            reader.close();

            // Split by semicolon and execute each statement
            String[] statements = sqlBuilder.toString().split(";");
            int successCount = 0;
            int errorCount = 0;

            try (Statement stmt = conn.createStatement()) {
                for (String sql : statements) {
                    if (! sql.trim().isEmpty()) {
                        try {
                            stmt.execute(sql. trim());
                            successCount++;
                            System.out.println("   ✅ Executed: " + sql. trim().substring(0, Math.min(50, sql.trim().length())) + "...");
                        } catch (SQLException e) {
                            // Table already exists is not an error
                            if (! e.getMessage().contains("already exists")) {
                                System.err.println("   ⚠️  Error:   " + e.getMessage());
                                errorCount++;
                            }
                        }
                    }
                }
            }

            System.out.println("\n✅ Database initialized successfully!");
            System.out.println("   Statements executed: " + successCount);
            System.out.println("   Errors (if any): " + errorCount);

            // Verify tables exist
            verifyTables(conn);

            initialized = true;

        } catch (SQLException | IOException e) {
            System. err.println("❌ Error initializing database: " + e. getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Verify that all tables were created
     */
    private static void verifyTables(Connection conn) {
        String[] tables = {"users", "classroom", "classroom_rolls", "classroom_students", "routine", "exam", "ct_quiz", "lab_test", "notice"};

        System.out.println("\n📋 Verifying tables...");
        try (Statement stmt = conn.createStatement()) {
            for (String tableName : tables) {
                try (ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='" + tableName + "'")) {
                    if (rs.next()) {
                        System.out. println("   ✅ " + tableName + " table exists");
                    } else {
                        System.out.println("   ❌ " + tableName + " table NOT found!");
                    }
                }
            }
        } catch (SQLException e) {
            System. err.println("   ❌ Error verifying tables: " + e.getMessage());
        }
    }

    /**
     * Get database file path (for debugging)
     */
    public static String getDatabasePath() {
        return DB_PATH + "/classbuddy.db";
    }

    /**
     * Close statement and result set safely
     */
    public static void closeResources(Statement stmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
        } catch (SQLException e) {
            System.err.println("Error closing resources: " + e.getMessage());
        }
    }

    /**
     * Check if database is initialized
     */
    public static boolean isDatabaseReady() {
        return initialized;
    }

    /**
     * Test database connection (for debugging)
     */
    public static void testConnection() {
        try (Connection conn = getConnection()) {
            if (conn != null) {
                System.out.println("✅ Database connection test PASSED!");
                System.out.println("📍 Database location: " + getDatabasePath());
            }
        } catch (SQLException e) {
            System.err.println("❌ Database connection test FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }
}