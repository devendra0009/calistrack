-- Discriminate skill workouts vs daily routines (e.g. morning stretch).

ALTER TABLE workout
    ADD COLUMN IF NOT EXISTS kind VARCHAR(20) NOT NULL DEFAULT 'SKILL';

ALTER TABLE workout_plan
    ADD COLUMN IF NOT EXISTS kind VARCHAR(20) NOT NULL DEFAULT 'SKILL';

ALTER TABLE workout_plan
    ADD COLUMN IF NOT EXISTS code VARCHAR(64);

CREATE UNIQUE INDEX IF NOT EXISTS uq_workout_plan_code
    ON workout_plan (code)
    WHERE code IS NOT NULL;
