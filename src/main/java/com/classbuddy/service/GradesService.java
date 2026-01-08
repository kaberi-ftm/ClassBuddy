package com.classbuddy.service;

import com.classbuddy.model.CTQuizResult;
import com.classbuddy.model.ExamResult;
import com.classbuddy.model.LabEvaluation;
import com.classbuddy.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GradesService {

    public static boolean addExamResult(int classroomId, int examId, String rollNumber, Double score, Double total, String grade, String remarks) {
        Integer studentId = resolveStudentId(classroomId, rollNumber);
        String sql = "INSERT OR REPLACE INTO exam_result (classroom_id, exam_id, student_id, roll_number, score, total, grade, remarks) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classroomId);
            ps.setInt(2, examId);
            if (studentId == null) ps.setNull(3, Types.INTEGER); else ps.setInt(3, studentId);
            ps.setString(4, rollNumber);
            setDouble(ps, 5, score);
            setDouble(ps, 6, total);
            ps.setString(7, grade);
            ps.setString(8, remarks);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error adding exam result: " + e.getMessage());
            return false;
        }
    }

    public static boolean addCTQuizResult(int classroomId, int ctQuizId, String rollNumber, Double score, Double total, String grade, String remarks) {
        Integer studentId = resolveStudentId(classroomId, rollNumber);
        String sql = "INSERT OR REPLACE INTO ct_quiz_result (classroom_id, ct_quiz_id, student_id, roll_number, score, total, grade, remarks) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classroomId);
            ps.setInt(2, ctQuizId);
            if (studentId == null) ps.setNull(3, Types.INTEGER); else ps.setInt(3, studentId);
            ps.setString(4, rollNumber);
            setDouble(ps, 5, score);
            setDouble(ps, 6, total);
            ps.setString(7, grade);
            ps.setString(8, remarks);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error adding CT result: " + e.getMessage());
            return false;
        }
    }

    public static boolean addLabEvaluation(int classroomId, int labTestId, String rollNumber, Double score, Double total, String grade, String remarks) {
        Integer studentId = resolveStudentId(classroomId, rollNumber);
        String sql = "INSERT OR REPLACE INTO lab_evaluation (classroom_id, lab_test_id, student_id, roll_number, score, total, grade, remarks) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classroomId);
            ps.setInt(2, labTestId);
            if (studentId == null) ps.setNull(3, Types.INTEGER); else ps.setInt(3, studentId);
            ps.setString(4, rollNumber);
            setDouble(ps, 5, score);
            setDouble(ps, 6, total);
            ps.setString(7, grade);
            ps.setString(8, remarks);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error adding lab evaluation: " + e.getMessage());
            return false;
        }
    }

    public static boolean deleteExamResult(int classroomId, int examId, String rollNumber) {
        String sql = "DELETE FROM exam_result WHERE classroom_id = ? AND exam_id = ? AND roll_number = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classroomId);
            ps.setInt(2, examId);
            ps.setString(3, rollNumber);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting exam result: " + e.getMessage());
            return false;
        }
    }

    public static boolean deleteCTQuizResult(int classroomId, int ctQuizId, String rollNumber) {
        String sql = "DELETE FROM ct_quiz_result WHERE classroom_id = ? AND ct_quiz_id = ? AND roll_number = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classroomId);
            ps.setInt(2, ctQuizId);
            ps.setString(3, rollNumber);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting CT result: " + e.getMessage());
            return false;
        }
    }

    public static boolean deleteLabEvaluation(int classroomId, int labTestId, String rollNumber) {
        String sql = "DELETE FROM lab_evaluation WHERE classroom_id = ? AND lab_test_id = ? AND roll_number = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classroomId);
            ps.setInt(2, labTestId);
            ps.setString(3, rollNumber);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting lab evaluation: " + e.getMessage());
            return false;
        }
    }

    public static List<ExamResult> getExamResults(int classroomId) {
        String sql = "SELECT * FROM exam_result WHERE classroom_id = ? ORDER BY recorded_at DESC LIMIT 50";
        List<ExamResult> list = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classroomId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new ExamResult(
                            rs.getInt("id"), rs.getInt("classroom_id"), rs.getInt("exam_id"),
                            getInteger(rs, "student_id"), rs.getString("roll_number"),
                            getDouble(rs, "score"), getDouble(rs, "total"), rs.getString("grade"),
                            rs.getString("remarks"), rs.getTimestamp("recorded_at").toLocalDateTime()
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching exam results: " + e.getMessage());
        }
        return list;
    }

    public static List<CTQuizResult> getCTResults(int classroomId) {
        String sql = "SELECT * FROM ct_quiz_result WHERE classroom_id = ? ORDER BY recorded_at DESC LIMIT 50";
        List<CTQuizResult> list = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classroomId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new CTQuizResult(
                            rs.getInt("id"), rs.getInt("classroom_id"), rs.getInt("ct_quiz_id"),
                            getInteger(rs, "student_id"), rs.getString("roll_number"),
                            getDouble(rs, "score"), getDouble(rs, "total"), rs.getString("grade"),
                            rs.getString("remarks"), rs.getTimestamp("recorded_at").toLocalDateTime()
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching CT results: " + e.getMessage());
        }
        return list;
    }

    public static List<LabEvaluation> getLabEvaluations(int classroomId) {
        String sql = "SELECT * FROM lab_evaluation WHERE classroom_id = ? ORDER BY recorded_at DESC LIMIT 50";
        List<LabEvaluation> list = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classroomId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new LabEvaluation(
                            rs.getInt("id"), rs.getInt("classroom_id"), rs.getInt("lab_test_id"),
                            getInteger(rs, "student_id"), rs.getString("roll_number"),
                            getDouble(rs, "score"), getDouble(rs, "total"), rs.getString("grade"),
                            rs.getString("remarks"), rs.getTimestamp("recorded_at").toLocalDateTime()
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching lab evaluations: " + e.getMessage());
        }
        return list;
    }

    private static void setDouble(PreparedStatement ps, int idx, Double value) throws SQLException {
        if (value == null) {
            ps.setNull(idx, Types.REAL);
        } else {
            ps.setDouble(idx, value);
        }
    }

    private static Double getDouble(ResultSet rs, String col) throws SQLException {
        double v = rs.getDouble(col);
        return rs.wasNull() ? null : v;
    }

    private static Integer getInteger(ResultSet rs, String col) throws SQLException {
        int v = rs.getInt(col);
        return rs.wasNull() ? null : v;
    }

    private static Integer resolveStudentId(int classroomId, String rollNumber) {
        if (rollNumber == null || rollNumber.trim().isEmpty()) return null;
        String sql = "SELECT student_id FROM classroom_students WHERE classroom_id = ? AND roll_number = ? LIMIT 1";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classroomId);
            ps.setString(2, rollNumber.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("student_id");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error resolving student id: " + e.getMessage());
        }
        return null;
    }
}
