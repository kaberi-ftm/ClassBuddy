package com.classbuddy.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a CT (Class Test) or Quiz in a classroom
 */
public class CTQuiz {
    private int id;
    private int classroomId;
    private String name;
    private String syllabus;
    private LocalDate deadline;
    private boolean isCompleted;
    private LocalDateTime createdAt;

    // Constructor for NEW CT/Quiz
    public CTQuiz(int classroomId, String name, String syllabus, LocalDate deadline) {
        this.classroomId = classroomId;
        this.name = name;
        this.syllabus = syllabus;
        this.deadline = deadline;
        this.isCompleted = false;
        this.createdAt = LocalDateTime.now();
    }

    // Constructor for EXISTING CT/Quiz (from DB)
    public CTQuiz(int id, int classroomId, String name, String syllabus,
                  LocalDate deadline, boolean isCompleted, LocalDateTime createdAt) {
        this.id = id;
        this.classroomId = classroomId;
        this.name = name;
        this.syllabus = syllabus;
        this.deadline = deadline;
        this.isCompleted = isCompleted;
        this.createdAt = createdAt;
    }

    // Getters
    public int getId() { return id; }
    public int getClassroomId() { return classroomId; }
    public String getName() { return name; }
    public String getSyllabus() { return syllabus; }
    public LocalDate getDeadline() { return deadline; }
    public boolean isCompleted() { return isCompleted; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setSyllabus(String syllabus) { this.syllabus = syllabus; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    @Override
    public String toString() {
        return name + " - Deadline: " + deadline + 
               (isCompleted ? " (Completed)" : " (Pending)");
    }
}
