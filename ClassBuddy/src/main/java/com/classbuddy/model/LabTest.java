package com.classbuddy.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a lab test in a classroom
 */
public class LabTest {
    private int id;
    private int classroomId;
    private LocalDate testDate;
    private String experimentNumber;
    private String teacherName;
    private String evaluationCriteria;
    private LocalDateTime createdAt;

    // Constructor for NEW lab test
    public LabTest(int classroomId, LocalDate testDate, String experimentNumber,
                   String teacherName, String evaluationCriteria) {
        this.classroomId = classroomId;
        this.testDate = testDate;
        this.experimentNumber = experimentNumber;
        this.teacherName = teacherName;
        this.evaluationCriteria = evaluationCriteria;
        this.createdAt = LocalDateTime.now();
    }

    // Constructor for EXISTING lab test (from DB)
    public LabTest(int id, int classroomId, LocalDate testDate, String experimentNumber,
                   String teacherName, String evaluationCriteria, LocalDateTime createdAt) {
        this.id = id;
        this.classroomId = classroomId;
        this.testDate = testDate;
        this.experimentNumber = experimentNumber;
        this.teacherName = teacherName;
        this.evaluationCriteria = evaluationCriteria;
        this.createdAt = createdAt;
    }

    // Getters
    public int getId() { return id; }
    public int getClassroomId() { return classroomId; }
    public LocalDate getTestDate() { return testDate; }
    public String getExperimentNumber() { return experimentNumber; }
    public String getTeacherName() { return teacherName; }
    public String getEvaluationCriteria() { return evaluationCriteria; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters
    public void setTestDate(LocalDate testDate) { this.testDate = testDate; }
    public void setExperimentNumber(String experimentNumber) { this.experimentNumber = experimentNumber; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
    public void setEvaluationCriteria(String evaluationCriteria) { this.evaluationCriteria = evaluationCriteria; }

    @Override
    public String toString() {
        return "Lab Test - Experiment " + experimentNumber + " on " + testDate +
               " (Teacher: " + teacherName + ")";
    }
}
