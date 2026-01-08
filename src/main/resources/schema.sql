
CREATE TABLE IF NOT EXISTS users (
                                     id INTEGER PRIMARY KEY AUTOINCREMENT,
                                     username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK(role IN ('ADMIN', 'STUDENT')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );


CREATE TABLE IF NOT EXISTS classroom (
                                         id INTEGER PRIMARY KEY AUTOINCREMENT,
                                         admin_id INTEGER NOT NULL,
                                         name VARCHAR(100) NOT NULL,
    section VARCHAR(50),
    department VARCHAR(50),
    class_id VARCHAR(50) UNIQUE,
    qr_code_path VARCHAR(255),
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (admin_id) REFERENCES users(id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS classroom_rolls (
                                               id INTEGER PRIMARY KEY AUTOINCREMENT,
                                               classroom_id INTEGER NOT NULL,
                                               roll_number VARCHAR(20) NOT NULL,
    student_name VARCHAR(100),
    is_active BOOLEAN DEFAULT 1,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(classroom_id, roll_number),
    FOREIGN KEY (classroom_id) REFERENCES classroom(id) ON DELETE CASCADE
    );


CREATE TABLE IF NOT EXISTS classroom_students (
                                                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                                                  classroom_id INTEGER NOT NULL,
                                                  student_id INTEGER NOT NULL,
                                                  roll_number VARCHAR(20) NOT NULL,
    enrollment_status TEXT DEFAULT 'ACTIVE',
    enrolled_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(classroom_id, student_id),
    FOREIGN KEY (classroom_id) REFERENCES classroom(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE
    );


CREATE TABLE IF NOT EXISTS routine (
                                       id INTEGER PRIMARY KEY AUTOINCREMENT,
                                       classroom_id INTEGER NOT NULL,
                                       day VARCHAR(15) NOT NULL,
    applicable_days TEXT,
    period_number INTEGER NOT NULL,
    course_name VARCHAR(100) NOT NULL,
    teacher_name VARCHAR(100),
    room VARCHAR(50),
    time_start TIME NOT NULL,
    time_end TIME NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (classroom_id) REFERENCES classroom(id) ON DELETE CASCADE
    );


CREATE TABLE IF NOT EXISTS exam (
                                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                                    classroom_id INTEGER NOT NULL,
                                    course_name VARCHAR(100) NOT NULL,
    exam_type VARCHAR(20) CHECK(exam_type IN ('Mid', 'Final', 'Viva', 'CT', 'Quiz', 'Lab Test')),
    exam_date DATE NOT NULL,
    exam_time TIME NOT NULL,
    room VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (classroom_id) REFERENCES classroom(id) ON DELETE CASCADE
    );


CREATE TABLE IF NOT EXISTS ct_quiz (
                                       id INTEGER PRIMARY KEY AUTOINCREMENT,
                                       classroom_id INTEGER NOT NULL,
                                       name VARCHAR(100) NOT NULL,
    syllabus TEXT,
    deadline DATE NOT NULL,
    is_completed BOOLEAN DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (classroom_id) REFERENCES classroom(id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS lab_test (
                                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                                        classroom_id INTEGER NOT NULL,
                                        test_date DATE NOT NULL,
                                        experiment_number VARCHAR(50),
    teacher_name VARCHAR(100),
    evaluation_criteria TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (classroom_id) REFERENCES classroom(id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS notice (
                                      id INTEGER PRIMARY KEY AUTOINCREMENT,
                                      classroom_id INTEGER NOT NULL,
                                      title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    category VARCHAR(20) CHECK(category IN ('Routine', 'Exam', 'CT', 'General')),
    is_pinned BOOLEAN DEFAULT 0,
    created_by INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (classroom_id) REFERENCES classroom(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
    );
-- Notification Settings Table
CREATE TABLE IF NOT EXISTS notification_settings (
                                                     id INTEGER PRIMARY KEY AUTOINCREMENT,
                                                     user_id INTEGER NOT NULL,
                                                     exam_notification_hours INTEGER DEFAULT 24,  -- Hours before exam to notify
                                                     ct_quiz_notification_hours INTEGER DEFAULT 24,
                                                     lab_test_notification_hours INTEGER DEFAULT 24,
                                                     routine_notification_minutes INTEGER DEFAULT 60,  -- Minutes before class
                                                     enable_exam_notifications BOOLEAN DEFAULT 1,
                                                     enable_routine_notifications BOOLEAN DEFAULT 1,
                                                     enable_notice_notifications BOOLEAN DEFAULT 1,
                                                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                     FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    );

-- Notifications History Table
CREATE TABLE IF NOT EXISTS notifications (
                                             id INTEGER PRIMARY KEY AUTOINCREMENT,
                                             user_id INTEGER NOT NULL,
                                             classroom_id INTEGER NOT NULL,
                                             type TEXT NOT NULL,  -- 'ROUTINE', 'EXAM', 'NOTICE', 'CT_QUIZ', 'LAB_TEST'
                                             title TEXT NOT NULL,
                                             message TEXT NOT NULL,
                                             reference_id INTEGER,  -- ID of exam/routine/notice etc.
                                             is_read BOOLEAN DEFAULT 0,
                                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                             FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (classroom_id) REFERENCES classroom(id) ON DELETE CASCADE
    );

-- User Role in Classroom (Updated to support CR role)
CREATE TABLE IF NOT EXISTS classroom_student_roles (
                                                       id INTEGER PRIMARY KEY AUTOINCREMENT,
                                                       classroom_id INTEGER NOT NULL,
                                                       user_id INTEGER NOT NULL,
                                                       role TEXT DEFAULT 'STUDENT',  -- 'STUDENT', 'CR' (Class Representative)
                                                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                       FOREIGN KEY (classroom_id) REFERENCES classroom(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE(classroom_id, user_id)
    );

-- User Profiles Table
CREATE TABLE IF NOT EXISTS user_profiles (
                                            user_id INTEGER PRIMARY KEY,
                                            full_name VARCHAR(100),
    phone_number VARCHAR(20),
    address TEXT,
    bio TEXT,
    avatar_url TEXT,
    department VARCHAR(100),
    student_id VARCHAR(50),
    roll_number VARCHAR(20) UNIQUE,
    designation VARCHAR(100),
    date_of_birth TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    );

-- Calendar Events Table (for time-slotted calendar)
CREATE TABLE IF NOT EXISTS calendar_events (
                                               id INTEGER PRIMARY KEY AUTOINCREMENT,
                                               classroom_id INTEGER NOT NULL,
                                               event_type VARCHAR(20) NOT NULL,  -- 'ROUTINE', 'EXAM', 'CT_QUIZ', 'LAB_TEST', 'CUSTOM'
    reference_id INTEGER,  -- Links to exam/routine/etc table
    title VARCHAR(200) NOT NULL,
    description TEXT,
    event_date DATE NOT NULL,
    start_time TIME,
    end_time TIME,
    location VARCHAR(100),
    color VARCHAR(10) DEFAULT '#f97316',  -- Orange by default
    created_by INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (classroom_id) REFERENCES classroom(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
    );

-- Personal Calendar Events (user-specific events)
CREATE TABLE IF NOT EXISTS personal_calendar_events (
                                                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                                                        user_id INTEGER NOT NULL,
                                                        title VARCHAR(200) NOT NULL,
    description TEXT,
    event_date DATE NOT NULL,
    start_time TIME,
    end_time TIME,
    location VARCHAR(100),
    color VARCHAR(10) DEFAULT '#9333ea',  -- Purple by default
    reminder_minutes INTEGER DEFAULT 30,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    );

-- Attendance tracking table
CREATE TABLE IF NOT EXISTS attendance (
                                         id INTEGER PRIMARY KEY AUTOINCREMENT,
                                         classroom_id INTEGER NOT NULL,
                                         student_id INTEGER NOT NULL,
                                         roll_number VARCHAR(20) NOT NULL,
    routine_id INTEGER,
    date DATE NOT NULL,
    status VARCHAR(10) CHECK(status IN ('PRESENT', 'ABSENT', 'LATE')) DEFAULT 'ABSENT',
    marked_by_user_id INTEGER NOT NULL,
    marked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (classroom_id) REFERENCES classroom(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (routine_id) REFERENCES routine(id) ON DELETE SET NULL,
    FOREIGN KEY (marked_by_user_id) REFERENCES users(id),
    UNIQUE(classroom_id, student_id, date, routine_id)
    );

-- Assessment results
CREATE TABLE IF NOT EXISTS exam_result (
                                           id INTEGER PRIMARY KEY AUTOINCREMENT,
                                           classroom_id INTEGER NOT NULL,
                                           exam_id INTEGER NOT NULL,
                                           student_id INTEGER,
                                           roll_number VARCHAR(20) NOT NULL,
    score REAL,
    total REAL,
    grade VARCHAR(10),
    remarks TEXT,
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (classroom_id) REFERENCES classroom(id) ON DELETE CASCADE,
    FOREIGN KEY (exam_id) REFERENCES exam(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE SET NULL,
    UNIQUE(classroom_id, exam_id, roll_number)
    );

CREATE TABLE IF NOT EXISTS ct_quiz_result (
                                              id INTEGER PRIMARY KEY AUTOINCREMENT,
                                              classroom_id INTEGER NOT NULL,
                                              ct_quiz_id INTEGER NOT NULL,
                                              student_id INTEGER,
                                              roll_number VARCHAR(20) NOT NULL,
    score REAL,
    total REAL,
    grade VARCHAR(10),
    remarks TEXT,
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (classroom_id) REFERENCES classroom(id) ON DELETE CASCADE,
    FOREIGN KEY (ct_quiz_id) REFERENCES ct_quiz(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE SET NULL,
    UNIQUE(classroom_id, ct_quiz_id, roll_number)
    );

CREATE TABLE IF NOT EXISTS lab_evaluation (
                                              id INTEGER PRIMARY KEY AUTOINCREMENT,
                                              classroom_id INTEGER NOT NULL,
                                              lab_test_id INTEGER NOT NULL,
                                              student_id INTEGER,
                                              roll_number VARCHAR(20) NOT NULL,
    score REAL,
    total REAL,
    grade VARCHAR(10),
    remarks TEXT,
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (classroom_id) REFERENCES classroom(id) ON DELETE CASCADE,
    FOREIGN KEY (lab_test_id) REFERENCES lab_test(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE SET NULL,
    UNIQUE(classroom_id, lab_test_id, roll_number)
    );

CREATE TABLE IF NOT EXISTS audit_log (
                                         id INTEGER PRIMARY KEY AUTOINCREMENT,
                                         user_id INTEGER NOT NULL,
    action VARCHAR(20) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id INTEGER,
    old_value TEXT,
    new_value TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_classroom_admin ON classroom(admin_id);
CREATE INDEX IF NOT EXISTS idx_classroom_rolls ON classroom_rolls(classroom_id);
CREATE INDEX IF NOT EXISTS idx_classroom_students ON classroom_students(classroom_id);
CREATE INDEX IF NOT EXISTS idx_routine_classroom ON routine(classroom_id);
CREATE INDEX IF NOT EXISTS idx_exam_classroom ON exam(classroom_id);
CREATE INDEX IF NOT EXISTS idx_user_profiles ON user_profiles(user_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_user_profiles_roll_number ON user_profiles(roll_number);
CREATE INDEX IF NOT EXISTS idx_calendar_events_classroom ON calendar_events(classroom_id, event_date);
CREATE INDEX IF NOT EXISTS idx_calendar_events_type ON calendar_events(event_type, reference_id);
CREATE INDEX IF NOT EXISTS idx_personal_calendar_user ON personal_calendar_events(user_id, event_date);
CREATE INDEX IF NOT EXISTS idx_attendance_classroom_date ON attendance(classroom_id, date);
CREATE INDEX IF NOT EXISTS idx_attendance_student ON attendance(student_id, classroom_id);
CREATE INDEX IF NOT EXISTS idx_exam_result_classroom ON exam_result(classroom_id, exam_id);
CREATE INDEX IF NOT EXISTS idx_ct_result_classroom ON ct_quiz_result(classroom_id, ct_quiz_id);
CREATE INDEX IF NOT EXISTS idx_lab_eval_classroom ON lab_evaluation(classroom_id, lab_test_id);
CREATE INDEX IF NOT EXISTS idx_audit_log_user ON audit_log(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_log_entity ON audit_log(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_log_timestamp ON audit_log(timestamp);