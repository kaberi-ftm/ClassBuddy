package com.classbuddy.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * CalendarEvent - Represents a calendar event in a classroom
 */
public class CalendarEvent {
    private int id;
    private int classroomId;
    private EventType eventType;
    private Integer referenceId;  // Links to exam/routine/etc
    private String title;
    private String description;
    private LocalDate eventDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String location;
    private String color;
    private Integer createdBy;
    private LocalDateTime createdAt;

    public enum EventType {
        ROUTINE, EXAM, CT_QUIZ, LAB_TEST, CUSTOM
    }

    // Constructor for new event
    public CalendarEvent(int classroomId, EventType eventType, String title, 
                        LocalDate eventDate, LocalTime startTime, LocalTime endTime) {
        this.classroomId = classroomId;
        this.eventType = eventType;
        this.title = title;
        this.eventDate = eventDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.color = "#f97316";  // Default orange
        this.createdAt = LocalDateTime.now();
    }

    // Constructor for existing event (from DB)
    public CalendarEvent(int id, int classroomId, EventType eventType, Integer referenceId,
                        String title, String description, LocalDate eventDate, 
                        LocalTime startTime, LocalTime endTime, String location, 
                        String color, Integer createdBy, LocalDateTime createdAt) {
        this.id = id;
        this.classroomId = classroomId;
        this.eventType = eventType;
        this.referenceId = referenceId;
        this.title = title;
        this.description = description;
        this.eventDate = eventDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.color = color;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    // Getters
    public int getId() { return id; }
    public int getClassroomId() { return classroomId; }
    public EventType getEventType() { return eventType; }
    public Integer getReferenceId() { return referenceId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDate getEventDate() { return eventDate; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public String getLocation() { return location; }
    public String getColor() { return color; }
    public Integer getCreatedBy() { return createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setClassroomId(int classroomId) { this.classroomId = classroomId; }
    public void setEventType(EventType eventType) { this.eventType = eventType; }
    public void setReferenceId(Integer referenceId) { this.referenceId = referenceId; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public void setLocation(String location) { this.location = location; }
    public void setColor(String color) { this.color = color; }
    public void setCreatedBy(Integer createdBy) { this.createdBy = createdBy; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "CalendarEvent{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", eventType=" + eventType +
                ", eventDate=" + eventDate +
                ", startTime=" + startTime +
                '}';
    }
}
