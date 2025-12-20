package com.classbuddy;

import com.classbuddy.util.DatabaseUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx. scene.Scene;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql. Statement;

public class App extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        System.out.println("\n");
        System.out.println("╔════════════════════════════════════════════╗");
        System.out. println("║     ClassBuddy Application Starting        ║");
        System.out.println("╚════════════════════════════════════════════╝");

        // Initialize database on app startup
        System.out.println("\n🔧 Step 1: Initializing database...");
        DatabaseUtil.initializeDatabase();

        // Test connection
        System.out.println("\n🧪 Step 2: Testing database connection...");
        DatabaseUtil.testConnection();

        // Verify data in database
        System.out.println("\n📋 Step 3: Checking existing data.. .");
        verifyDatabase();

        System.out.println("\n✅ Database ready!");
        System.out.println("════════════════════════════════════════════\n");

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 900, 600);

        stage.setTitle("ClassBuddy");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    /**
     * Verify database has tables and data
     */
    private static void verifyDatabase() {
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement()) {

            // Check users table
            System.out.println("   📊 Checking USERS table:");
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM users")) {
                if (rs.next()) {
                    int count = rs.getInt("count");
                    System.out. println("      Total users: " + count);

                    if (count > 0) {
                        System.out.println("      Users in database:");
                        try (ResultSet userRs = stmt.executeQuery("SELECT id, username, email, role FROM users")) {
                            while (userRs.next()) {
                                System.out.println("         - ID: " + userRs.getInt("id") +
                                        ", Username: " + userRs.getString("username") +
                                        ", Email: " + userRs.getString("email") +
                                        ", Role: " + userRs.getString("role"));
                            }
                        }
                    }
                }
            }

            // Check classrooms table
            System.out. println("   📊 Checking CLASSROOM table:");
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM classroom")) {
                if (rs. next()) {
                    int count = rs.getInt("count");
                    System.out.println("      Total classrooms: " + count);
                }
            }

        } catch (Exception e) {
            System.err.println("   ❌ Error verifying database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public static void main(String[] args) {
        launch();
    }
}