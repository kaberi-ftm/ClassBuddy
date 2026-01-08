package com.classbuddy.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * AuditLog - Tracks all user actions in the system for compliance and debugging
 */
public class AuditLog {

    public enum Action {
        CREATE, UPDATE, DELETE, VIEW, LOGIN, LOGOUT, MARK_READ, DELETE_NOTIFICATION
    }

    private int id;
    private int userId;
    private Action action;
    private String entityType;        // "Routine", "Exam", "Classroom", "Notification", etc.
    private int entityId;
    private String oldValue;          // Previous state (JSON string for complex objects)
    private String newValue;          // Current state (JSON string for complex objects)
    private LocalDateTime timestamp;

    // Constructor
    public AuditLog(int userId, Action action, String entityType, int entityId, String oldValue, String newValue) {
        this.userId = userId;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.timestamp = LocalDateTime.now();
    }

    // Full constructor with ID and timestamp
    public AuditLog(int id, int userId, Action action, String entityType, int entityId, String oldValue, String newValue, LocalDateTime timestamp) {
        this.id = id;
        this.userId = userId;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public int getEntityId() {
        return entityId;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return String.format("[%s] User %d %s %s #%d | Before: %s → After: %s",
                timestamp.format(formatter), userId, action, entityType, entityId, oldValue, newValue);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AuditLog log = (AuditLog) obj;
        return id == log.id && userId == log.userId && entityId == log.entityId &&
               action == log.action && entityType.equals(log.entityType);
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id) * 31 + Integer.hashCode(userId) * 31 + entityId * 31;
    }
}
