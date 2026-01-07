package com.classbuddy.service;

import com.classbuddy.model.Attendance;
import com.classbuddy.util.DatabaseUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for managing student attendance records
 */
public class AttendanceService {

    /**
     * Mark attendance for a single student
     */
    public static boolean markAttendance(int classroomId, int studentId, String rollNumber,
                                        LocalDate date, Attendance.Status status, int markedByUserId) {
        String sql = "INSERT OR REPLACE INTO attendance " +
                    "(classroom_id, student_id, roll_number, date, status, marked_by_user_id, marked_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, classroomId);
            pstmt.setInt(2, studentId);
            pstmt.setString(3, rollNumber);
            pstmt.setDate(4, Date.valueOf(date));
            pstmt.setString(5, status.name());
            pstmt.setInt(6, markedByUserId);
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error marking attendance: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Mark attendance for a single student with routine context
     */
    public static boolean markAttendanceWithRoutine(int classroomId, int studentId, String rollNumber,
                                                    Integer routineId, LocalDate date, 
                                                    Attendance.Status status, int markedByUserId) {
        String sql = "INSERT OR REPLACE INTO attendance " +
                    "(classroom_id, student_id, roll_number, routine_id, date, status, marked_by_user_id, marked_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, classroomId);
            pstmt.setInt(2, studentId);
            pstmt.setString(3, rollNumber);
            if (routineId != null) {
                pstmt.setInt(4, routineId);
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }
            pstmt.setDate(5, Date.valueOf(date));
            pstmt.setString(6, status.name());
            pstmt.setInt(7, markedByUserId);
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error marking attendance with routine: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Mark bulk attendance for multiple students on the same date
     */
    public static int markBulkAttendance(int classroomId, LocalDate date, 
                                        List<Attendance> attendanceList, int markedByUserId) {
        String sql = "INSERT OR REPLACE INTO attendance " +
                    "(classroom_id, student_id, roll_number, routine_id, date, status, marked_by_user_id, marked_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        
        int successCount = 0;
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            conn.setAutoCommit(false);
            
            for (Attendance att : attendanceList) {
                pstmt.setInt(1, classroomId);
                pstmt.setInt(2, att.getStudentId());
                pstmt.setString(3, att.getRollNumber());
                
                if (att.getRoutineId() != null) {
                    pstmt.setInt(4, att.getRoutineId());
                } else {
                    pstmt.setNull(4, Types.INTEGER);
                }
                
                pstmt.setDate(5, Date.valueOf(date));
                pstmt.setString(6, att.getStatus().name());
                pstmt.setInt(7, markedByUserId);
                
                pstmt.addBatch();
            }
            
            int[] results = pstmt.executeBatch();
            conn.commit();
            
            for (int result : results) {
                if (result > 0) successCount++;
            }
            
        } catch (SQLException e) {
            System.err.println("Error marking bulk attendance: " + e.getMessage());
            e.printStackTrace();
        }
        
        return successCount;
    }

    /**
     * Get all attendance records for a student in a classroom
     */
    public static List<Attendance> getAttendanceForStudent(int studentId, int classroomId) {
        String sql = "SELECT * FROM attendance WHERE student_id = ? AND classroom_id = ? ORDER BY date DESC";
        List<Attendance> attendanceList = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, classroomId);
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                attendanceList.add(mapResultSetToAttendance(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error fetching student attendance: " + e.getMessage());
            e.printStackTrace();
        }
        
        return attendanceList;
    }

    /**
     * Get all attendance records for a specific date in a classroom
     */
    public static List<Attendance> getAttendanceForDate(int classroomId, LocalDate date) {
        String sql = "SELECT * FROM attendance WHERE classroom_id = ? AND date = ? ORDER BY roll_number";
        List<Attendance> attendanceList = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, classroomId);
            pstmt.setDate(2, Date.valueOf(date));
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                attendanceList.add(mapResultSetToAttendance(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error fetching date attendance: " + e.getMessage());
            e.printStackTrace();
        }
        
        return attendanceList;
    }

    /**
     * Get attendance statistics for a student
     * Returns: {total_classes, present, absent, late, percentage}
     */
    public static Map<String, Object> getAttendanceStats(int studentId, int classroomId) {
        String sql = "SELECT " +
                    "COUNT(*) as total, " +
                    "SUM(CASE WHEN status = 'PRESENT' THEN 1 ELSE 0 END) as present, " +
                    "SUM(CASE WHEN status = 'ABSENT' THEN 1 ELSE 0 END) as absent, " +
                    "SUM(CASE WHEN status = 'LATE' THEN 1 ELSE 0 END) as late " +
                    "FROM attendance WHERE student_id = ? AND classroom_id = ?";
        
        Map<String, Object> stats = new HashMap<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, classroomId);
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int total = rs.getInt("total");
                int present = rs.getInt("present");
                int absent = rs.getInt("absent");
                int late = rs.getInt("late");
                
                double percentage = total > 0 ? (present * 100.0 / total) : 0.0;
                
                stats.put("total_classes", total);
                stats.put("present", present);
                stats.put("absent", absent);
                stats.put("late", late);
                stats.put("percentage", Math.round(percentage * 100.0) / 100.0);
            }
            
        } catch (SQLException e) {
            System.err.println("Error calculating attendance stats: " + e.getMessage());
            e.printStackTrace();
        }
        
        return stats;
    }

    /**
     * Get attendance records for a specific routine
     */
    public static List<Attendance> getAttendanceByRoutine(int routineId) {
        String sql = "SELECT * FROM attendance WHERE routine_id = ? ORDER BY date DESC, roll_number";
        List<Attendance> attendanceList = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, routineId);
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                attendanceList.add(mapResultSetToAttendance(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error fetching routine attendance: " + e.getMessage());
            e.printStackTrace();
        }
        
        return attendanceList;
    }

    /**
     * Update attendance status
     */
    public static boolean updateAttendance(int attendanceId, Attendance.Status newStatus) {
        String sql = "UPDATE attendance SET status = ?, marked_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, newStatus.name());
            pstmt.setInt(2, attendanceId);
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating attendance: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete attendance record
     */
    public static boolean deleteAttendance(int attendanceId) {
        String sql = "DELETE FROM attendance WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, attendanceId);
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting attendance: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Check if attendance is already marked for a date
     */
    public static boolean isAttendanceMarked(int classroomId, LocalDate date) {
        String sql = "SELECT COUNT(*) FROM attendance WHERE classroom_id = ? AND date = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, classroomId);
            pstmt.setDate(2, Date.valueOf(date));
            
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
            
        } catch (SQLException e) {
            System.err.println("Error checking attendance: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Helper method to map ResultSet to Attendance object
     */
    private static Attendance mapResultSetToAttendance(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int classroomId = rs.getInt("classroom_id");
        int studentId = rs.getInt("student_id");
        String rollNumber = rs.getString("roll_number");
        Integer routineId = rs.getObject("routine_id", Integer.class);
        LocalDate date = rs.getDate("date").toLocalDate();
        Attendance.Status status = Attendance.Status.valueOf(rs.getString("status"));
        int markedByUserId = rs.getInt("marked_by_user_id");
        Timestamp markedAtTs = rs.getTimestamp("marked_at");
        
        return new Attendance(id, classroomId, studentId, rollNumber, routineId, date, 
                            status, markedByUserId, markedAtTs.toLocalDateTime());
    }
}
