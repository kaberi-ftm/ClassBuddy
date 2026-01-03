package com.classbuddy.model;

import java. time.LocalDateTime;

/**
 * Represents a notice/announcement in a classroom
 */
public class Notice {
    private int id;
    private int classroomId;
    private String title;
    private String content;
    private String category;           // "Routine", "Exam", "CT", "General"
    private boolean isPinned;
    private int createdBy;             // Admin who created it
    private LocalDateTime createdAt;

    // Constructor for NEW notice
    public Notice(int classroomId, String title, String content,
                  String category, int createdBy) {
        this.classroomId = classroomId;
        this.title = title;
        this.content = content;
        this.category = category;
        this.createdBy = createdBy;
        this.isPinned = false;
        this.createdAt = LocalDateTime. now();
    }

    // Constructor for EXISTING notice
    public Notice(int id, int classroomId, String title, String content,
                  String category, boolean isPinned, int createdBy,
                  LocalDateTime createdAt) {
        this.id = id;
        this.classroomId = classroomId;
        this.title = title;
        this.content = content;
        this.category = category;
        this.isPinned = isPinned;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    // Getters
    public int getId() { return id; }
    public int getClassroomId() { return classroomId; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getCategory() { return category; }
    public boolean isPinned() { return isPinned; }
    public int getCreatedBy() { return createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters
    public void setPinned(boolean pinned) { this.isPinned = pinned; }

    @Override
    public String toString() {
        return title + " [" + category + "] - " + createdAt;
    }
}