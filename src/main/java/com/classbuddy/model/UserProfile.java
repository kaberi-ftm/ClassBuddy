package com.classbuddy.model;

import java.time.LocalDateTime;

/**
 * UserProfile - Extended user information and preferences
 */
public class UserProfile {
    private int userId;
    private String fullName;
    private String phoneNumber;
    private String address;
    private String bio;
    private String avatarUrl;
    private String department;
    private String studentId;  // For students
    private String designation;  // For teachers
    private LocalDateTime dateOfBirth;
    private LocalDateTime updatedAt;

    // Constructor for new profile
    public UserProfile(int userId) {
        this.userId = userId;
        this.updatedAt = LocalDateTime.now();
    }

    // Constructor for existing profile (from DB)
    public UserProfile(int userId, String fullName, String phoneNumber, String address,
                      String bio, String avatarUrl, String department, String studentId,
                      String designation, LocalDateTime dateOfBirth, LocalDateTime updatedAt) {
        this.userId = userId;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.bio = bio;
        this.avatarUrl = avatarUrl;
        this.department = department;
        this.studentId = studentId;
        this.designation = designation;
        this.dateOfBirth = dateOfBirth;
        this.updatedAt = updatedAt;
    }

    // Getters
    public int getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public String getBio() {
        return bio;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getDepartment() {
        return department;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getDesignation() {
        return designation;
    }

    public LocalDateTime getDateOfBirth() {
        return dateOfBirth;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Setters
    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
        this.updatedAt = LocalDateTime.now();
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        this.updatedAt = LocalDateTime.now();
    }

    public void setAddress(String address) {
        this.address = address;
        this.updatedAt = LocalDateTime.now();
    }

    public void setBio(String bio) {
        this.bio = bio;
        this.updatedAt = LocalDateTime.now();
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
        this.updatedAt = LocalDateTime.now();
    }

    public void setDepartment(String department) {
        this.department = department;
        this.updatedAt = LocalDateTime.now();
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
        this.updatedAt = LocalDateTime.now();
    }

    public void setDesignation(String designation) {
        this.designation = designation;
        this.updatedAt = LocalDateTime.now();
    }

    public void setDateOfBirth(LocalDateTime dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
        this.updatedAt = LocalDateTime.now();
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "UserProfile{" +
                "userId=" + userId +
                ", fullName='" + fullName + '\'' +
                ", department='" + department + '\'' +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
