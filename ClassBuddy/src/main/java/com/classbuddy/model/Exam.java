package com.classbuddy.model;

import java. time.LocalDate;
import java. time.LocalTime;
import java.time.LocalDateTime;

/**
 * Represents an exam/test in a classroom
 */
public class Exam {
    private int id;
    private int classroomId;
    private String courseName;
    private String examType;           // "Mid", "Final", "Viva"
    private LocalDate examDate;
    private LocalTime examTime;
    private String room;
    private LocalDateTime createdAt;

    // Constructor for NEW exam
    public Exam(int classroomId, String courseName, String examType,
                LocalDate examDate, LocalTime examTime, String room) {
        this.classroomId = classroomId;
        this.courseName = courseName;
        this.examType = examType;
        this.examDate = examDate;
        this.examTime = examTime;
        this.room = room;
        this.createdAt = LocalDateTime.now();
    }

    // Constructor for EXISTING exam (from DB)
    public Exam(int id, int classroomId, String courseName, String examType,
                LocalDate examDate, LocalTime examTime, String room,
                LocalDateTime createdAt) {
        this.id = id;
        this.classroomId = classroomId;
        this.courseName = courseName;
        this.examType = examType;
        this.examDate = examDate;
        this.examTime = examTime;
        this.room = room;
        this.createdAt = createdAt;
    }

    // Getters
    public int getId() { return id; }
    public int getClassroomId() { return classroomId; }
    public String getCourseName() { return courseName; }
    public String getExamType() { return examType; }
    public LocalDate getExamDate() { return examDate; }
    public LocalTime getExamTime() { return examTime; }
    public String getRoom() { return room; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        return courseName + " (" + examType + ") - " + examDate + " at " + examTime;
    }
}