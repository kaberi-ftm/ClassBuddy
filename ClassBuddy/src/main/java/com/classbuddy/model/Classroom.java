package com.classbuddy.model;

import java.time.LocalDateTime;

public class Classroom {
    private int id;
    private int adminId;
    private String name;
    private String section;
    private String department;
    private String passwordHash;
    private LocalDateTime createdAt;

    // Constructor for new classroom (before saving to DB)
    public Classroom(int adminId, String name, String section,
                     String department, String passwordHash) {
        this.adminId = adminId;
        this.name = name;
        this.section = section;
        this.department = department;
        this.passwordHash = passwordHash;
        this.createdAt = LocalDateTime.now();
    }

    // Constructor for existing classroom (from DB)
    public Classroom(int id, int adminId, String name, String section,
                     String department, String passwordHash, LocalDateTime createdAt) {
        this.id = id;
        this.adminId = adminId;
        this.name = name;
        this.section = section;
        this.department = department;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getAdminId() {
        return adminId;
    }

    public String getName() {
        return name;
    }

    public String getSection() {
        return section;
    }

    public String getDepartment() {
        return department;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    @Override
    public String toString() {
        return name + " - " + section;
    }
}