package com.classbuddy.service;

import com.classbuddy.model.CalendarEvent;
import com.classbuddy.util.DatabaseUtil;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * CalendarService - Manages calendar events
 */
public class CalendarService {

    /**
     * Get all calendar events for a classroom
     */
    public static List<CalendarEvent> getClassroomEvents(int classroomId) {
        List<CalendarEvent> events = new ArrayList<>();
        String sql = "SELECT * FROM calendar_events WHERE classroom_id = ? ORDER BY event_date, start_time";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, classroomId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                events.add(createEventFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching classroom events: " + e.getMessage());
        }

        return events;
    }

    /**
     * Get events for a specific date range
     */
    public static List<CalendarEvent> getEventsByDateRange(int classroomId, LocalDate startDate, LocalDate endDate) {
        List<CalendarEvent> events = new ArrayList<>();
        String sql = "SELECT * FROM calendar_events WHERE classroom_id = ? " +
                    "AND event_date BETWEEN ? AND ? ORDER BY event_date, start_time";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, classroomId);
            pstmt.setDate(2, Date.valueOf(startDate));
            pstmt.setDate(3, Date.valueOf(endDate));
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                events.add(createEventFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching events by date range: " + e.getMessage());
        }

        return events;
    }

    /**
     * Get events for a specific date
     */
    public static List<CalendarEvent> getEventsByDate(int classroomId, LocalDate date) {
        return getEventsByDateRange(classroomId, date, date);
    }

    /**
     * Save calendar event
     */
    public static boolean saveEvent(CalendarEvent event) {
        String sql = "INSERT INTO calendar_events (classroom_id, event_type, reference_id, title, " +
                    "description, event_date, start_time, end_time, location, color, created_by, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, event.getClassroomId());
            pstmt.setString(2, event.getEventType().toString());
            pstmt.setObject(3, event.getReferenceId());
            pstmt.setString(4, event.getTitle());
            pstmt.setString(5, event.getDescription());
            pstmt.setDate(6, Date.valueOf(event.getEventDate()));
            pstmt.setTime(7, event.getStartTime() != null ? Time.valueOf(event.getStartTime()) : null);
            pstmt.setTime(8, event.getEndTime() != null ? Time.valueOf(event.getEndTime()) : null);
            pstmt.setString(9, event.getLocation());
            pstmt.setString(10, event.getColor());
            pstmt.setObject(11, event.getCreatedBy());
            pstmt.setTimestamp(12, Timestamp.valueOf(LocalDateTime.now()));

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    event.setId(rs.getInt(1));
                }
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error saving calendar event: " + e.getMessage());
        }

        return false;
    }

    /**
     * Update calendar event
     */
    public static boolean updateEvent(CalendarEvent event) {
        String sql = "UPDATE calendar_events SET title = ?, description = ?, event_date = ?, " +
                    "start_time = ?, end_time = ?, location = ?, color = ? WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, event.getTitle());
            pstmt.setString(2, event.getDescription());
            pstmt.setDate(3, Date.valueOf(event.getEventDate()));
            pstmt.setTime(4, event.getStartTime() != null ? Time.valueOf(event.getStartTime()) : null);
            pstmt.setTime(5, event.getEndTime() != null ? Time.valueOf(event.getEndTime()) : null);
            pstmt.setString(6, event.getLocation());
            pstmt.setString(7, event.getColor());
            pstmt.setInt(8, event.getId());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error updating calendar event: " + e.getMessage());
            return false;
        }
    }

    /**
     * Delete calendar event
     */
    public static boolean deleteEvent(int eventId) {
        String sql = "DELETE FROM calendar_events WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, eventId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting calendar event: " + e.getMessage());
            return false;
        }
    }

    /**
     * Helper method to create event from ResultSet
     */
    private static CalendarEvent createEventFromResultSet(ResultSet rs) throws SQLException {
        return new CalendarEvent(
                rs.getInt("id"),
                rs.getInt("classroom_id"),
                CalendarEvent.EventType.valueOf(rs.getString("event_type")),
                (Integer) rs.getObject("reference_id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getDate("event_date").toLocalDate(),
                rs.getTime("start_time") != null ? rs.getTime("start_time").toLocalTime() : null,
                rs.getTime("end_time") != null ? rs.getTime("end_time").toLocalTime() : null,
                rs.getString("location"),
                rs.getString("color"),
                (Integer) rs.getObject("created_by"),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }

    /**
     * Sync exam to calendar
     */
    public static void syncExamToCalendar(int examId, int classroomId, String courseName, 
                                         LocalDate examDate, java.time.LocalTime examTime, String room) {
        CalendarEvent event = new CalendarEvent(
                classroomId,
                CalendarEvent.EventType.EXAM,
                "Exam: " + courseName,
                examDate,
                examTime,
                examTime.plusHours(2)  // Default 2 hour duration
        );
        event.setReferenceId(examId);
        event.setLocation(room);
        event.setDescription("Examination for " + courseName);
        event.setColor("#ef4444");  // Red for exams

        saveEvent(event);
    }

    /**
     * Sync routine to calendar
     */
    public static void syncRoutineToCalendar(int routineId, int classroomId, String courseName, 
                                            String day, java.time.LocalTime startTime, java.time.LocalTime endTime, 
                                            String room, String teacher) {
        // Create event for upcoming occurrence
        LocalDate nextDate = getNextDateForDay(day);
        
        CalendarEvent event = new CalendarEvent(
                classroomId,
                CalendarEvent.EventType.ROUTINE,
                courseName,
                nextDate,
                startTime,
                endTime
        );
        event.setReferenceId(routineId);
        event.setLocation(room);
        event.setDescription("Teacher: " + teacher);
        event.setColor("#f97316");  // Orange for routine

        saveEvent(event);
    }

    /**
     * Helper method to get next date for a day of week
     */
    private static LocalDate getNextDateForDay(String dayName) {
        LocalDate today = LocalDate.now();
        java.time.DayOfWeek targetDay = java.time.DayOfWeek.valueOf(dayName.toUpperCase());
        java.time.DayOfWeek currentDay = today.getDayOfWeek();
        
        int daysUntilTarget = targetDay.getValue() - currentDay.getValue();
        if (daysUntilTarget <= 0) {
            daysUntilTarget += 7;
        }
        
        return today.plusDays(daysUntilTarget);
    }
}
