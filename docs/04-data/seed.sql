-- CaliTrack docs reference schema + Muscle-Up path seed
-- For documentation / local experiments. App migrations will use Flyway.
-- Requires PostgreSQL 13+ (gen_random_uuid).

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ---------------------------------------------------------------------------
-- Tables
-- ---------------------------------------------------------------------------

CREATE TABLE app_user (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    display_name            VARCHAR(100) NOT NULL,
    height_cm               NUMERIC(5, 2),
    weight_kg               NUMERIC(5, 2),
    age                     INT CHECK (age IS NULL OR age > 0),
    gender                  VARCHAR(20) CHECK (gender IS NULL OR gender IN ('MALE', 'FEMALE', 'OTHER', 'UNSPECIFIED')),
    experience              VARCHAR(20) CHECK (experience IS NULL OR experience IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED')),
    current_goal_node_id    UUID,
    role                    VARCHAR(20) NOT NULL DEFAULT 'USER' CHECK (role IN ('USER', 'ADMIN')),
    avatar_url              TEXT,
    deleted_at              TIMESTAMPTZ,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE user_auth_identity (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    provider            VARCHAR(20) NOT NULL CHECK (provider IN ('LOCAL', 'FIREBASE')),
    email               VARCHAR(255) NOT NULL,
    password_hash       TEXT,
    provider_subject    VARCHAR(255),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_auth_provider_email UNIQUE (provider, email),
    CONSTRAINT chk_local_password CHECK (
        provider <> 'LOCAL' OR password_hash IS NOT NULL
    )
);

CREATE TABLE refresh_token (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    token_hash          VARCHAR(64) NOT NULL UNIQUE,
    expires_at          TIMESTAMPTZ NOT NULL,
    revoked_at          TIMESTAMPTZ,
    replaced_by_id      UUID REFERENCES refresh_token (id),
    user_agent          VARCHAR(512),
    ip_address          VARCHAR(45),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE exercise (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(120) NOT NULL UNIQUE,
    description     TEXT,
    category        VARCHAR(20) NOT NULL CHECK (category IN ('PULL', 'PUSH', 'CORE', 'BALANCE', 'STATIC', 'MOBILITY', 'LEGS')),
    metric_type     VARCHAR(20) NOT NULL CHECK (metric_type IN ('TIME', 'REPS', 'DISTANCE', 'ANGLE', 'WEIGHT')),
    difficulty      VARCHAR(20) NOT NULL CHECK (difficulty IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'ELITE')),
    thumbnail_url   TEXT,
    demo_video_url  TEXT,
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'COMING_SOON', 'DEPRECATED')),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE node (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                VARCHAR(120) NOT NULL UNIQUE,
    description         TEXT,
    node_type           VARCHAR(20) NOT NULL CHECK (node_type IN ('MILESTONE', 'SKILL', 'HOLD', 'MOBILITY')),
    exercise_id         UUID NOT NULL REFERENCES exercise (id),
    target_value        NUMERIC(10, 2) NOT NULL,
    operator            VARCHAR(5) NOT NULL CHECK (operator IN ('>=', '<=', '==', '<', '>')),
    unit_label          VARCHAR(20) NOT NULL,
    difficulty          VARCHAR(20) NOT NULL CHECK (difficulty IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'ELITE')),
    xp_reward           INT,
    estimated_minutes   INT,
    status              VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'COMING_SOON', 'DEPRECATED')),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

ALTER TABLE app_user
    ADD CONSTRAINT fk_user_goal_node
    FOREIGN KEY (current_goal_node_id) REFERENCES node (id);

CREATE TABLE node_edge (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    from_node_id    UUID NOT NULL REFERENCES node (id),
    to_node_id      UUID NOT NULL REFERENCES node (id),
    relation_type   VARCHAR(20) NOT NULL DEFAULT 'PREREQUISITE' CHECK (relation_type IN ('PREREQUISITE')),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_node_edge UNIQUE (from_node_id, to_node_id),
    CONSTRAINT chk_node_edge_not_self CHECK (from_node_id <> to_node_id)
);

