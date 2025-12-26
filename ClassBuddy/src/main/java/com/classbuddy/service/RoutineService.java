package com.classbuddy.service;

import com.classbuddy.model.Routine;
import com.classbuddy.util. DatabaseUtil;
import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing class routines/schedules
 */
public class RoutineService {

    /**
     * ADMIN: Add routine entry
     */
    public static boolean addRoutine(int classroomId, String day,
                                     int periodNumber, String courseName,
                                     String teacherName, String room,
                                     LocalTime timeStart, LocalTime timeEnd) {
        String sql = "INSERT INTO routine " +
                "(classroom_id, day, period_number, course_name, " +
                "teacher_name, room, time_start, time_end) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt. setInt(1, classroomId);
            pstmt. setString(2, day);
            pstmt.setInt(3, periodNumber);
            pstmt.setString(4, courseName);
            pstmt.setString(5, teacherName);
            pstmt. setString(6, room);
            pstmt.setTime(7, java.sql.Time. valueOf(timeStart));
            pstmt.setTime(8, java.sql.Time.valueOf(timeEnd));

            pstmt.executeUpdate();
            System. out.println("✅ Routine added");
            return true;

        } catch (SQLException e) {
            System.err.println("❌ Error adding routine:  " + e.getMessage());
            return false;
        }
    }

    /**
     * Get routine for a specific day
     */
    public static List<Routine> getDayRoutine(int classroomId, String day) {
        List<Routine> routines = new ArrayList<>();
        String sql = "SELECT * FROM routine WHERE classroom_id = ?  " +
                "AND day = ? ORDER BY period_number ASC";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, classroomId);
            pstmt.setString(2, day);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Routine routine = new Routine(
                            rs.getInt("id"),
                            rs.getInt("classroom_id"),
                            rs.getString("day"),
                            rs.getInt("period_number"),
                            rs.getString("course_name"),
                            rs.getString("teacher_name"),
                            rs. getString("room"),
                            rs.getTime("time_start").toLocalTime(),
                            rs.getTime("time_end").toLocalTime(),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    );
                    routines.add(routine);
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Error fetching routine");
        }

        return routines;
    }

    /**
     * Get complete weekly routine
     */
    public static List<Routine> getWeeklyRoutine(int classroomId) {
        List<Routine> routines = new ArrayList<>();
        String sql = "SELECT * FROM routine WHERE classroom_id = ? " +
                "ORDER BY " +
                "CASE day " +
                "  WHEN 'Monday' THEN 1 " +
                "  WHEN 'Tuesday' THEN 2 " +
                "  WHEN 'Wednesday' THEN 3 " +
                "  WHEN 'Thursday' THEN 4 " +
                "  WHEN 'Friday' THEN 5 " +
                "  WHEN 'Saturday' THEN 6 " +
                "  WHEN 'Sunday' THEN 7 " +
                "END, period_number ASC";

        try (Connection conn = DatabaseUtil. getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, classroomId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Routine routine = new Routine(
                            rs.getInt("id"),
                            rs. getInt("classroom_id"),
                            rs.getString("day"),
                            rs.getInt("period_number"),
                            rs.getString("course_name"),
                            rs.getString("teacher_name"),
                            rs.getString("room"),
                            rs. getTime("time_start").toLocalTime(),
                            rs. getTime("time_end").toLocalTime(),
                            rs. getTimestamp("created_at").toLocalDateTime()
                    );
                    routines.add(routine);
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Error fetching weekly routine");
        }

        return routines;
    }

    /**
     * Delete routine entry
     */
    public static boolean deleteRoutine(int routineId) {
        String sql = "DELETE FROM routine WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn. prepareStatement(sql)) {

            pstmt.setInt(1, routineId);
            pstmt.executeUpdate();
            System.out.println("✅ Routine deleted");
            return true;

        } catch (SQLException e) {
            System. err.println("❌ Error deleting routine");
            return false;
        }
    }

    /**
     * Update routine entry
     */
    public static boolean updateRoutine(int routineId, String day,
                                        int periodNumber, String courseName,
                                        String teacherName, String room,
                                        LocalTime timeStart, LocalTime timeEnd) {
        String sql = "UPDATE routine SET day=?, period_number=?, " +
                "course_name=?, teacher_name=?, room=?, " +
                "time_start=?, time_end=? WHERE id=?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, day);
            pstmt.setInt(2, periodNumber);
            pstmt.setString(3, courseName);
            pstmt.setString(4, teacherName);
            pstmt.setString(5, room);
            pstmt.setTime(6, java.sql.Time.valueOf(timeStart));
            pstmt.setTime(7, java.sql.Time.valueOf(timeEnd));
            pstmt.setInt(8, routineId);

            pstmt.executeUpdate();
            System. out.println("✅ Routine updated");
            return true;

        } catch (SQLException e) {
            System.err.println("❌ Error updating routine");
            return false;
        }
    }
}