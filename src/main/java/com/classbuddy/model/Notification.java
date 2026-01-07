package com.classbuddy.model;

import java.time.LocalDateTime;

public class Notification {
    private int id;
    private int userId;
    private int classroomId;
    private NotificationType type;
    private String title;
    private String message;
    private Integer referenceId;
    private boolean isRead;
    private LocalDateTime createdAt;

    public enum NotificationType {
        ROUTINE("Class Starting Soon"),
        EXAM("Upcoming Exam"),
        NOTICE("New Notice"),
        CT_QUIZ("CT/Quiz Deadline"),
        LAB_TEST("Lab Test Scheduled");

        private final String displayName;

        NotificationType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Constructor for new notification
    public Notification(int userId, int classroomId, NotificationType type,
                        String title, String message, Integer referenceId) {
        this.userId = userId;
        this.classroomId = classroomId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.referenceId = referenceId;
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
    }

    // Constructor for existing notification
    public Notification(int id, int userId, int classroomId, NotificationType type,
                        String title, String message, Integer referenceId,
                        boolean isRead, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.classroomId = classroomId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.referenceId = referenceId;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    // Getters
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public int getClassroomId() { return classroomId; }
    public NotificationType getType() { return type; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public Integer getReferenceId() { return referenceId; }
    public boolean isRead() { return isRead; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters
    public void setRead(boolean read) { isRead = read; }

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", type=" + type +
                ", title='" + title + '\'' +
                ", isRead=" + isRead +
                '}';
    }
}