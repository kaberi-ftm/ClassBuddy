-- Expand exam.exam_type CHECK constraint to support CT/Quiz/Lab Test.
-- SQLite requires table rebuild to change a CHECK constraint.

PRAGMA foreign_keys=OFF;

CREATE TABLE IF NOT EXISTS exam_new (
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

INSERT INTO exam_new (id, classroom_id, course_name, exam_type, exam_date, exam_time, room, created_at)
SELECT id, classroom_id, course_name, exam_type, exam_date, exam_time, room, created_at
FROM exam;

DROP TABLE exam;
ALTER TABLE exam_new RENAME TO exam;

CREATE INDEX IF NOT EXISTS idx_exam_classroom ON exam(classroom_id);

PRAGMA foreign_keys=ON;
