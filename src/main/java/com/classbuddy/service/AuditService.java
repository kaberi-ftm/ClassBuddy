package com.classbuddy.service;

import com.classbuddy.model.AuditLog;
import com.classbuddy.util.DatabaseUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * AuditService - Central logging service for all user actions
 * Provides audit trail capabilities for compliance and debugging
 */
public class AuditService {

    /**
     * Log a user action in the audit trail
     * This method is thread-safe and can be called from any service
     *
     * @param userId ID of the user performing the action
     * @param action Type of action (CREATE, UPDATE, DELETE, etc.)
     * @param entityType Type of entity being modified (Routine, Exam, etc.)
     * @param entityId ID of the entity
     * @param oldValue Previous state (can be null for CREATE actions)
     * @param newValue Current state (can be null for DELETE actions)
     */
    public static void log(int userId, AuditLog.Action action, String entityType, int entityId, 
                           String oldValue, String newValue) {
        if (userId <= 0) {
            System.err.println("Invalid userId for audit log: " + userId);
            return;
        }

        String sql = "INSERT INTO audit_log (user_id, action, entity_type, entity_id, old_value, new_value, timestamp) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, action.toString());
            pstmt.setString(3, entityType);
            pstmt.setInt(4, entityId);
            pstmt.setString(5, oldValue);
            pstmt.setString(6, newValue);
            pstmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error logging audit trail: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get all audit logs for a specific user
     * @param userId User ID to filter by
     * @return List of audit logs for the user
     */
    public static List<AuditLog> getLogsForUser(int userId) {
        String sql = "SELECT * FROM audit_log WHERE user_id = ? ORDER BY timestamp DESC LIMIT 100";
        List<AuditLog> logs = new ArrayList<>();

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                logs.add(parseAuditLog(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching audit logs for user: " + e.getMessage());
        }

        return logs;
    }

    /**
     * Get all audit logs for a specific entity
     * @param entityType Type of entity (Routine, Exam, etc.)
     * @param entityId ID of the entity
     * @return List of audit logs for the entity
     */
    public static List<AuditLog> getLogsForEntity(String entityType, int entityId) {
        String sql = "SELECT * FROM audit_log WHERE entity_type = ? AND entity_id = ? ORDER BY timestamp DESC LIMIT 50";
        List<AuditLog> logs = new ArrayList<>();

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, entityType);
            pstmt.setInt(2, entityId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                logs.add(parseAuditLog(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching audit logs for entity: " + e.getMessage());
        }

        return logs;
    }

    /**
     * Get recent audit logs across all users
     * @param limit Number of recent logs to return
     * @return List of recent audit logs
     */
    public static List<AuditLog> getRecentLogs(int limit) {
        String sql = "SELECT * FROM audit_log ORDER BY timestamp DESC LIMIT ?";
        List<AuditLog> logs = new ArrayList<>();

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                logs.add(parseAuditLog(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching recent audit logs: " + e.getMessage());
        }

        return logs;
    }

    /**
     * Get audit logs for a specific action type
     * @param action Type of action to filter by
     * @param limit Maximum number of logs to return
     * @return List of audit logs for the action
     */
    public static List<AuditLog> getLogsByAction(AuditLog.Action action, int limit) {
        String sql = "SELECT * FROM audit_log WHERE action = ? ORDER BY timestamp DESC LIMIT ?";
        List<AuditLog> logs = new ArrayList<>();

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, action.toString());
            pstmt.setInt(2, limit);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                logs.add(parseAuditLog(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching audit logs by action: " + e.getMessage());
        }

        return logs;
    }

    /**
     * Get count of audit logs
     * @return Total number of audit logs in database
     */
    public static int getAuditLogCount() {
        String sql = "SELECT COUNT(*) as count FROM audit_log";

        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("count");
            }

        } catch (SQLException e) {
            System.err.println("Error getting audit log count: " + e.getMessage());
        }

        return 0;
    }

    /**
     * Clear old audit logs (older than specified days)
     * Should be called periodically for database maintenance
     * @param olderThanDays Number of days to keep
     * @return Number of logs deleted
     */
    public static int clearOldLogs(int olderThanDays) {
        String sql = "DELETE FROM audit_log WHERE timestamp < datetime('now', '-' || ? || ' days')";
        int deleted = 0;

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, olderThanDays);
            deleted = pstmt.executeUpdate();
            System.out.println("Cleared " + deleted + " audit logs older than " + olderThanDays + " days");

        } catch (SQLException e) {
            System.err.println("Error clearing old audit logs: " + e.getMessage());
        }

        return deleted;
    }

    /**
     * Parse an AuditLog from ResultSet
     */
    private static AuditLog parseAuditLog(ResultSet rs) throws SQLException {
        return new AuditLog(
                rs.getInt("id"),
                rs.getInt("user_id"),
                AuditLog.Action.valueOf(rs.getString("action")),
                rs.getString("entity_type"),
                rs.getInt("entity_id"),
                rs.getString("old_value"),
                rs.getString("new_value"),
                rs.getTimestamp("timestamp").toLocalDateTime()
        );
    }
}
