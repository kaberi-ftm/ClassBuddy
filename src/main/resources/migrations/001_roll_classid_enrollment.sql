-- basic schema upgrades for older DBs

-- user_profiles: roll number
ALTER TABLE user_profiles ADD COLUMN roll_number VARCHAR(20);
CREATE UNIQUE INDEX IF NOT EXISTS idx_user_profiles_roll_number ON user_profiles(roll_number);

-- classroom: class id + qr
ALTER TABLE classroom ADD COLUMN class_id VARCHAR(50);
CREATE UNIQUE INDEX IF NOT EXISTS idx_classroom_class_id ON classroom(class_id);
ALTER TABLE classroom ADD COLUMN qr_code_path VARCHAR(255);

-- classroom_students: enrollment status
ALTER TABLE classroom_students ADD COLUMN enrollment_status TEXT DEFAULT 'ACTIVE';
ALTER TABLE classroom_students ADD COLUMN enrolled_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- routine: multi-day support
ALTER TABLE routine ADD COLUMN applicable_days TEXT;

-- best-effort backfill for routine
UPDATE routine SET applicable_days = day WHERE applicable_days IS NULL;
