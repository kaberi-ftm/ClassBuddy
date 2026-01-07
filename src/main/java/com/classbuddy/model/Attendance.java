package com.classbuddy.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents an attendance record for a student in a classroom
 */
public class Attendance {
    
    public enum Status {
        PRESENT("Present"),
        ABSENT("Absent"),
        LATE("Late");
        
        private final String displayName;
        
        Status(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
    
    private int id;
    private int classroomId;
    private int studentId;
    private String rollNumber;
    private Integer routineId;  // Nullable - specific routine/period
    private LocalDate date;
    private Status status;
    private int markedByUserId;
    private LocalDateTime markedAt;
    
    // Constructor for NEW attendance record
    public Attendance(int classroomId, int studentId, String rollNumber, LocalDate date, 
                     Status status, int markedByUserId) {
        this.classroomId = classroomId;
        this.studentId = studentId;
        this.rollNumber = rollNumber;
        this.date = date;
        this.status = status;
        this.markedByUserId = markedByUserId;
        this.markedAt = LocalDateTime.now();
    }
    
    // Constructor for NEW attendance with routine
    public Attendance(int classroomId, int studentId, String rollNumber, Integer routineId,
                     LocalDate date, Status status, int markedByUserId) {
        this.classroomId = classroomId;
        this.studentId = studentId;
        this.rollNumber = rollNumber;
        this.routineId = routineId;
        this.date = date;
        this.status = status;
        this.markedByUserId = markedByUserId;
        this.markedAt = LocalDateTime.now();
    }
    
    // Constructor for EXISTING attendance (from DB)
    public Attendance(int id, int classroomId, int studentId, String rollNumber, Integer routineId,
                     LocalDate date, Status status, int markedByUserId, LocalDateTime markedAt) {
        this.id = id;
        this.classroomId = classroomId;
        this.studentId = studentId;
        this.rollNumber = rollNumber;
        this.routineId = routineId;
        this.date = date;
        this.status = status;
        this.markedByUserId = markedByUserId;
        this.markedAt = markedAt;
    }
    
    // Getters
    public int getId() { return id; }
    public int getClassroomId() { return classroomId; }
    public int getStudentId() { return studentId; }
    public String getRollNumber() { return rollNumber; }
    public Integer getRoutineId() { return routineId; }
    public LocalDate getDate() { return date; }
    public Status getStatus() { return status; }
    public int getMarkedByUserId() { return markedByUserId; }
    public LocalDateTime getMarkedAt() { return markedAt; }
    
    // Setters
    public void setId(int id) { this.id = id; }
    public void setStatus(Status status) { this.status = status; }
    public void setRoutineId(Integer routineId) { this.routineId = routineId; }
    
    @Override
    public String toString() {
        return String.format("Attendance[id=%d, student=%d, roll=%s, date=%s, status=%s]",
                           id, studentId, rollNumber, date, status);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Attendance that = (Attendance) obj;
        return id == that.id;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
