package com.classbuddy.service;

import com.classbuddy.util.DatabaseUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public class AnalyticsService {

    public static int countUpcomingExams(int classroomId, int days) {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(days);
        String sql = "SELECT COUNT(*) FROM exam WHERE classroom_id = ? AND exam_date >= ? AND exam_date <= ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classroomId);
            ps.setDate(2, java.sql.Date.valueOf(start));
            ps.setDate(3, java.sql.Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            System.err.println("Error counting upcoming exams: " + e.getMessage());
            return 0;
        }
    }

    public static int countUpcomingCTs(int classroomId, int days) {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(days);
        String sql = "SELECT COUNT(*) FROM ct_quiz WHERE classroom_id = ? AND deadline >= ? AND deadline <= ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classroomId);
            ps.setDate(2, java.sql.Date.valueOf(start));
            ps.setDate(3, java.sql.Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            System.err.println("Error counting upcoming CTs: " + e.getMessage());
            return 0;
        }
    }

    public static int countUpcomingLabs(int classroomId, int days) {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(days);
        String sql = "SELECT COUNT(*) FROM lab_test WHERE classroom_id = ? AND test_date >= ? AND test_date <= ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classroomId);
            ps.setDate(2, java.sql.Date.valueOf(start));
            ps.setDate(3, java.sql.Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            System.err.println("Error counting upcoming labs: " + e.getMessage());
            return 0;
        }
    }

    public static Double averageExamPercent(int classroomId) {
        String sql = "SELECT AVG((score * 100.0) / total) FROM exam_result WHERE classroom_id = ? AND score IS NOT NULL AND total IS NOT NULL AND total > 0";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classroomId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double v = rs.getDouble(1);
                    return rs.wasNull() ? null : v;
                }
                return null;
            }
        } catch (SQLException e) {
            System.err.println("Error averaging exam percent: " + e.getMessage());
            return null;
        }
    }

    public static Double averageCTPercent(int classroomId) {
        String sql = "SELECT AVG((score * 100.0) / total) FROM ct_quiz_result WHERE classroom_id = ? AND score IS NOT NULL AND total IS NOT NULL AND total > 0";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classroomId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double v = rs.getDouble(1);
                    return rs.wasNull() ? null : v;
                }
                return null;
            }
        } catch (SQLException e) {
            System.err.println("Error averaging ct percent: " + e.getMessage());
            return null;
        }
    }

    public static Double averageLabPercent(int classroomId) {
        String sql = "SELECT AVG((score * 100.0) / total) FROM lab_evaluation WHERE classroom_id = ? AND score IS NOT NULL AND total IS NOT NULL AND total > 0";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classroomId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double v = rs.getDouble(1);
                    return rs.wasNull() ? null : v;
                }
                return null;
            }
        } catch (SQLException e) {
            System.err.println("Error averaging lab percent: " + e.getMessage());
            return null;
        }
    }

    public static Map<String, Integer> examGradeDistribution(int classroomId) {
        String sql = "SELECT grade, COUNT(*) FROM exam_result WHERE classroom_id = ? AND grade IS NOT NULL GROUP BY grade ORDER BY grade";
        Map<String, Integer> dist = new LinkedHashMap<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classroomId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String g = rs.getString(1);
                    int c = rs.getInt(2);
                    dist.put(g, c);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching grade distribution: " + e.getMessage());
        }
        return dist;
    }

    public static Double overallAttendancePercent(int classroomId) {
        String sql = "SELECT SUM(CASE WHEN status = 'PRESENT' THEN 1 ELSE 0 END) AS present, COUNT(*) AS total FROM attendance WHERE classroom_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classroomId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt("total");
                    int present = rs.getInt("present");
                    if (total == 0) return null;
                    return present * 100.0 / total;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error computing attendance percent: " + e.getMessage());
        }
        return null;
    }

    public static Map<String, Double> courseExamAverages(int classroomId) {
        String sql = "SELECT e.course_name, AVG((er.score * 100.0)/er.total) AS avg_pct " +
                "FROM exam_result er JOIN exam e ON er.exam_id = e.id " +
                "WHERE er.classroom_id = ? AND er.score IS NOT NULL AND er.total IS NOT NULL AND er.total > 0 " +
                "GROUP BY e.course_name ORDER BY e.course_name";
        Map<String, Double> result = new LinkedHashMap<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classroomId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String course = rs.getString(1);
                    double pct = rs.getDouble(2);
                    if (!rs.wasNull()) {
                        result.put(course, pct);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error computing course exam averages: " + e.getMessage());
        }
        return result;
    }
}
