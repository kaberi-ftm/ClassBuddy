package com.classbuddy.model;

import java.time.LocalDateTime;

public class LabEvaluation {
    private final int id;
    private final int classroomId;
    private final int labTestId;
    private final Integer studentId;
    private final String rollNumber;
    private final Double score;
    private final Double total;
    private final String grade;
    private final String remarks;
    private final LocalDateTime recordedAt;

    public LabEvaluation(int id, int classroomId, int labTestId, Integer studentId, String rollNumber,
                         Double score, Double total, String grade, String remarks, LocalDateTime recordedAt) {
        this.id = id;
        this.classroomId = classroomId;
        this.labTestId = labTestId;
        this.studentId = studentId;
        this.rollNumber = rollNumber;
        this.score = score;
        this.total = total;
        this.grade = grade;
        this.remarks = remarks;
        this.recordedAt = recordedAt;
    }

    public int getId() { return id; }
    public int getClassroomId() { return classroomId; }
    public int getLabTestId() { return labTestId; }
    public Integer getStudentId() { return studentId; }
    public String getRollNumber() { return rollNumber; }
    public Double getScore() { return score; }
    public Double getTotal() { return total; }
    public String getGrade() { return grade; }
    public String getRemarks() { return remarks; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
}
