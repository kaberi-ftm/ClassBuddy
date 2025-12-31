package com.classbuddy.service;

import com.classbuddy.model.LabTest;
import com.classbuddy.util.DatabaseUtil;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing lab tests
 */
public class LabTestService {

    /**
     * Add a new lab test
     */
    public static boolean addLabTest(int classroomId, LocalDate testDate, String experimentNumber,
                                     String teacherName, String evaluationCriteria) {
        String sql = "INSERT INTO lab_test (classroom_id, test_date, experiment_number, " +
                     "teacher_name, evaluation_criteria) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, classroomId);
            pstmt.setDate(2, Date.valueOf(testDate));
            pstmt.setString(3, experimentNumber);
            pstmt.setString(4, teacherName);
            pstmt.setString(5, evaluationCriteria);

            pstmt.executeUpdate();
            System.out.println("✅ Lab test added successfully");
            return true;

        } catch (SQLException e) {
            System.err.println("❌ Error adding lab test: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get all lab tests for a classroom
     */
    public static List<LabTest> getClassroomLabTests(int classroomId) {
        List<LabTest> labTests = new ArrayList<>();
        String sql = "SELECT * FROM lab_test WHERE classroom_id = ? ORDER BY test_date ASC";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, classroomId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    LabTest labTest = new LabTest(
                            rs.getInt("id"),
                            rs.getInt("classroom_id"),
                            rs.getDate("test_date").toLocalDate(),
                            rs.getString("experiment_number"),
                            rs.getString("teacher_name"),
                            rs.getString("evaluation_criteria"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    );
                    labTests.add(labTest);
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Error fetching lab tests: " + e.getMessage());
        }

        return labTests;
    }

    /**
     * Update a lab test
     */
    public static boolean updateLabTest(int id, LocalDate testDate, String experimentNumber,
                                        String teacherName, String evaluationCriteria) {
        String sql = "UPDATE lab_test SET test_date = ?, experiment_number = ?, " +
                     "teacher_name = ?, evaluation_criteria = ? WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(testDate));
            pstmt.setString(2, experimentNumber);
            pstmt.setString(3, teacherName);
            pstmt.setString(4, evaluationCriteria);
            pstmt.setInt(5, id);

            pstmt.executeUpdate();
            System.out.println("✅ Lab test updated successfully");
            return true;

        } catch (SQLException e) {
            System.err.println("❌ Error updating lab test: " + e.getMessage());
            return false;
        }
    }

    /**
     * Delete a lab test
     */
    public static boolean deleteLabTest(int id) {
        String sql = "DELETE FROM lab_test WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            System.out.println("✅ Lab test deleted successfully");
            return true;

        } catch (SQLException e) {
            System.err.println("❌ Error deleting lab test: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get upcoming lab tests (test date in future)
     */
    public static List<LabTest> getUpcomingLabTests(int classroomId) {
        List<LabTest> labTests = new ArrayList<>();
        String sql = "SELECT * FROM lab_test WHERE classroom_id = ? AND test_date >= date('now') " +
                     "ORDER BY test_date ASC";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, classroomId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    LabTest labTest = new LabTest(
                            rs.getInt("id"),
                            rs.getInt("classroom_id"),
                            rs.getDate("test_date").toLocalDate(),
                            rs.getString("experiment_number"),
                            rs.getString("teacher_name"),
                            rs.getString("evaluation_criteria"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    );
                    labTests.add(labTest);
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Error fetching upcoming lab tests: " + e.getMessage());
        }

        return labTests;
    }
}
