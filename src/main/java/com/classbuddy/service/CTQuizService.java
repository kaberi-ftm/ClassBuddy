package com.classbuddy.service;

import com.classbuddy.model.CTQuiz;
import com.classbuddy.util.DatabaseUtil;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing CT/Quiz entries
 */
public class CTQuizService {

    /**
     * Add a new CT/Quiz
     */
    public static boolean addCTQuiz(int classroomId, String name, String syllabus, LocalDate deadline) {
        String sql = "INSERT INTO ct_quiz (classroom_id, name, syllabus, deadline) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, classroomId);
            pstmt.setString(2, name);
            pstmt.setString(3, syllabus);
            pstmt.setDate(4, Date.valueOf(deadline));

            pstmt.executeUpdate();
            System.out.println("CT/Quiz added successfully");
            return true;

        } catch (SQLException e) {
            System.err.println("Error adding CT/Quiz: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get all CT/Quiz entries for a classroom
     */
    public static List<CTQuiz> getClassroomCTQuizzes(int classroomId) {
        List<CTQuiz> ctQuizzes = new ArrayList<>();
        String sql = "SELECT * FROM ct_quiz WHERE classroom_id = ? ORDER BY deadline ASC";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, classroomId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    CTQuiz ctQuiz = new CTQuiz(
                            rs.getInt("id"),
                            rs.getInt("classroom_id"),
                            rs.getString("name"),
                            rs.getString("syllabus"),
                            rs.getDate("deadline").toLocalDate(),
                            rs.getBoolean("is_completed"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    );
                    ctQuizzes.add(ctQuiz);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching CT/Quiz entries: " + e.getMessage());
        }

        return ctQuizzes;
    }

    /**
     * Update a CT/Quiz
     */
    public static boolean updateCTQuiz(int id, String name, String syllabus, LocalDate deadline) {
        String sql = "UPDATE ct_quiz SET name = ?, syllabus = ?, deadline = ? WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, syllabus);
            pstmt.setDate(3, Date.valueOf(deadline));
            pstmt.setInt(4, id);

            pstmt.executeUpdate();
            System.out.println("CT/Quiz updated successfully");
            return true;

        } catch (SQLException e) {
            System.err.println("Error updating CT/Quiz: " + e.getMessage());
            return false;
        }
    }

    /**
     * Mark CT/Quiz as completed
     */
    public static boolean markAsCompleted(int id, boolean completed) {
        String sql = "UPDATE ct_quiz SET is_completed = ? WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, completed);
            pstmt.setInt(2, id);

            pstmt.executeUpdate();
            System.out.println("CT/Quiz completion status updated");
            return true;

        } catch (SQLException e) {
            System.err.println("Error updating CT/Quiz status: " + e.getMessage());
            return false;
        }
    }

    /**
     * Delete a CT/Quiz
     */
    public static boolean deleteCTQuiz(int id) {
        String sql = "DELETE FROM ct_quiz WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            System.out.println("CT/Quiz deleted successfully");
            return true;

        } catch (SQLException e) {
            System.err.println("Error deleting CT/Quiz: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get upcoming CT/Quiz entries (not completed and deadline in future)
     */
    public static List<CTQuiz> getUpcomingCTQuizzes(int classroomId) {
        List<CTQuiz> ctQuizzes = new ArrayList<>();
        String sql = "SELECT * FROM ct_quiz WHERE classroom_id = ? AND is_completed = 0 " +
                     "AND deadline >= date('now') ORDER BY deadline ASC";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, classroomId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    CTQuiz ctQuiz = new CTQuiz(
                            rs.getInt("id"),
                            rs.getInt("classroom_id"),
                            rs.getString("name"),
                            rs.getString("syllabus"),
                            rs.getDate("deadline").toLocalDate(),
                            rs.getBoolean("is_completed"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    );
                    ctQuizzes.add(ctQuiz);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching upcoming CT/Quiz entries: " + e.getMessage());
        }

        return ctQuizzes;
    }
}
