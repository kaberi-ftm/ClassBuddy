package com.classbuddy.service;

import com. classbuddy.model.User;
import com.classbuddy. model.Role;
import com.classbuddy.util.DatabaseUtil;
import com.classbuddy.util. PasswordHasher;
import java.sql.*;

public class AuthService {


     /**
     * Register a new user
     * @return true if registration successful, false if user already exists
     */
    public static boolean registerUser(String username, String email, String password, Role role) {
        System.out.println("\n🔍 DEBUG: Starting registration for '" + username + "'");

        // Check if user already exists
        if (userExists(username, email)) {
            System.out.println("❌ User already exists: " + username);
            return false;
        }

        String hashedPassword = PasswordHasher.hashPassword(password);
        String sql = "INSERT INTO users (username, email, password_hash, role, created_at, updated_at) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

        try (Connection conn = DatabaseUtil.getConnection()) {
            System.out.println("✅ Database connection established");

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                pstmt.setString(2, email);
                pstmt.setString(3, hashedPassword);
                pstmt. setString(4, role.name());

                System.out.println("📝 Executing INSERT query...");
                int rowsAffected = pstmt. executeUpdate();
                System.out.println("✅ Rows affected: " + rowsAffected);

                if (rowsAffected > 0) {
                    System.out.println("✅ User registered successfully:  " + username);
                    System.out.println("   Email: " + email);
                    System.out.println("   Role: " + role.getDisplayName());
                    return true;
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Error registering user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }

        System.out.println("❌ Registration failed - unknown reason");
        return false;
    }
    /**
     * Login user - verify username and password
     * @return User object if login successful, null otherwise
     */
    public static User loginUser(String usernameOrEmail, String password) {
        String sql = "SELECT * FROM users WHERE username = ? OR email = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, usernameOrEmail);
            pstmt.setString(2, usernameOrEmail);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String hashedPassword = rs.getString("password_hash");

                    // Verify password
                    if (PasswordHasher.verifyPassword(password, hashedPassword)) {
                        return new User(
                                rs.getInt("id"),
                                rs.getString("username"),
                                rs.getString("email"),
                                hashedPassword,
                                Role.fromString(rs.getString("role")),
                                rs.getTimestamp("created_at").toLocalDateTime(),
                                rs.getTimestamp("updated_at").toLocalDateTime()
                        );
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Error during login: " + e.getMessage());
        }

        return null; // Login failed
    }

    /**
     * Check if username or email already exists
     */
    public static boolean userExists(String username, String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ? OR email = ? ";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, email);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs. next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            System.err.println("Error checking user existence: " + e.getMessage());
        }

        return false;
    }

    /**
     * Get user by ID
     */
    public static User getUserById(int userId) {
        String sql = "SELECT * FROM users WHERE id = ? ";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("email"),
                            rs.getString("password_hash"),
                            Role.fromString(rs.getString("role")),
                            rs.getTimestamp("created_at").toLocalDateTime(),
                            rs.getTimestamp("updated_at").toLocalDateTime()
                    );
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching user: " + e.getMessage());
        }

        return null;
    }

    /**
     * Check if username is unique
     */
    public static boolean isUsernameUnique(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }

        } catch (SQLException e) {
            System.err.println("Error checking username:  " + e.getMessage());
        }

        return false;
    }

    /**
     * Check if email is unique
     */
    public static boolean isEmailUnique(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ? ";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }

        } catch (SQLException e) {
            System. err.println("Error checking email: " + e.getMessage());
        }

        return false;
    }
    /**
     * Update user's last login time
     */
    public static void updateLastLoginTime(int userId) {
        String sql = "UPDATE users SET updated_at = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ User login time updated: ID " + userId);
            }

        } catch (SQLException e) {
            System.err.println("⚠️ Error updating login time: " + e.getMessage());
        }
    }
}