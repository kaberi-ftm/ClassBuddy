package com.classbuddy.service;

import com.classbuddy.model.Classroom;
import com. classbuddy.util.DatabaseUtil;
import com.classbuddy.util.  PasswordHasher;
import com.classbuddy.util.ClassIdGenerator;
import com.classbuddy.util.QRCodeGenerator;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClassroomService {

    /**
     * Create a new classroom
     */
    public static boolean createClassroom(int adminId, String name, String section,
                                          String department, String password) {
        String hashedPassword = PasswordHasher.hashPassword(password);
        String classId = ClassIdGenerator.generate(name, section);
        if (!isClassIdAvailable(classId)) {
            System.out.println("Class ID already exists: " + classId);
            return false;
        }

        String qrPath;
        try {
            qrPath = QRCodeGenerator.generateToDataDir(classId, classId);
        } catch (Exception e) {
            System.err.println("QR generation failed: " + e.getMessage());
            qrPath = null;
        }

        String sql = "INSERT INTO classroom (admin_id, name, section, department, class_id, qr_code_path, password_hash) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn. prepareStatement(sql)) {

            pstmt.setInt(1, adminId);
            pstmt.setString(2, name);
            pstmt.setString(3, section);
            pstmt.setString(4, department);
            pstmt.setString(5, classId);
            pstmt.setString(6, qrPath);
            pstmt.setString(7, hashedPassword);

            pstmt.executeUpdate();
            System.out.println("Classroom created:  " + name);
            return true;

        } catch (SQLException e) {
            System.err.println("Error creating classroom: " + e. getMessage());
            return false;
        }
    }

    /**
     * Get all classrooms for a specific admin
     */
    public static List<Classroom> getAdminClassrooms(int adminId) {
        List<Classroom> classrooms = new ArrayList<>();
        String sql = "SELECT * FROM classroom WHERE admin_id = ?  ORDER BY created_at DESC";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, adminId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Classroom classroom = new Classroom(
                            rs.getInt("id"),
                            rs.getInt("admin_id"),
                            rs.getString("name"),
                            rs.getString("section"),
                            rs.getString("department"),
                            rs.getString("class_id"),
                            rs.getString("qr_code_path"),
                            rs.getString("password_hash"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    );
                    classrooms.add(classroom);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching classrooms: " + e.getMessage());
        }

        return classrooms;
    }

    /**
     * Get all classrooms for a specific student
     */
    public static List<Classroom> getStudentClassrooms(int studentId) {
        List<Classroom> classrooms = new ArrayList<>();
        String sql = "SELECT c.* FROM classroom c " +
                "INNER JOIN classroom_students cs ON c.id = cs.classroom_id " +
                "WHERE cs.student_id = ? AND (cs.enrollment_status IS NULL OR cs.enrollment_status = 'ACTIVE') " +
                "ORDER BY c.created_at DESC";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Classroom classroom = new Classroom(
                            rs.getInt("id"),
                            rs.getInt("admin_id"),
                            rs.getString("name"),
                            rs.getString("section"),
                            rs.getString("department"),
                            rs.getString("class_id"),
                            rs.getString("qr_code_path"),
                            rs.getString("password_hash"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    );
                    classrooms.add(classroom);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching student classrooms: " + e.getMessage());
        }

        return classrooms;
    }

    /**
     * Get classroom by ID
     */
    public static Classroom getClassroomById(int classroomId) {
        String sql = "SELECT * FROM classroom WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, classroomId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Classroom(
                            rs.getInt("id"),
                            rs.getInt("admin_id"),
                            rs.getString("name"),
                            rs.getString("section"),
                            rs.getString("department"),
                            rs.getString("class_id"),
                            rs.getString("qr_code_path"),
                            rs.getString("password_hash"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    );
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching classroom: " + e.getMessage());
        }

        return null;
    }

    /**
    /**
     * Get classroom by password (for student joining)
     */
    public static Classroom getClassroomByPassword(String password) {
        String sql = "SELECT * FROM classroom LIMIT 1";

        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement()) {

            try (ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    String hashedPassword = rs.getString("password_hash");
                    if (PasswordHasher.verifyPassword(password, hashedPassword)) {
                        return new Classroom(
                                rs.getInt("id"),
                                rs.getInt("admin_id"),
                                rs.getString("name"),
                                rs.getString("section"),
                                rs.getString("department"),
                                rs.getString("class_id"),
                                rs.getString("qr_code_path"),
                                hashedPassword,
                                rs.getTimestamp("created_at").toLocalDateTime()
                        );
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Error finding classroom by password: " + e.getMessage());
        }

        return null;
    }

    public static Classroom getClassroomByClassId(String classId) {
        String sql = "SELECT * FROM classroom WHERE class_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, classId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Classroom(
                            rs.getInt("id"),
                            rs.getInt("admin_id"),
                            rs.getString("name"),
                            rs.getString("section"),
                            rs.getString("department"),
                            rs.getString("class_id"),
                            rs.getString("qr_code_path"),
                            rs.getString("password_hash"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    );
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching classroom by class_id: " + e.getMessage());
        }

        return null;
    }

    public static boolean isClassIdAvailable(String classId) {
        if (classId == null || classId.trim().isEmpty()) return false;

        String sql = "SELECT COUNT(*) FROM classroom WHERE class_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, classId.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }

        } catch (SQLException e) {
            System.err.println("Error checking class id: " + e.getMessage());
        }

        return false;
    }

    public static boolean updateEnrollmentStatus(int classroomId, int userId, String status) {
        String sql = "UPDATE classroom_students SET enrollment_status = ? WHERE classroom_id = ? AND student_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            pstmt.setInt(2, classroomId);
            pstmt.setInt(3, userId);

            int rows = pstmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("Error updating enrollment status: " + e.getMessage());
            return false;
        }
    }

    /**
     * Add allowed roll number to classroom (Admin adds rolls)
     */
    public static boolean addRollToClassroom(int classroomId, String rollNumber, String studentName) {
        String sql = "INSERT INTO classroom_rolls (classroom_id, roll_number, student_name) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, classroomId);
            pstmt.setString(2, rollNumber);
            pstmt.setString(3, studentName != null ? studentName : "");

            pstmt.executeUpdate();
            System.out.println("Roll " + rollNumber + " added to classroom " + classroomId);
            return true;

        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                System.out.println("Roll " + rollNumber + " already exists in this classroom");
                return false;
            }
            System.err.println("Error adding roll: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get all allowed rolls in a classroom
     */
    public static List<String> getClassroomRolls(int classroomId) {
        List<String> rolls = new ArrayList<>();
        String sql = "SELECT roll_number FROM classroom_rolls WHERE classroom_id = ?  AND is_active = 1";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, classroomId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    rolls.add(rs. getString("roll_number"));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching classroom rolls: " + e.getMessage());
        }

        return rolls;
    }

    /**
     * Check if roll is allowed in classroom
     */
    public static boolean isRollAllowedInClassroom(int classroomId, String rollNumber) {
        String sql = "SELECT COUNT(*) FROM classroom_rolls WHERE classroom_id = ? AND roll_number = ?  AND is_active = 1";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn. prepareStatement(sql)) {

            pstmt.setInt(1, classroomId);
            pstmt.setString(2, rollNumber);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            System.err.println("Error checking roll: " + e.getMessage());
        }

        return false;
    }

    /**
     * Add student to classroom (when student joins)
     */
    public static boolean addStudentToClassroom(int classroomId, int studentId, String rollNumber) {
        String sql = "INSERT INTO classroom_students (classroom_id, student_id, roll_number) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn. prepareStatement(sql)) {

            pstmt.setInt(1, classroomId);
            pstmt.setInt(2, studentId);
            pstmt.setString(3, rollNumber);

            pstmt.executeUpdate();
            System. out.println("Student " + studentId + " added to classroom " + classroomId);
            return true;

        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                System.out.println("Student already joined this classroom");
                return false;
            }
            System.err.println("Error adding student to classroom: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if student already joined classroom
     */
    public static boolean isStudentInClassroom(int classroomId, int studentId) {
        String sql = "SELECT COUNT(*) FROM classroom_students WHERE classroom_id = ? AND student_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, classroomId);
            pstmt.setInt(2, studentId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            System.err.println("Error checking student in classroom: " + e.getMessage());
        }

        return false;
    }

    /**
     * Update classroom details
     */
    public static boolean updateClassroom(int classroomId, String name, String section,
                                          String department) {
        String sql = "UPDATE classroom SET name = ?, section = ?, department = ? WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, section);
            pstmt.setString(3, department);
            pstmt.setInt(4, classroomId);

            pstmt.executeUpdate();
            System.out.println("Classroom updated: " + name);
            return true;

        } catch (SQLException e) {
            System.err.println("Error updating classroom: " + e.getMessage());
            return false;
        }
    }

    /**
     * Delete classroom
     */
    public static boolean deleteClassroom(int classroomId) {
        String sql = "DELETE FROM classroom WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn. prepareStatement(sql)) {

            pstmt.setInt(1, classroomId);
            pstmt.executeUpdate();
            System.out.println("Classroom deleted: ID " + classroomId);
            return true;

        } catch (SQLException e) {
            System.err.println("Error deleting classroom: " + e.getMessage());
            return false;
        }
    }

    /**
     * Verify classroom password
     */
    public static boolean verifyClassroomPassword(int classroomId, String password) {
        String sql = "SELECT password_hash FROM classroom WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, classroomId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String hashedPassword = rs. getString("password_hash");
                    return PasswordHasher.verifyPassword(password, hashedPassword);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error verifying classroom password: " + e.getMessage());
        }

        return false;
    }

    /**
     * Check if classroom name is unique for an admin
     */
    public static boolean isClassroomNameUnique(int adminId, String name) {
        String sql = "SELECT COUNT(*) FROM classroom WHERE admin_id = ? AND name = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, adminId);
            pstmt.setString(2, name);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }

        } catch (SQLException e) {
            System.err.println("Error checking classroom name: " + e.getMessage());
        }

        return false;
    }
}