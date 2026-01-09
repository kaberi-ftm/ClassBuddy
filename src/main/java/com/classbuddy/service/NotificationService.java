package com.classbuddy.service;

import com.classbuddy.model.AuditLog;
import com.classbuddy.model.Notification;
import com.classbuddy.model.Notification.NotificationType;
import com.classbuddy.model.NotificationSettings;
import com.classbuddy.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationService {

    /**
     * Create a new notification for a user
     */
    public static boolean createNotification(int userId, int classroomId, NotificationType type,
                                             String title, String message, Integer referenceId) {
        String sql = "INSERT INTO notifications (user_id, classroom_id, type, title, message, reference_id, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, classroomId);
            pstmt.setString(3, type.name());
            pstmt.setString(4, title);
            pstmt.setString(5, message);
            if (referenceId != null) {
                pstmt.setInt(6, referenceId);
            } else {
                pstmt. setNull(6, Types.INTEGER);
            }

            pstmt.executeUpdate();
            System.out.println("Notification created: " + title);
            return true;

        } catch (SQLException e) {
            System.err.println("Error creating notification: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get all unread notifications for a user
     */
    public static List<Notification> getUnreadNotifications(int userId) {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE user_id = ? AND is_read = 0 " +
                "ORDER BY created_at DESC";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    notifications.add(parseNotification(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching notifications: " + e.getMessage());
        }

        return notifications;
    }

    /**
     * Get all notifications for a user (read and unread)
     */
    public static List<Notification> getAllNotifications(int userId, int limit) {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE user_id = ? " +
                "ORDER BY created_at DESC LIMIT ? ";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, limit);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs. next()) {
                    notifications. add(parseNotification(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching all notifications: " + e.getMessage());
        }

        return notifications;
    }

    /**
     * Mark notification as read
     */
    public static boolean markAsRead(int notificationId) {
        return markAsRead(notificationId, -1);
    }

    /**
     * Mark notification as read with audit logging
     */
    public static boolean markAsRead(int notificationId, int userId) {
        String sql = "UPDATE notifications SET is_read = 1 WHERE id = ? ";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, notificationId);
            pstmt.executeUpdate();
            
            if (userId > 0) {
                AuditService.log(userId, AuditLog.Action.MARK_READ, "Notification", notificationId, "is_read=0", "is_read=1");
            }
            
            return true;

        } catch (SQLException e) {
            System.err.println("Error marking notification as read: " + e.getMessage());
            return false;
        }
    }

    /**
     * Mark all notifications as read for a user
     */
    public static boolean markAllAsRead(int userId) {
        String sql = "UPDATE notifications SET is_read = 1 WHERE user_id = ?  AND is_read = 0";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn. prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            int updated = pstmt.executeUpdate();
            System.out.println("Marked " + updated + " notifications as read");
            return true;

        } catch (SQLException e) {
            System.err.println("Error marking all as read: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get notification settings for a user
     */
    public static NotificationSettings getNotificationSettings(int userId) {
        String sql = "SELECT * FROM notification_settings WHERE user_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new NotificationSettings(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getBoolean("enable_exam_notifications"),
                            rs.getBoolean("enable_routine_notifications"),
                            rs.getBoolean("enable_notice_notifications"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    );
                } else {
                    // Create default settings
                    NotificationSettings defaultSettings = new NotificationSettings(userId);
                    saveNotificationSettings(defaultSettings);
                    return defaultSettings;
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching notification settings: " + e.getMessage());
            return new NotificationSettings(userId);  // Return defaults
        }
    }

    /**
     * Save notification settings
     */
    public static boolean saveNotificationSettings(NotificationSettings settings) {
        String sql = "INSERT OR REPLACE INTO notification_settings " +
                "(user_id, enable_exam_notifications, enable_routine_notifications, enable_notice_notifications) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, settings.getUserId());
            pstmt.setBoolean(2, settings.isEnableExamNotifications());
            pstmt.setBoolean(3, settings.isEnableRoutineNotifications());
            pstmt.setBoolean(4, settings.isEnableNoticeNotifications());

            pstmt.executeUpdate();
            System.out.println("Notification settings saved");
            return true;

        } catch (SQLException e) {
            System.err.println("Error saving notification settings: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check and send notifications for upcoming classes (1 hour before)
     */


    /**
     * Send notification to all students in a classroom when notice is posted
     */
    public static void notifyNewNotice(int classroomId, int noticeId, String noticeTitle) {
        String sql = "SELECT cs.student_id FROM classroom_students cs " +
                "JOIN notification_settings ns ON cs.student_id = ns.user_id " +
                "WHERE cs.classroom_id = ? AND ns.enable_notice_notifications = 1 " +
                "AND (cs.enrollment_status IS NULL OR cs.enrollment_status = 'ACTIVE')";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, classroomId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int studentId = rs.getInt("student_id");
                    createNotification(
                            studentId,
                            classroomId,
                            NotificationType.NOTICE,
                            "New Notice Posted",
                            noticeTitle,
                            noticeId
                    );
                }
            }

            System.out.println("Notified students about new notice");

        } catch (SQLException e) {
            System.err.println("Error notifying students: " + e.getMessage());
        }
    }

    /**
     * Helper method to parse notification from ResultSet
     */
    private static Notification parseNotification(ResultSet rs) throws SQLException {
        return new Notification(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getInt("classroom_id"),
                NotificationType.valueOf(rs.getString("type")),
                rs.getString("title"),
                rs.getString("message"),
                rs.getInt("reference_id"),
                rs.getBoolean("is_read"),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }

    /**
     * Get notifications for a user (for notification center display)
     */
    public static List<Notification> getNotificationsForUser(int userId, int limit) {
        return getAllNotifications(userId, limit);
    }

    /**
     * Mark notification as read by ID
     */
    public static void markNotificationAsRead(int notificationId) {
        markAsRead(notificationId);
    }

    /**
     * Delete a notification
     */
    public static boolean deleteNotification(int notificationId) {
        return deleteNotification(notificationId, -1);
    }

    /**
     * Delete a notification with audit logging
     */
    public static boolean deleteNotification(int notificationId, int userId) {
        String sql = "DELETE FROM notifications WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, notificationId);
            pstmt.executeUpdate();
            
            if (userId > 0) {
                AuditService.log(userId, AuditLog.Action.DELETE_NOTIFICATION, "Notification", notificationId, null, null);
            }
            
            return true;

        } catch (SQLException e) {
            System.err.println("Error deleting notification: " + e.getMessage());
            return false;
        }
    }
}