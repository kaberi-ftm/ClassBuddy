package com.classbuddy.service;

import com.classbuddy.model.Notice;
import com.classbuddy.util.DatabaseUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing notices/announcements
 */
public class NoticeService {

    /**
     * ADMIN: Post a notice
     */
    public static boolean postNotice(int classroomId, String title,
                                     String content, String category,
                                     int createdBy) {
        String sql = "INSERT INTO notice " +
                "(classroom_id, title, content, category, created_by) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, classroomId);
            pstmt.setString(2, title);
            pstmt.setString(3, content);
            pstmt.setString(4, category);
            pstmt.setInt(5, createdBy);

            pstmt. executeUpdate();
            System.out.println("✅ Notice posted:  " + title);
            return true;

        } catch (SQLException e) {
            System.err. println("❌ Error posting notice: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get all notices for a classroom (pinned first)
     */
    public static List<Notice> getClassroomNotices(int classroomId) {
        List<Notice> notices = new ArrayList<>();
        String sql = "SELECT * FROM notice WHERE classroom_id = ? " +
                "ORDER BY is_pinned DESC, created_at DESC";

        try (Connection conn = DatabaseUtil. getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, classroomId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Notice notice = new Notice(
                            rs.getInt("id"),
                            rs.getInt("classroom_id"),
                            rs.getString("title"),
                            rs.getString("content"),
                            rs.getString("category"),
                            rs.getBoolean("is_pinned"),
                            rs.getInt("created_by"),
                            rs. getTimestamp("created_at").toLocalDateTime()
                    );
                    notices.add(notice);
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Error fetching notices");
        }

        return notices;
    }

    /**
     * Get notices by category
     */
    public static List<Notice> getNoticesByCategory(int classroomId, String category) {
        List<Notice> notices = new ArrayList<>();
        String sql = "SELECT * FROM notice WHERE classroom_id = ? " +
                "AND category = ? ORDER BY created_at DESC";

        try (Connection conn = DatabaseUtil. getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, classroomId);
            pstmt.setString(2, category);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs. next()) {
                    Notice notice = new Notice(
                            rs.getInt("id"),
                            rs.getInt("classroom_id"),
                            rs.getString("title"),
                            rs. getString("content"),
                            rs.getString("category"),
                            rs.getBoolean("is_pinned"),
                            rs. getInt("created_by"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    );
                    notices.add(notice);
                }
            }

        } catch (SQLException e) {
            System. err.println("❌ Error fetching notices by category");
        }

        return notices;
    }

    /**
     * Pin/Unpin notice
     */
    public static boolean togglePin(int noticeId, boolean pin) {
        String sql = "UPDATE notice SET is_pinned = ? WHERE id = ?";

        try (Connection conn = DatabaseUtil. getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, pin);
            pstmt.setInt(2, noticeId);

            pstmt. executeUpdate();
            System.out.println("✅ Notice " + (pin ? "pinned" : "unpinned"));
            return true;

        } catch (SQLException e) {
            System.err.println("❌ Error toggling pin");
            return false;
        }
    }

    /**
     * Delete notice
     */
    public static boolean deleteNotice(int noticeId) {
        String sql = "DELETE FROM notice WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt. setInt(1, noticeId);
            pstmt.executeUpdate();
            System.out. println("✅ Notice deleted");
            return true;

        } catch (SQLException e) {
            System.err.println("❌ Error deleting notice");
            return false;
        }
    }

    /**
     * Update notice
     */
    public static boolean updateNotice(int noticeId, String title,
                                       String content, String category) {
        String sql = "UPDATE notice SET title=?, content=?, category=? " +
                "WHERE id=?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, title);
            pstmt.setString(2, content);
            pstmt.setString(3, category);
            pstmt.setInt(4, noticeId);

            pstmt.executeUpdate();
            System.out.println("✅ Notice updated");
            return true;

        } catch (SQLException e) {
            System. err.println("❌ Error updating notice");
            return false;
        }
    }
}