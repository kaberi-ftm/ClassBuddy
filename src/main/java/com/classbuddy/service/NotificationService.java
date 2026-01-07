package com.classbuddy. service;

import com.classbuddy.model.Notification;
import com.classbuddy.model. Notification.NotificationType;
import com.classbuddy.model.NotificationSettings;
import com.classbuddy.util.DatabaseUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.LocalDate;
import java.util. ArrayList;
import java.util. List;

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
        String sql = "UPDATE notifications SET is_read = 1 WHERE id = ? ";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, notificationId);
            pstmt.executeUpdate();
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
                            rs.getInt("exam_notification_hours"),
                            rs.getInt("ct_quiz_notification_hours"),
                            rs.getInt("lab_test_notification_hours"),
                            rs.getInt("routine_notification_minutes"),
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
                "(user_id, exam_notification_hours, ct_quiz_notification_hours, " +
                "lab_test_notification_hours, routine_notification_minutes, " +
                "enable_exam_notifications, enable_routine_notifications, enable_notice_notifications) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, settings.getUserId());
            pstmt.setInt(2, settings.getExamNotificationHours());
            pstmt.setInt(3, settings.getCtQuizNotificationHours());
            pstmt.setInt(4, settings.getLabTestNotificationHours());
            pstmt.setInt(5, settings.getRoutineNotificationMinutes());
            pstmt.setBoolean(6, settings.isEnableExamNotifications());
            pstmt.setBoolean(7, settings.isEnableRoutineNotifications());
            pstmt.setBoolean(8, settings.isEnableNoticeNotifications());

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
    public static void checkRoutineNotifications() {
        String sql = "SELECT r.*, cs.student_id, ns.routine_notification_minutes " +
                "FROM routine r " +
                "JOIN classroom_students cs ON r.classroom_id = cs.classroom_id " +
                "JOIN notification_settings ns ON cs.student_id = ns.user_id " +
                "WHERE ns.enable_routine_notifications = 1 " +
                "AND (cs.enrollment_status IS NULL OR cs.enrollment_status = 'ACTIVE')";

        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            LocalDateTime now = LocalDateTime.now();
            String currentDay = now.getDayOfWeek().toString();
            LocalTime currentTime = now.toLocalTime();

            while (rs.next()) {
                String day = rs.getString("day");
                LocalTime classTime = rs.getTime("time_start").toLocalTime();
                int studentId = rs.getInt("student_id");
                int notificationMinutes = rs.getInt("routine_notification_minutes");

                // Check if class is today and within notification window
                if (day.equalsIgnoreCase(currentDay)) {
                    long minutesUntilClass = java.time.Duration.between(currentTime, classTime).toMinutes();

                    if (minutesUntilClass > 0 && minutesUntilClass <= notificationMinutes) {
                        // Send notification
                        String courseName = rs.getString("course_name");
                        String room = rs.getString("room");

                        createNotification(
                                studentId,
                                rs.getInt("classroom_id"),
                                NotificationType. ROUTINE,
                                "Class Starting Soon",
                                courseName + " class starts in " + minutesUntilClass + " minutes at " + room,
                                rs.getInt("id")
                        );
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Error checking routine notifications: " + e.getMessage());
        }
    }

    /**
     * Check and send notifications for upcoming exams (24 hours before)
     */
    public static void checkExamNotifications() {
        String sql = "SELECT e.*, cs.student_id, ns.exam_notification_hours, c.name as classroom_name " +
                "FROM exam e " +
                "JOIN classroom c ON e.classroom_id = c.id " +
                "JOIN classroom_students cs ON e.classroom_id = cs.classroom_id " +
                "JOIN notification_settings ns ON cs. student_id = ns.user_id " +
                "WHERE ns.enable_exam_notifications = 1 " +
                "AND (cs.enrollment_status IS NULL OR cs.enrollment_status = 'ACTIVE') " +
                "AND e.exam_date >= DATE('now')";

        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            LocalDateTime now = LocalDateTime.now();

            while (rs.next()) {
                LocalDate examDate = rs.getDate("exam_date").toLocalDate();
                LocalTime examTime = rs.getTime("exam_time").toLocalTime();
                LocalDateTime examDateTime = LocalDateTime.of(examDate, examTime);

                int studentId = rs.getInt("student_id");
                int notificationHours = rs.getInt("exam_notification_hours");

                long hoursUntilExam = java.time.Duration.between(now, examDateTime).toHours();

                if (hoursUntilExam > 0 && hoursUntilExam <= notificationHours) {
                    String courseName = rs.getString("course_name");
                    String examType = rs.getString("exam_type");
                    String classroomName = rs.getString("classroom_name");

                    createNotification(
                            studentId,
                            rs.getInt("classroom_id"),
                            NotificationType.EXAM,
                            examType + " Exam Tomorrow",
                            courseName + " " + examType + " exam in " + classroomName + " at " + examTime,
                            rs.getInt("id")
                    );
                }
            }

        } catch (SQLException e) {
            System.err.println("Error checking exam notifications: " + e.getMessage());
        }
    }

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
                rs. getInt("id"),
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
}