-- Placement questionnaire prompts keyed by current goal node
-- (path order itself is derived by walking node_edge back from the goal)
CREATE TABLE path_question (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    goal_node_id    UUID NOT NULL REFERENCES node (id),
    node_id         UUID NOT NULL REFERENCES node (id),
    prompt          TEXT NOT NULL,
    answer_type     VARCHAR(20) NOT NULL CHECK (answer_type IN ('REPS', 'YES_NO')),
    sort_order      INT NOT NULL CHECK (sort_order >= 1),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_path_question_goal_sort UNIQUE (goal_node_id, sort_order),
    CONSTRAINT uq_path_question_goal_node UNIQUE (goal_node_id, node_id)
);

CREATE TABLE workout (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title               VARCHAR(160) NOT NULL,
    description         TEXT,
    goal_node_id        UUID NOT NULL REFERENCES node (id),
    kind                VARCHAR(20) NOT NULL DEFAULT 'SKILL'
                            CHECK (kind IN ('SKILL', 'STRETCH')),
    difficulty          VARCHAR(20) NOT NULL CHECK (difficulty IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'ELITE')),
    created_by_user_id  UUID REFERENCES app_user (id),
    status              VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'COMING_SOON', 'DEPRECATED')),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE workout_exercise (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workout_id              UUID NOT NULL REFERENCES workout (id) ON DELETE CASCADE,
    exercise_id             UUID NOT NULL REFERENCES exercise (id),
    sequence                INT NOT NULL CHECK (sequence >= 1),
    target_sets             INT,
    target_reps             INT,
    target_hold_seconds     INT,
    target_rest_seconds     INT,
    notes                   TEXT,
    demo_video_url          TEXT,
    CONSTRAINT uq_workout_sequence UNIQUE (workout_id, sequence)
);

CREATE TABLE workout_plan (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    node_id         UUID NOT NULL REFERENCES node (id),
    title           VARCHAR(160) NOT NULL,
    description     TEXT,
    kind            VARCHAR(20) NOT NULL DEFAULT 'SKILL'
                        CHECK (kind IN ('SKILL', 'DAILY_ROUTINE')),
    code            VARCHAR(64),
    duration_days   INT NOT NULL CHECK (duration_days >= 1),
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'DEPRECATED')),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_workout_plan_code UNIQUE (code)
);

CREATE TABLE workout_plan_day (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    plan_id     UUID NOT NULL REFERENCES workout_plan (id) ON DELETE CASCADE,
    day_number  INT NOT NULL CHECK (day_number >= 1),
    workout_id  UUID NOT NULL REFERENCES workout (id),
    CONSTRAINT uq_plan_day_number UNIQUE (plan_id, day_number)
);

CREATE TABLE user_node (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                 UUID NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    node_id                 UUID NOT NULL REFERENCES node (id),
    status                  VARCHAR(20) NOT NULL CHECK (status IN ('LOCKED', 'AVAILABLE', 'IN_PROGRESS', 'COMPLETED')),
    progress_percentage     NUMERIC(5, 2) NOT NULL DEFAULT 0 CHECK (progress_percentage >= 0 AND progress_percentage <= 100),
    verified                BOOLEAN NOT NULL DEFAULT FALSE,
    verified_by_ai          BOOLEAN NOT NULL DEFAULT FALSE,
    last_attempt_at         TIMESTAMPTZ,
    best_score              NUMERIC(5, 2),
    current_score           NUMERIC(5, 2),
    unlocked_at             TIMESTAMPTZ,
    mastery                 VARCHAR(20) CHECK (mastery IS NULL OR mastery IN ('BRONZE', 'SILVER', 'GOLD', 'PLATINUM')),
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_user_node UNIQUE (user_id, node_id)
);

