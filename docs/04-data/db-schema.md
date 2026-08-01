# DB Schema (PostgreSQL)

Naming: snake_case tables. Timestamps: `timestamptz`. Money/XP optional fields nullable for V1.

Canonical CREATE + seed: [`seed.sql`](seed.sql).

---

## app_user

| Column | Type | Constraints | Notes |
| --- | --- | --- | --- |
| id | uuid | PK, default gen_random_uuid() | |
| display_name | varchar(100) | NOT NULL | |
| height_cm | numeric(5,2) | NULL | stored metric; UI may enter ft/in |
| weight_kg | numeric(5,2) | NULL | stored metric; UI may enter lb |
| age | int | NULL, CHECK > 0 | legacy; prefer date_of_birth |
| date_of_birth | date | NULL | source of truth for age (computed in API/UI) |
| gender | varchar(20) | NULL, CHECK IN (…) | see enums |
| experience | varchar(20) | NULL, CHECK IN (…) | |
| current_goal_node_id | uuid | FK → node(id), NULL | set after onboarding |
| role | varchar(20) | NOT NULL, DEFAULT `USER`, CHECK IN (`USER`,`ADMIN`) | |
| avatar_url | text | NULL | Cloudinary later |
| deleted_at | timestamptz | NULL | soft delete; NULL = active |
| created_at | timestamptz | NOT NULL, default now() | |
| updated_at | timestamptz | NOT NULL, default now() | |

---

## user_auth_identity

| Column | Type | Constraints | Notes |
| --- | --- | --- | --- |
| id | uuid | PK | |
| user_id | uuid | FK → app_user(id) ON DELETE CASCADE, NOT NULL | |
| provider | varchar(20) | NOT NULL, CHECK (`LOCAL`, …) | V1: LOCAL only |
| email | varchar(255) | NOT NULL | |
| password_hash | text | NULL | required when provider=LOCAL |
| provider_subject | varchar(255) | NULL | for OAuth later |
| created_at | timestamptz | NOT NULL, default now() | |
| updated_at | timestamptz | NOT NULL, default now() | |

**Unique:** `(provider, email)`  
**Unique:** `(provider, provider_subject)` WHERE provider_subject IS NOT NULL (optional partial unique in Flyway)

Access JWTs are **not** stored here (see `refresh_token` + [ADR-011](../06-decisions/ADR-011-jwt-refresh-tokens.md)).

---

## refresh_token

| Column | Type | Constraints | Notes |
| --- | --- | --- | --- |
| id | uuid | PK | |
| user_id | uuid | FK → app_user(id) ON DELETE CASCADE, NOT NULL | |
| token_hash | varchar(64) | NOT NULL, UNIQUE | SHA-256 hex of raw refresh token |
| expires_at | timestamptz | NOT NULL | |
| revoked_at | timestamptz | NULL | NULL = active |
| replaced_by_id | uuid | FK → refresh_token(id), NULL | set on rotation |
| user_agent | varchar(512) | NULL | optional device hint |
| ip_address | varchar(45) | NULL | optional |
| created_at | timestamptz | NOT NULL, default now() | |

**Active token:** `revoked_at IS NULL` AND `expires_at > now()`.

**Never store** the raw refresh token or access JWT in this table.

---

## exercise

| Column | Type | Constraints | Notes |
| --- | --- | --- | --- |
| id | uuid | PK | |
| name | varchar(120) | NOT NULL, UNIQUE | |
| description | text | NULL | |
| category | varchar(20) | NOT NULL, CHECK | PULL, PUSH, … |
| metric_type | varchar(20) | NOT NULL, CHECK | REPS, TIME, … |
| difficulty | varchar(20) | NOT NULL, CHECK | |
| thumbnail_url | text | NULL | later |
| demo_video_url | text | NULL | later — how to perform |
| status | varchar(20) | NOT NULL, DEFAULT `ACTIVE`, CHECK | |
| created_at | timestamptz | NOT NULL, default now() | |
| updated_at | timestamptz | NOT NULL, default now() | |

