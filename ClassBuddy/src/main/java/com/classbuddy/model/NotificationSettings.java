package com.classbuddy.model;

import java.time.LocalDateTime;

public class NotificationSettings {
    private int id;
    private int userId;
    private int examNotificationHours;
    private int ctQuizNotificationHours;
    private int labTestNotificationHours;
    private int routineNotificationMinutes;
    private boolean enableExamNotifications;
    private boolean enableRoutineNotifications;
    private boolean enableNoticeNotifications;
    private LocalDateTime createdAt;

    // Constructor with defaults
    public NotificationSettings(int userId) {
        this.userId = userId;
        this. examNotificationHours = 24;  // 1 day before
        this.ctQuizNotificationHours = 24;
        this.labTestNotificationHours = 24;
        this.routineNotificationMinutes = 60;  // 1 hour before
        this.enableExamNotifications = true;
        this.enableRoutineNotifications = true;
        this.enableNoticeNotifications = true;
        this.createdAt = LocalDateTime.now();
    }

    // Full constructor
    public NotificationSettings(int id, int userId, int examNotificationHours,
                                int ctQuizNotificationHours, int labTestNotificationHours,
                                int routineNotificationMinutes, boolean enableExamNotifications,
                                boolean enableRoutineNotifications, boolean enableNoticeNotifications,
                                LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.examNotificationHours = examNotificationHours;
        this.ctQuizNotificationHours = ctQuizNotificationHours;
        this.labTestNotificationHours = labTestNotificationHours;
        this.routineNotificationMinutes = routineNotificationMinutes;
        this.enableExamNotifications = enableExamNotifications;
        this.enableRoutineNotifications = enableRoutineNotifications;
        this.enableNoticeNotifications = enableNoticeNotifications;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public int getExamNotificationHours() { return examNotificationHours; }
    public void setExamNotificationHours(int hours) { this.examNotificationHours = hours; }
    public int getCtQuizNotificationHours() { return ctQuizNotificationHours; }
    public void setCtQuizNotificationHours(int hours) { this.ctQuizNotificationHours = hours; }
    public int getLabTestNotificationHours() { return labTestNotificationHours; }
    public void setLabTestNotificationHours(int hours) { this.labTestNotificationHours = hours; }
    public int getRoutineNotificationMinutes() { return routineNotificationMinutes; }
    public void setRoutineNotificationMinutes(int minutes) { this.routineNotificationMinutes = minutes; }
    public boolean isEnableExamNotifications() { return enableExamNotifications; }
    public void setEnableExamNotifications(boolean enable) { this.enableExamNotifications = enable; }
    public boolean isEnableRoutineNotifications() { return enableRoutineNotifications; }
    public void setEnableRoutineNotifications(boolean enable) { this.enableRoutineNotifications = enable; }
    public boolean isEnableNoticeNotifications() { return enableNoticeNotifications; }
    public void setEnableNoticeNotifications(boolean enable) { this.enableNoticeNotifications = enable; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}