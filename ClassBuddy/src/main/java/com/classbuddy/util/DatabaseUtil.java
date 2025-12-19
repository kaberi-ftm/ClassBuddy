package com.classbuddy.util;

import java.io.IOException;
import java.sql.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io. InputStream;
import java.nio.file. Paths;

public class DatabaseUtil {
    // Store DB in user home directory for easier access
    private static final String DB_PATH = Paths.get(System.getProperty("user.home"), "ClassBuddy").toString();
    private static final String DB_URL = "jdbc:sqlite:" + DB_PATH + "/classbuddy.db";
    private static boolean initialized = false;

    static {
        // Create directory if it doesn't exist
        java.nio.file.Path path = Paths.get(DB_PATH);
        try {
            if (!java.nio.file.Files.exists(path)) {
                java. nio.file.Files.createDirectories(path);
                System.out.println("Created database directory: " + DB_PATH);
            }
        } catch (Exception e) {
            System.err.println("Error creating database directory: " + e.getMessage());
        }
    }

    /**
     * Get database connection
     */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.sqlite. JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC not found:  " + e.getMessage());
        }
        return DriverManager. getConnection(DB_URL);
    }

    /**
     * Initialize database (create tables if they don't exist)
     */
    public static void initializeDatabase() {
        if (initialized) return;

        System.out.println("Initializing database at: " + DB_URL);

        try (Connection conn = getConnection()) {
            System.out.println("Database connection established!");

            // Read schema.sql from resources
            InputStream inputStream = DatabaseUtil.class.getResourceAsStream("/schema.sql");
            if (inputStream == null) {
                System.err.println("❌ schema.sql not found in resources!");
                System.err.println("Make sure schema.sql is in src/main/resources/");
                return;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sqlBuilder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                if (! line.trim().isEmpty() && !line.trim().startsWith("--")) {
                    sqlBuilder.append(line).append("\n");
                }
            }

            reader.close();

            // Split by semicolon and execute each statement
            String[] statements = sqlBuilder.toString().split(";");
            try (Statement stmt = conn.createStatement()) {
                int count = 0;
                for (String sql : statements) {
                    if (! sql.trim().isEmpty()) {
                        try {
                            stmt.execute(sql. trim());
                            count++;
                        } catch (SQLException e) {
                            // Table already exists is not an error
                            if (! e.getMessage().contains("already exists")) {
                                System. err.println("Error executing SQL: " + e.getMessage());
                            }
                        }
                    }
                }
                System.out.println("✅ Database initialized successfully!  (" + count + " statements executed)");
            }

            initialized = true;

        } catch (SQLException | IOException e) {
            System.err.println("❌ Error initializing database: " + e. getMessage());
            e.printStackTrace();
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
                System.out.println("Database location: " + getDatabasePath());
            }
        } catch (SQLException e) {
            System.err.println("❌ Database connection test FAILED: " + e.getMessage());
        }
    }
}