---

## node

| Column | Type | Constraints | Notes |
| --- | --- | --- | --- |
| id | uuid | PK | |
| name | varchar(120) | NOT NULL, UNIQUE | |
| description | text | NULL | |
| node_type | varchar(20) | NOT NULL, CHECK | MILESTONE, SKILL, HOLD, MOBILITY |
| exercise_id | uuid | FK → exercise(id), NOT NULL | |
| target_value | numeric(10,2) | NOT NULL | e.g. 10 |
| operator | varchar(5) | NOT NULL, CHECK | >=, <=, ==, <, > |
| unit_label | varchar(20) | NOT NULL | REPS, SEC, … |
| difficulty | varchar(20) | NOT NULL, CHECK | can differ from exercise (e.g. First vs 10 Pull-ups) |
| xp_reward | int | NULL | later |
| estimated_minutes | int | NULL | |
| status | varchar(20) | NOT NULL, DEFAULT `ACTIVE`, CHECK | |
| created_at | timestamptz | NOT NULL, default now() | |
| updated_at | timestamptz | NOT NULL, default now() | |

**No `category` on node** — use `exercise.category` via `exercise_id` (avoids duplicate / drift).

---

## node_edge

| Column | Type | Constraints | Notes |
| --- | --- | --- | --- |
| id | uuid | PK | |
| from_node_id | uuid | FK → node(id), NOT NULL | **prerequisite** |
| to_node_id | uuid | FK → node(id), NOT NULL | **next skill** |
| relation_type | varchar(20) | NOT NULL, DEFAULT `PREREQUISITE`, CHECK | |
| created_at | timestamptz | NOT NULL, default now() | |

**Unique:** `(from_node_id, to_node_id)`  
**Check:** `from_node_id <> to_node_id`

Example: Australian Pull-up → Band Assisted Pull-up → … → Muscle-Up.

Onboarding **path order** is derived at runtime by walking this graph backward from the goal node (ancestor closure + topological sort). No separate path-order table.

---

## path_question

Placement questionnaire rows keyed by `current_goal_node` / goal node.

| Column | Type | Constraints | Notes |
| --- | --- | --- | --- |
| id | uuid | PK | |
| goal_node_id | uuid | FK → node(id), NOT NULL | which goal these questions belong to |
| node_id | uuid | FK → node(id), NOT NULL | skill the answer places against |
| prompt | text | NOT NULL | UI copy |
| answer_type | varchar(20) | NOT NULL, CHECK | `REPS`, `YES_NO` |
| sort_order | int | NOT NULL, CHECK >= 1 | question order |
| created_at | timestamptz | NOT NULL, default now() | |

**Unique:** `(goal_node_id, sort_order)`, `(goal_node_id, node_id)`

Answers are one-shot V1 (not stored); they only drive `user_node` + first `workout_session`.

---

## workout

| Column | Type | Constraints | Notes |
| --- | --- | --- | --- |
| id | uuid | PK | |
| title | varchar(160) | NOT NULL | |
| description | text | NULL | |
| goal_node_id | uuid | FK → node(id), NOT NULL | skill this workout trains |
| difficulty | varchar(20) | NOT NULL, CHECK | |
| kind | varchar(20) | NOT NULL, DEFAULT `SKILL` | `SKILL` or `STRETCH` |
| created_by_user_id | uuid | FK → app_user(id), NULL | admin |
| status | varchar(20) | NOT NULL, DEFAULT `ACTIVE`, CHECK | |
| created_at | timestamptz | NOT NULL, default now() | |
| updated_at | timestamptz | NOT NULL, default now() | |

---

## workout_exercise