CREATE TABLE user_plan_enrollment (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    plan_id         UUID NOT NULL REFERENCES workout_plan (id),
    node_id         UUID NOT NULL REFERENCES node (id),
    current_day     INT NOT NULL DEFAULT 1 CHECK (current_day >= 1),
    status          VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'AWAITING_VERIFY', 'COMPLETED')),
    started_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    completed_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE workout_session (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    workout_id          UUID NOT NULL REFERENCES workout (id),
    plan_enrollment_id  UUID REFERENCES user_plan_enrollment (id),
    plan_day_number     INT CHECK (plan_day_number IS NULL OR plan_day_number >= 1),
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                            CHECK (status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'ABANDONED')),
    verified            BOOLEAN NOT NULL DEFAULT FALSE,
    started_at          TIMESTAMPTZ,
    completed_at        TIMESTAMPTZ,
    duration_seconds    INT,
    calories            INT,
    ai_score            NUMERIC(5, 2),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE assessment (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                 UUID NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    node_id                 UUID NOT NULL REFERENCES node (id),
    workout_session_id      UUID REFERENCES workout_session (id),
    status                  VARCHAR(20) NOT NULL CHECK (status IN (
                                'NOT_ATTEMPTED', 'PENDING_REVIEW', 'PENDING_AI', 'PASSED', 'FAILED'
                            )),
    video_url               TEXT,
    attempt_score           NUMERIC(5, 2),
    ai_form_score           NUMERIC(5, 2),
    verified                BOOLEAN NOT NULL DEFAULT FALSE,
    remarks                 TEXT,
    performed_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE exercise_attempt (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workout_session_id      UUID NOT NULL REFERENCES workout_session (id) ON DELETE CASCADE,
    workout_exercise_id     UUID NOT NULL REFERENCES workout_exercise (id),
    actual_sets             INT,
    actual_reps             INT,
    actual_hold_seconds     INT,
    actual_rest_seconds     INT,
    video_url               TEXT,
    notes                   TEXT,
    ai_score                NUMERIC(5, 2),
    status                  VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS'
                                CHECK (status IN ('IN_PROGRESS', 'COMPLETED', 'SKIPPED')),
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_session_workout_exercise UNIQUE (workout_session_id, workout_exercise_id)
);

-- ---------------------------------------------------------------------------
-- Seed IDs (fixed for readable docs / demos)
-- ---------------------------------------------------------------------------

-- Exercises
-- e1 Pull-up, e2 Dead Hang, e3 Rows, e4 Scapular Pull, e5 Band Assisted Pull-up
-- e6 Australian Pull-up, e7 Negative Pull-up, e8 Chest To Bar, e9 Explosive Pull-up, e10 Muscle Up, e11 Dip

INSERT INTO exercise (id, name, description, category, metric_type, difficulty) VALUES
('11111111-1111-1111-1111-111111110001', 'Australian Pull-up', 'Horizontal row-style pull-up', 'PULL', 'REPS', 'BEGINNER'),
('11111111-1111-1111-1111-111111110002', 'Band Assisted Pull-up', 'Pull-up with band assistance', 'PULL', 'REPS', 'BEGINNER'),
('11111111-1111-1111-1111-111111110003', 'Negative Pull-up', 'Slow eccentric pull-up', 'PULL', 'REPS', 'BEGINNER'),
('11111111-1111-1111-1111-111111110004', 'Pull-up', 'Standard dead-hang pull-up', 'PULL', 'REPS', 'INTERMEDIATE'),
('11111111-1111-1111-1111-111111110005', 'Chest To Bar', 'Pull-up until chest reaches bar', 'PULL', 'REPS', 'ADVANCED'),
('11111111-1111-1111-1111-111111110006', 'Explosive Pull-up', 'Pull-up with high intent / hip drive', 'PULL', 'REPS', 'ADVANCED'),
('11111111-1111-1111-1111-111111110007', 'Muscle Up', 'Bar muscle-up', 'PULL', 'REPS', 'ELITE'),
('11111111-1111-1111-1111-111111110008', 'Dead Hang', 'Passive hang on bar', 'PULL', 'TIME', 'BEGINNER'),
('11111111-1111-1111-1111-111111110009', 'Rows', 'Bodyweight rows', 'PULL', 'REPS', 'BEGINNER'),
('11111111-1111-1111-1111-111111110010', 'Scapular Pull', 'Scapular depression/retraction on bar', 'PULL', 'REPS', 'BEGINNER'),
('11111111-1111-1111-1111-111111110011', 'Dip', 'Parallel bar dip', 'PUSH', 'REPS', 'INTERMEDIATE');

-- Nodes (Muscle-Up path) — category comes from exercise via exercise_id
INSERT INTO node (id, name, description, node_type, exercise_id, target_value, operator, unit_label, difficulty, estimated_minutes) VALUES
('22222222-2222-2222-2222-222222220001', 'Australian Pull-up', 'Build horizontal pulling strength', 'MILESTONE',
 '11111111-1111-1111-1111-111111110001', 10, '>=', 'REPS', 'BEGINNER', 20),
('22222222-2222-2222-2222-222222220002', 'Band Assisted Pull-up', 'Vertical pull with assistance', 'MILESTONE',
 '11111111-1111-1111-1111-111111110002', 8, '>=', 'REPS', 'BEGINNER', 20),
('22222222-2222-2222-2222-222222220003', 'Negative Pull-up', 'Control the eccentric', 'MILESTONE',
 '11111111-1111-1111-1111-111111110003', 5, '>=', 'REPS', 'BEGINNER', 20),
('22222222-2222-2222-2222-222222220004', 'First Pull-up', 'One strict pull-up', 'MILESTONE',
 '11111111-1111-1111-1111-111111110004', 1, '>=', 'REP', 'BEGINNER', 25),
('22222222-2222-2222-2222-222222220005', '5 Pull-ups', 'Five strict pull-ups', 'MILESTONE',
 '11111111-1111-1111-1111-111111110004', 5, '>=', 'REPS', 'BEGINNER', 25),
('22222222-2222-2222-2222-222222220006', '10 Pull-ups', 'Ten strict pull-ups', 'MILESTONE',
 '11111111-1111-1111-1111-111111110004', 10, '>=', 'REPS', 'INTERMEDIATE', 30),
('22222222-2222-2222-2222-222222220007', 'Chest To Bar', 'Five chest-to-bar pull-ups', 'SKILL',
 '11111111-1111-1111-1111-111111110005', 5, '>=', 'REPS', 'ADVANCED', 30),
('22222222-2222-2222-2222-222222220008', 'Explosive Pull-up', 'Five explosive pull-ups', 'MILESTONE',
 '11111111-1111-1111-1111-111111110006', 5, '>=', 'REPS', 'ADVANCED', 30),
('22222222-2222-2222-2222-222222220009', '15 Dips', 'Fifteen dips for push strength into MU', 'MILESTONE',
 '11111111-1111-1111-1111-111111110011', 15, '>=', 'REPS', 'INTERMEDIATE', 25),
('22222222-2222-2222-2222-222222220010', 'Muscle Up', 'One bar muscle-up', 'SKILL',
 '11111111-1111-1111-1111-111111110007', 1, '>=', 'REP', 'ELITE', 40);

-- Edges: from_node = prerequisite, to_node = next
-- Linear path + CTB needs Explosive + MU needs CTB and 15 Dips
INSERT INTO node_edge (from_node_id, to_node_id) VALUES
('22222222-2222-2222-2222-222222220001', '22222222-2222-2222-2222-222222220002'), -- Australian → Band Assisted
('22222222-2222-2222-2222-222222220002', '22222222-2222-2222-2222-222222220003'), -- Band → Negative
('22222222-2222-2222-2222-222222220003', '22222222-2222-2222-2222-222222220004'), -- Negative → First PU
('22222222-2222-2222-2222-222222220004', '22222222-2222-2222-2222-222222220005'), -- First → 5
('22222222-2222-2222-2222-222222220005', '22222222-2222-2222-2222-222222220006'), -- 5 → 10
('22222222-2222-2222-2222-222222220006', '22222222-2222-2222-2222-222222220007'), -- 10 → CTB
('22222222-2222-2222-2222-222222220006', '22222222-2222-2222-2222-222222220008'), -- 10 → Explosive
('22222222-2222-2222-2222-222222220007', '22222222-2222-2222-2222-222222220010'), -- CTB → Muscle Up
('22222222-2222-2222-2222-222222220008', '22222222-2222-2222-2222-222222220007'), -- Explosive → CTB
('22222222-2222-2222-2222-222222220009', '22222222-2222-2222-2222-222222220010'); -- 15 Dips → Muscle Up

-- Placement questions for Muscle Up goal (path order comes from node_edge walk)
INSERT INTO path_question (goal_node_id, node_id, prompt, answer_type, sort_order) VALUES
('22222222-2222-2222-2222-222222220010', '22222222-2222-2222-2222-222222220001',
 'How many Australian Pull-ups can you do?', 'REPS', 1),
('22222222-2222-2222-2222-222222220010', '22222222-2222-2222-2222-222222220005',
 'Can you do 5 Pull-ups?', 'YES_NO', 2),
('22222222-2222-2222-2222-222222220010', '22222222-2222-2222-2222-222222220006',
 'Can you do 10 Pull-ups?', 'YES_NO', 3);

-- Workouts
INSERT INTO workout (id, title, description, goal_node_id, difficulty) VALUES
('33333333-3333-3333-3333-333333330001', 'Pull Strength Beginner', 'Build toward 10 pull-ups',
 '22222222-2222-2222-2222-222222220006', 'BEGINNER'),
('33333333-3333-3333-3333-333333330002', 'Chest To Bar Prep', 'Prep for chest-to-bar',
 '22222222-2222-2222-2222-222222220007', 'ADVANCED'),
('33333333-3333-3333-3333-333333330003', 'Muscle Up Transition', 'Prep for first muscle-up',
 '22222222-2222-2222-2222-222222220010', 'ELITE');

INSERT INTO workout_exercise (workout_id, exercise_id, sequence, target_sets, target_reps, target_hold_seconds, target_rest_seconds, notes) VALUES
-- Pull Strength Beginner
('33333333-3333-3333-3333-333333330001', '11111111-1111-1111-1111-111111110010', 1, 3, 10, NULL, 60, 'Warmup scap pulls'),
('33333333-3333-3333-3333-333333330001', '11111111-1111-1111-1111-111111110004', 2, 4, 5, NULL, 90, 'Pull-ups'),
('33333333-3333-3333-3333-333333330001', '11111111-1111-1111-1111-111111110009', 3, 4, 12, NULL, 60, 'Rows'),
('33333333-3333-3333-3333-333333330001', '11111111-1111-1111-1111-111111110008', 4, 3, NULL, 45, 60, 'Dead hang'),
-- Chest To Bar Prep
('33333333-3333-3333-3333-333333330002', '11111111-1111-1111-1111-111111110004', 1, 4, 8, NULL, 90, 'Pull-ups'),
('33333333-3333-3333-3333-333333330002', '11111111-1111-1111-1111-111111110008', 2, 3, NULL, 45, 60, 'Dead hang'),
('33333333-3333-3333-3333-333333330002', '11111111-1111-1111-1111-111111110009', 3, 4, 12, NULL, 60, 'Rows'),
-- Muscle Up Transition
('33333333-3333-3333-3333-333333330003', '11111111-1111-1111-1111-111111110006', 1, 4, 5, NULL, 120, 'Explosive pull-ups'),
('33333333-3333-3333-3333-333333330003', '11111111-1111-1111-1111-111111110011', 2, 4, 10, NULL, 90, 'Dips'),
('33333333-3333-3333-3333-333333330003', '11111111-1111-1111-1111-111111110008', 3, 3, NULL, 40, 60, 'Dead hang');

-- Curated multi-day plans (one ACTIVE plan per node; days reuse catalog workouts)
INSERT INTO workout_plan (id, node_id, title, description, duration_days, status) VALUES
('55555555-5555-5555-5555-555555550001', '22222222-2222-2222-2222-222222220006',
 '10 Pull-Ups — 7 Day Build', 'Practice toward 10 pull-ups', 7, 'ACTIVE'),
('55555555-5555-5555-5555-555555550002', '22222222-2222-2222-2222-222222220007',
 'Chest To Bar — 7 Day Prep', 'Prep for chest-to-bar pull-ups', 7, 'ACTIVE'),
('55555555-5555-5555-5555-555555550003', '22222222-2222-2222-2222-222222220010',
 'Muscle Up — 7 Day Transition', 'Prep for first muscle-up', 7, 'ACTIVE');

INSERT INTO workout_plan_day (id, plan_id, day_number, workout_id) VALUES
-- 10 Pull-Ups plan (repeat beginner workout across 7 days)
('66666666-6666-6666-6666-666666660001', '55555555-5555-5555-5555-555555550001', 1, '33333333-3333-3333-3333-333333330001'),
('66666666-6666-6666-6666-666666660002', '55555555-5555-5555-5555-555555550001', 2, '33333333-3333-3333-3333-333333330001'),
('66666666-6666-6666-6666-666666660003', '55555555-5555-5555-5555-555555550001', 3, '33333333-3333-3333-3333-333333330001'),
('66666666-6666-6666-6666-666666660004', '55555555-5555-5555-5555-555555550001', 4, '33333333-3333-3333-3333-333333330001'),
('66666666-6666-6666-6666-666666660005', '55555555-5555-5555-5555-555555550001', 5, '33333333-3333-3333-3333-333333330001'),
('66666666-6666-6666-6666-666666660006', '55555555-5555-5555-5555-555555550001', 6, '33333333-3333-3333-3333-333333330001'),
('66666666-6666-6666-6666-666666660007', '55555555-5555-5555-5555-555555550001', 7, '33333333-3333-3333-3333-333333330001'),
-- Chest To Bar
('66666666-6666-6666-6666-666666660011', '55555555-5555-5555-5555-555555550002', 1, '33333333-3333-3333-3333-333333330002'),
('66666666-6666-6666-6666-666666660012', '55555555-5555-5555-5555-555555550002', 2, '33333333-3333-3333-3333-333333330002'),
('66666666-6666-6666-6666-666666660013', '55555555-5555-5555-5555-555555550002', 3, '33333333-3333-3333-3333-333333330002'),
('66666666-6666-6666-6666-666666660014', '55555555-5555-5555-5555-555555550002', 4, '33333333-3333-3333-3333-333333330002'),
('66666666-6666-6666-6666-666666660015', '55555555-5555-5555-5555-555555550002', 5, '33333333-3333-3333-3333-333333330002'),
('66666666-6666-6666-6666-666666660016', '55555555-5555-5555-5555-555555550002', 6, '33333333-3333-3333-3333-333333330002'),
('66666666-6666-6666-6666-666666660017', '55555555-5555-5555-5555-555555550002', 7, '33333333-3333-3333-3333-333333330002'),
-- Muscle Up
('66666666-6666-6666-6666-666666660021', '55555555-5555-5555-5555-555555550003', 1, '33333333-3333-3333-3333-333333330003'),
('66666666-6666-6666-6666-666666660022', '55555555-5555-5555-5555-555555550003', 2, '33333333-3333-3333-3333-333333330003'),
('66666666-6666-6666-6666-666666660023', '55555555-5555-5555-5555-555555550003', 3, '33333333-3333-3333-3333-333333330003'),
('66666666-6666-6666-6666-666666660024', '55555555-5555-5555-5555-555555550003', 4, '33333333-3333-3333-3333-333333330003'),
('66666666-6666-6666-6666-666666660025', '55555555-5555-5555-5555-555555550003', 5, '33333333-3333-3333-3333-333333330003'),
('66666666-6666-6666-6666-666666660026', '55555555-5555-5555-5555-555555550003', 6, '33333333-3333-3333-3333-333333330003'),
('66666666-6666-6666-6666-666666660027', '55555555-5555-5555-5555-555555550003', 7, '33333333-3333-3333-3333-333333330003');

-- Optional demo admin user (password hash is placeholder — replace in real app)
INSERT INTO app_user (id, display_name, role, current_goal_node_id) VALUES
('44444444-4444-4444-4444-444444440001', 'Admin', 'ADMIN', '22222222-2222-2222-2222-222222220010');

INSERT INTO user_auth_identity (user_id, provider, email, password_hash) VALUES
('44444444-4444-4444-4444-444444440001', 'LOCAL', 'admin@calistrack.local', '$2a$10$replace_me_with_real_bcrypt_hash');
