package com.classbuddy.service;

import com.classbuddy.util.DatabaseUtil;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

/**
 * Service to export classroom attendance to CSV.
 * Produces two sections: Summary per student and Raw entries.
 */
public class AttendanceExportService {


    public static ExportResult exportClassroomAttendanceCSV(int classroomId, Path outputPath) throws IOException {
        int totalRows = 0;
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputPath.toFile()))) {
            writer.println("# ClassBuddy Attendance Export - Format Version 1.0");
            writer.println("# Generated: " + LocalDate.now());

            // Summary section
            writer.println("SECTION,Summary");
            writer.println("Roll,Student,Present,Absent,Late,Percentage");
            totalRows += writeSummarySection(writer, classroomId);

            writer.println();

            // Raw entries section
            writer.println("SECTION,Raw");
            writer.println("Date,Roll,Student,Status,RoutineId,MarkedBy,MarkedAt");
            totalRows += writeRawSection(writer, classroomId);
        }
        return new ExportResult(true, totalRows, outputPath.toString());
    }

    private static int writeSummarySection(PrintWriter writer, int classroomId) {
        String sql = "SELECT u.username AS student_name, cs.roll_number, " +
                "SUM(CASE WHEN a.status='PRESENT' THEN 1 ELSE 0 END) AS present_count, " +
                "SUM(CASE WHEN a.status='ABSENT' THEN 1 ELSE 0 END) AS absent_count, " +
                "SUM(CASE WHEN a.status='LATE' THEN 1 ELSE 0 END) AS late_count, " +
                "COUNT(a.id) AS total_count " +
                "FROM classroom_students cs " +
                "JOIN users u ON u.id = cs.student_id " +
                "LEFT JOIN attendance a ON a.classroom_id = cs.classroom_id AND a.student_id = cs.student_id " +
                "WHERE cs.classroom_id = ? AND cs.enrollment_status = 'ACTIVE' " +
                "GROUP BY u.username, cs.roll_number " +
                "ORDER BY cs.roll_number ASC";

        int rows = 0;
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, classroomId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String roll = esc(rs.getString("roll_number"));
                    String name = esc(rs.getString("student_name"));
                    int present = rs.getInt("present_count");
                    int absent = rs.getInt("absent_count");
                    int late = rs.getInt("late_count");
                    int total = rs.getInt("total_count");
                    double pct = total > 0 ? (present * 100.0) / total : 0.0;
                    writer.printf("%s,%s,%d,%d,%d,%.2f\n", roll, name, present, absent, late, pct);
                    rows++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rows;
    }

    private static int writeRawSection(PrintWriter writer, int classroomId) {
        String sql = "SELECT a.date, cs.roll_number, u.username AS student_name, a.status, a.routine_id, a.marked_by_user_id, a.marked_at " +
                "FROM attendance a " +
                "JOIN classroom_students cs ON cs.student_id = a.student_id AND cs.classroom_id = a.classroom_id " +
                "JOIN users u ON u.id = a.student_id " +
                "WHERE a.classroom_id = ? " +
                "ORDER BY a.date ASC, cs.roll_number ASC";
        int rows = 0;
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, classroomId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String date = rs.getString("date");
                    String roll = esc(rs.getString("roll_number"));
                    String name = esc(rs.getString("student_name"));
                    String status = rs.getString("status");
                    Object routineId = rs.getObject("routine_id");
                    Object markedBy = rs.getObject("marked_by_user_id");
                    String markedAt = rs.getString("marked_at");
                    writer.printf("%s,%s,%s,%s,%s,%s,%s\n",
                            date, roll, name, status,
                            routineId == null ? "" : routineId.toString(),
                            markedBy == null ? "" : markedBy.toString(),
                            markedAt == null ? "" : markedAt
                    );
                    rows++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rows;
    }

    private static String esc(String v) {
        if (v == null) return "";
        if (v.contains(",") || v.contains("\"") || v.contains("\n")) {
            return "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return v;
    }

    public static class ExportResult {
        private final boolean success;
        private final int totalRows;
        private final String filePath;

        public ExportResult(boolean success, int totalRows, String filePath) {
            this.success = success;
            this.totalRows = totalRows;
            this.filePath = filePath;
        }

        public boolean isSuccess() { return success; }
        public int getTotalRows() { return totalRows; }
        public String getFilePath() { return filePath; }
    }
}