| Column | Type | Constraints | Notes |
| --- | --- | --- | --- |
| id | uuid | PK | |
| workout_id | uuid | FK → workout(id) ON DELETE CASCADE, NOT NULL | |
| exercise_id | uuid | FK → exercise(id), NOT NULL | |
| sequence | int | NOT NULL, CHECK >= 1 | order in workout |
| target_sets | int | NULL | |
| target_reps | int | NULL | |
| target_hold_seconds | int | NULL | |
| target_rest_seconds | int | NULL | |
| notes | text | NULL | |
| demo_video_url | text | NULL | later |

**Unique:** `(workout_id, sequence)`

---

## user_node

| Column | Type | Constraints | Notes |
| --- | --- | --- | --- |
| id | uuid | PK | |
| user_id | uuid | FK → app_user(id) ON DELETE CASCADE, NOT NULL | |
| node_id | uuid | FK → node(id), NOT NULL | |
| status | varchar(20) | NOT NULL, CHECK | LOCKED, AVAILABLE, … |
| progress_percentage | numeric(5,2) | NOT NULL, DEFAULT 0, CHECK 0–100 | |
| verified | boolean | NOT NULL, DEFAULT false | |
| verified_by_ai | boolean | NOT NULL, DEFAULT false | V2 |
| last_attempt_at | timestamptz | NULL | |
| best_score | numeric(5,2) | NULL | |
| current_score | numeric(5,2) | NULL | |
| unlocked_at | timestamptz | NULL | |
| mastery | varchar(20) | NULL, CHECK | later |
| created_at | timestamptz | NOT NULL, default now() | |
| updated_at | timestamptz | NOT NULL, default now() | |

**Unique:** `(user_id, node_id)`

---

## assessment

| Column | Type | Constraints | Notes |
| --- | --- | --- | --- |
| id | uuid | PK | |
| user_id | uuid | FK → app_user(id) ON DELETE CASCADE, NOT NULL | |
| node_id | uuid | FK → node(id), NOT NULL | skill being proved (usually workout.goal_node_id) |
| workout_session_id | uuid | FK → workout_session(id), NULL | set when verifying after a completed session |
| status | varchar(20) | NOT NULL, CHECK | |
| video_url | text | NULL | required by app when submitting proof |
| attempt_score | numeric(5,2) | NULL | |
| ai_form_score | numeric(5,2) | NULL | V2 |
| verified | boolean | NOT NULL, DEFAULT false | manual in MVP |
| remarks | text | NULL | |
| performed_at | timestamptz | NOT NULL, default now() | |
| created_at | timestamptz | NOT NULL, default now() | |
| updated_at | timestamptz | NOT NULL, default now() | |

Used after a session is `COMPLETED` to prove the workout’s goal node. Onboarding **placement questions** do not create assessments (no video yet).

---

## workout_session

| Column | Type | Constraints | Notes |
| --- | --- | --- | --- |
| id | uuid | PK | |
| user_id | uuid | FK → app_user(id) ON DELETE CASCADE, NOT NULL | |
| workout_id | uuid | FK → workout(id), NOT NULL | workout → `goal_node_id` = focus skill |
| plan_enrollment_id | uuid | FK → user_plan_enrollment(id), NULL | set for plan-driven sessions |
| plan_day_number | int | NULL | Day N within the plan |
| status | varchar(20) | NOT NULL, DEFAULT `PENDING`, CHECK | PENDING → IN_PROGRESS → COMPLETED |
| verified | boolean | NOT NULL, DEFAULT false | true after PASSED assessment on node (plan complete) |
| started_at | timestamptz | NULL | set when status → IN_PROGRESS |
| completed_at | timestamptz | NULL | set when status → COMPLETED |
| duration_seconds | int | NULL | derived OK |
| calories | int | NULL | optional |
| ai_score | numeric(5,2) | NULL | later |
| created_at | timestamptz | NOT NULL, default now() | |
| updated_at | timestamptz | NOT NULL, default now() | |

