-- Curated multi-day workout plans per skill node + user enrollments.
-- Safe when Flyway is enabled later; with ddl-auto=update Hibernate also creates these.

CREATE TABLE IF NOT EXISTS workout_plan (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    node_id         UUID NOT NULL REFERENCES node (id),
    title           VARCHAR(160) NOT NULL,
    description     TEXT,
    duration_days   INT NOT NULL CHECK (duration_days >= 1),
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS workout_plan_day (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    plan_id     UUID NOT NULL REFERENCES workout_plan (id) ON DELETE CASCADE,
    day_number  INT NOT NULL CHECK (day_number >= 1),
    workout_id  UUID NOT NULL REFERENCES workout (id),
    CONSTRAINT uq_plan_day_number UNIQUE (plan_id, day_number)
);

CREATE TABLE IF NOT EXISTS user_plan_enrollment (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    plan_id         UUID NOT NULL REFERENCES workout_plan (id),
    node_id         UUID NOT NULL REFERENCES node (id),
    current_day     INT NOT NULL DEFAULT 1 CHECK (current_day >= 1),
    status          VARCHAR(20) NOT NULL,
    started_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    completed_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

ALTER TABLE workout_session
    ADD COLUMN IF NOT EXISTS plan_enrollment_id UUID REFERENCES user_plan_enrollment (id),
    ADD COLUMN IF NOT EXISTS plan_day_number INT;
