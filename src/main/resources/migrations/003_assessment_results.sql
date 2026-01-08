-- Assessment results tables

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

CREATE INDEX IF NOT EXISTS idx_exam_result_classroom ON exam_result(classroom_id, exam_id);
CREATE INDEX IF NOT EXISTS idx_ct_result_classroom ON ct_quiz_result(classroom_id, ct_quiz_id);
CREATE INDEX IF NOT EXISTS idx_lab_eval_classroom ON lab_evaluation(classroom_id, lab_test_id);