**App rule:** at most one `PENDING` or `IN_PROGRESS` session per user.

**Unlock next plan day:** session `COMPLETED` (verified not required).

**Unlock next node Day 1:** plan enrollment `AWAITING_VERIFY` + PASSED node assessment.

---

## workout_plan

| Column | Type | Constraints | Notes |
| --- | --- | --- | --- |
| id | uuid | PK | |
| node_id | uuid | FK → node(id), NOT NULL | skill / routine anchor this plan trains |
| title | varchar(160) | NOT NULL | |
| description | text | NULL | |
| kind | varchar(20) | NOT NULL, DEFAULT `SKILL` | `SKILL` or `DAILY_ROUTINE` |
| code | varchar(64) | UNIQUE, NULL | e.g. `morning_stretch` for routine lookup |
| duration_days | int | NOT NULL, ≥ 1 | |
| status | varchar(20) | NOT NULL, DEFAULT ACTIVE | ACTIVE / DEPRECATED; one ACTIVE per node (app rule) |
| created_at / updated_at | timestamptz | NOT NULL | |

## workout_plan_day

| Column | Type | Constraints | Notes |
| --- | --- | --- | --- |
| id | uuid | PK | |
| plan_id | uuid | FK → workout_plan(id) ON DELETE CASCADE | |
| day_number | int | NOT NULL, ≥ 1 | unique per plan |
| workout_id | uuid | FK → workout(id), NOT NULL | must belong to same node |

## user_plan_enrollment

| Column | Type | Constraints | Notes |
| --- | --- | --- | --- |
| id | uuid | PK | |
| user_id | uuid | FK → app_user | |
| plan_id | uuid | FK → workout_plan | |
| node_id | uuid | FK → node | denormalized focus |
| current_day | int | NOT NULL | |
| status | varchar(20) | NOT NULL | ACTIVE / AWAITING_VERIFY / COMPLETED |
| started_at / completed_at | timestamptz | | |
| created_at / updated_at | timestamptz | NOT NULL | |

---

## exercise_attempt

| Column | Type | Constraints | Notes |
| --- | --- | --- | --- |
| id | uuid | PK | |
| workout_session_id | uuid | FK → workout_session(id) ON DELETE CASCADE, NOT NULL | |
| workout_exercise_id | uuid | FK → workout_exercise(id), NOT NULL | |
| actual_sets | int | NULL | filled as user logs |
| actual_reps | int | NULL | |
| actual_hold_seconds | int | NULL | |
| actual_rest_seconds | int | NULL | |
| video_url | text | NULL | unused in MVP |
| notes | text | NULL | |
| ai_score | numeric(5,2) | NULL | later |
| status | varchar(20) | NOT NULL, DEFAULT `IN_PROGRESS`, CHECK | IN_PROGRESS → COMPLETED / SKIPPED |
| created_at | timestamptz | NOT NULL, default now() | |
| updated_at | timestamptz | NOT NULL, default now() | |

Created when the user **starts** that exercise line (not only at session end).

**Unique:** `(workout_session_id, workout_exercise_id)`

---

## Relationship summary

| From | To | FK | Cardinality |
| --- | --- | --- | --- |
| user_auth_identity | app_user | user_id | N:1 |
| refresh_token | app_user | user_id | N:1 |
| app_user | node | current_goal_node_id | N:1 |
| node | exercise | exercise_id | N:1 |
| node_edge | node | from_node_id, to_node_id | N:1 each |
| path_question | node | goal_node_id, node_id | N:1 each |
| workout | node | goal_node_id | N:1 |
| workout_exercise | workout, exercise | | N:1 each |
| user_node | app_user, node | | N:1 each |
| assessment | app_user, node | | N:1 each |
| assessment | workout_session | workout_session_id | N:1 (optional) |
| workout_session | app_user, workout | | N:1 each |
| exercise_attempt | workout_session, workout_exercise | | N:1 each |
