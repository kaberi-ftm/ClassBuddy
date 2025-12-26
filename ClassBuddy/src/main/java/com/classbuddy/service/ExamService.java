package com.classbuddy.service;

import com.classbuddy.model. Exam;
import com.classbuddy.util.DatabaseUtil;
import java.sql.*;
import java.time.LocalDate;
import java. time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing exams
 */
public class ExamService {

    /**
     * ADMIN:  Add exam to classroom
     */
    public static boolean addExam(int classroomId, String courseName,
                                  String examType, LocalDate examDate,
                                  LocalTime examTime, String room) {
        String sql = "INSERT INTO exam " +
                "(classroom_id, course_name, exam_type, exam_date, exam_time, room) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, classroomId);
            pstmt.setString(2, courseName);
            pstmt.setString(3, examType);
            pstmt. setDate(4, java.sql.Date.valueOf(examDate));
            pstmt. setTime(5, java.sql.Time.valueOf(examTime));
            pstmt.setString(6, room);

            pstmt.executeUpdate();
            System.out.println("✅ Exam added:  " + courseName);
            return true;

        } catch (SQLException e) {
            System.err.println("❌ Error adding exam: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get all exams for a classroom
     */
    public static List<Exam> getClassroomExams(int classroomId) {
        List<Exam> exams = new ArrayList<>();
        String sql = "SELECT * FROM exam WHERE classroom_id = ?  " +
                "ORDER BY exam_date ASC";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, classroomId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Exam exam = new Exam(
                            rs.getInt("id"),
                            rs.getInt("classroom_id"),
                            rs.getString("course_name"),
                            rs.getString("exam_type"),
                            rs.getDate("exam_date").toLocalDate(),
                            rs.getTime("exam_time").toLocalTime(),
                            rs.getString("room"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    );
                    exams.add(exam);
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Error fetching exams: " + e. getMessage());
        }

        return exams;
    }

    /**
     * Get upcoming exams (next 7 days)
     */
    public static List<Exam> getUpcomingExams(int classroomId) {
        List<Exam> exams = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate nextWeek = today.plusDays(7);

        String sql = "SELECT * FROM exam WHERE classroom_id = ? " +
                "AND exam_date >= ? AND exam_date <= ?  " +
                "ORDER BY exam_date ASC";

        try (Connection conn = DatabaseUtil. getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, classroomId);
            pstmt.setDate(2, java.sql.Date.valueOf(today));
            pstmt.setDate(3, java.sql. Date.valueOf(nextWeek));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Exam exam = new Exam(
                            rs.getInt("id"),
                            rs.getInt("classroom_id"),
                            rs.getString("course_name"),
                            rs.getString("exam_type"),
                            rs.getDate("exam_date").toLocalDate(),
                            rs.getTime("exam_time").toLocalTime(),
                            rs.getString("room"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    );
                    exams.add(exam);
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Error fetching upcoming exams");
        }

        return exams;
    }

    /**
     * Delete exam
     */
    public static boolean deleteExam(int examId) {
        String sql = "DELETE FROM exam WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, examId);
            pstmt.executeUpdate();
            System.out.println("✅ Exam deleted");
            return true;

        } catch (SQLException e) {
            System.err.println("❌ Error deleting exam");
            return false;
        }
    }

    /**
     * Update exam
     */
    public static boolean updateExam(int examId, String courseName,
                                     String examType, LocalDate examDate,
                                     LocalTime examTime, String room) {
        String sql = "UPDATE exam SET course_name=?, exam_type=?, " +
                "exam_date=?, exam_time=?, room=? WHERE id=?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, courseName);
            pstmt.setString(2, examType);
            pstmt. setDate(3, java.sql.Date.valueOf(examDate));
            pstmt.setTime(4, java.sql.Time.valueOf(examTime));
            pstmt.setString(5, room);
            pstmt.setInt(6, examId);

            pstmt.executeUpdate();
            System.out. println("✅ Exam updated");
            return true;

        } catch (SQLException e) {
            System.err.println("❌ Error updating exam");
            return false;
        }
    }
}