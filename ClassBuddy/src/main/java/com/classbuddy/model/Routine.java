package com.classbuddy.model;

import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * Represents a class routine/schedule entry
 */
public class Routine {
    private int id;
    private int classroomId;
    private String day;                // Monday, Tuesday, etc.
    private int periodNumber;          // 1st period, 2nd period, etc.
    private String courseName;
    private String teacherName;
    private String room;
    private LocalTime timeStart;
    private LocalTime timeEnd;
    private LocalDateTime createdAt;

    // Constructor for NEW routine
    public Routine(int classroomId, String day, int periodNumber,
                   String courseName, String teacherName, String room,
                   LocalTime timeStart, LocalTime timeEnd) {
        this.classroomId = classroomId;
        this.day = day;
        this.periodNumber = periodNumber;
        this.courseName = courseName;
        this.teacherName = teacherName;
        this.room = room;
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
        this.createdAt = LocalDateTime.now();
    }

    // Constructor for EXISTING routine
    public Routine(int id, int classroomId, String day, int periodNumber,
                   String courseName, String teacherName, String room,
                   LocalTime timeStart, LocalTime timeEnd,
                   LocalDateTime createdAt) {
        this.id = id;
        this.classroomId = classroomId;
        this.day = day;
        this.periodNumber = periodNumber;
        this.courseName = courseName;
        this. teacherName = teacherName;
        this.room = room;
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
        this. createdAt = createdAt;
    }

    // Getters
    public int getId() { return id; }
    public int getClassroomId() { return classroomId; }
    public String getDay() { return day; }
    public int getPeriodNumber() { return periodNumber; }
    public String getCourseName() { return courseName; }
    public String getTeacherName() { return teacherName; }
    public String getRoom() { return room; }
    public LocalTime getTimeStart() { return timeStart; }
    public LocalTime getTimeEnd() { return timeEnd; }

    @Override
    public String toString() {
        return day + " - Period " + periodNumber + ": " + courseName +
                " (" + timeStart + "-" + timeEnd + ")";
    }
}