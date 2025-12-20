-- Create Users Table
CREATE TABLE IF NOT EXISTS users (
                                     id INTEGER PRIMARY KEY AUTOINCREMENT,
                                     username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK(role IN ('ADMIN', 'STUDENT')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Create Classroom Table
CREATE TABLE IF NOT EXISTS classroom (
                                         id INTEGER PRIMARY KEY AUTOINCREMENT,
                                         admin_id INTEGER NOT NULL,
                                         name VARCHAR(100) NOT NULL,
    section VARCHAR(50),
    department VARCHAR(50),
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (admin_id) REFERENCES users(id) ON DELETE CASCADE
    );

-- Allowed Roll Numbers in Classroom (Admin specifies which rolls can join)
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

-- Students joined to Classroom (tracks which student joined which classroom)
CREATE TABLE IF NOT EXISTS classroom_students (
                                                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                                                  classroom_id INTEGER NOT NULL,
                                                  student_id INTEGER NOT NULL,
                                                  roll_number VARCHAR(20) NOT NULL,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(classroom_id, student_id),
    FOREIGN KEY (classroom_id) REFERENCES classroom(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE
    );

-- Class Routine
CREATE TABLE IF NOT EXISTS routine (
                                       id INTEGER PRIMARY KEY AUTOINCREMENT,
                                       classroom_id INTEGER NOT NULL,
                                       day VARCHAR(15) NOT NULL,
    period_number INTEGER NOT NULL,
    course_name VARCHAR(100) NOT NULL,
    teacher_name VARCHAR(100),
    room VARCHAR(50),
    time_start TIME NOT NULL,
    time_end TIME NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (classroom_id) REFERENCES classroom(id) ON DELETE CASCADE
    );

-- Exams
CREATE TABLE IF NOT EXISTS exam (
                                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                                    classroom_id INTEGER NOT NULL,
                                    course_name VARCHAR(100) NOT NULL,
    exam_type VARCHAR(20) CHECK(exam_type IN ('Mid', 'Final', 'Viva')),
    exam_date DATE NOT NULL,
    exam_time TIME NOT NULL,
    room VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (classroom_id) REFERENCES classroom(id) ON DELETE CASCADE
    );

-- CT/Quiz
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

-- Lab Tests
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

-- Notices
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

-- Indexes for Performance
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_classroom_admin ON classroom(admin_id);
CREATE INDEX IF NOT EXISTS idx_classroom_rolls ON classroom_rolls(classroom_id);
CREATE INDEX IF NOT EXISTS idx_classroom_students ON classroom_students(classroom_id);
CREATE INDEX IF NOT EXISTS idx_routine_classroom ON routine(classroom_id);
CREATE INDEX IF NOT EXISTS idx_exam_classroom ON exam(classroom_id);