package com.classbuddy.service;

import com.classbuddy.model.UserProfile;
import com.classbuddy.util.DatabaseUtil;

import java.sql.*;
import java.time.LocalDateTime;

/**
 * ProfileService - Handles user profile operations
 */
public class ProfileService {

    /**
     * Get user profile by user ID
     */
    public static UserProfile getProfile(int userId) {
        String sql = "SELECT * FROM user_profiles WHERE user_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new UserProfile(
                        rs.getInt("user_id"),
                        rs.getString("full_name"),
                        rs.getString("phone_number"),
                        rs.getString("address"),
                        rs.getString("bio"),
                        rs.getString("avatar_url"),
                        rs.getString("department"),
                        rs.getString("student_id"),
                        rs.getString("designation"),
                        rs.getTimestamp("date_of_birth") != null ? 
                            rs.getTimestamp("date_of_birth").toLocalDateTime() : null,
                        rs.getTimestamp("updated_at").toLocalDateTime()
                );
            }

        } catch (SQLException e) {
            System.err.println("Error fetching profile: " + e.getMessage());
        }

        return null;
    }

    /**
     * Create or update user profile
     */
    public static boolean saveProfile(UserProfile profile) {
        String sql = "INSERT INTO user_profiles (user_id, full_name, phone_number, address, bio, " +
                    "avatar_url, department, student_id, designation, date_of_birth, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                    "ON CONFLICT(user_id) DO UPDATE SET " +
                    "full_name = excluded.full_name, " +
                    "phone_number = excluded.phone_number, " +
                    "address = excluded.address, " +
                    "bio = excluded.bio, " +
                    "avatar_url = excluded.avatar_url, " +
                    "department = excluded.department, " +
                    "student_id = excluded.student_id, " +
                    "designation = excluded.designation, " +
                    "date_of_birth = excluded.date_of_birth, " +
                    "updated_at = excluded.updated_at";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, profile.getUserId());
            pstmt.setString(2, profile.getFullName());
            pstmt.setString(3, profile.getPhoneNumber());
            pstmt.setString(4, profile.getAddress());
            pstmt.setString(5, profile.getBio());
            pstmt.setString(6, profile.getAvatarUrl());
            pstmt.setString(7, profile.getDepartment());
            pstmt.setString(8, profile.getStudentId());
            pstmt.setString(9, profile.getDesignation());
            pstmt.setTimestamp(10, profile.getDateOfBirth() != null ? 
                Timestamp.valueOf(profile.getDateOfBirth()) : null);
            pstmt.setTimestamp(11, Timestamp.valueOf(LocalDateTime.now()));

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error saving profile: " + e.getMessage());
            return false;
        }
    }

    /**
     * Delete user profile
     */
    public static boolean deleteProfile(int userId) {
        String sql = "DELETE FROM user_profiles WHERE user_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting profile: " + e.getMessage());
            return false;
        }
    }
}
