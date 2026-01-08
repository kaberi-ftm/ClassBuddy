-- Migration: Add Attendance Tracking
-- Date: 2026-01-08
-- Purpose: Enable teachers to mark student attendance for each class session

CREATE TABLE IF NOT EXISTS attendance_record (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    classroom_id INTEGER NOT NULL,
    student_id INTEGER NOT NULL,
    attendance_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PRESENT', 'ABSENT', 'LATE', 'EXCUSE')),
    marked_by INTEGER,
    remarks TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (classroom_id) REFERENCES classroom(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (marked_by) REFERENCES users(id) ON DELETE SET NULL,
    UNIQUE(classroom_id, student_id, attendance_date)
);

CREATE INDEX IF NOT EXISTS idx_attendance_classroom_date 
    ON attendance_record(classroom_id, attendance_date);

CREATE INDEX IF NOT EXISTS idx_attendance_student 
    ON attendance_record(student_id, attendance_date);
