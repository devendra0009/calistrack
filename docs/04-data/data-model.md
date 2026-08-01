# Data Model

Tech-agnostic. Implementation: PostgreSQL (see `db-schema.md`).

## Entity groups

| Group | Entities | Mutability |
| --- | --- | --- |
| Identity | User, UserAuthIdentity, RefreshToken | Dynamic |
| Catalog | Exercise, Node, NodeEdge, Workout, WorkoutExercise | Static (admin/seed) |
| Progress | UserNode, Assessment | Dynamic |
| Training | WorkoutSession, ExerciseAttempt | Dynamic |

## Entities

### User

Athlete profile. Holds `current_goal` → Node. Password is **not** on User — see UserAuthIdentity.

### UserAuthIdentity

How the user signs in. V1: one `LOCAL` row (email + password hash). Ready for more providers later without reshaping User.

### RefreshToken

Hashed refresh token for session continuity and logout/revoke. Access JWTs are **not** persisted — see [ADR-011](../06-decisions/ADR-011-jwt-refresh-tokens.md).

### Exercise

Atomic movement (Pull-up, Dead Hang, Rows). Nodes and workout lines point here. Optional demo/thumbnail URLs for later.

### Node

A skill or measurable milestone on the learning path (First Pull-up, 10 Pull-ups, Muscle-Up). Linked to an Exercise + target metric (e.g. reps >= 10). Keeps its own `name`, optional `description`, and `difficulty`. Category lives only on Exercise — join via `exercise_id`.

### NodeEdge

Directed edge: **from_node = prerequisite**, **to_node = next skill** (bottom-up toward harder skills).

### Workout

Template aimed at a goal Node (e.g. “Chest To Bar Prep”). `kind` is `SKILL` (path training) or `STRETCH` (daily routine).

### WorkoutExercise

Ordered line items inside a Workout (sets, reps, hold, rest).

### UserNode

Per-user state on a Node: locked / available / in progress / completed, progress %, verified flags.

### WorkoutSession

One assigned run of a Workout for a User (usually Day N of a node’s curated plan). Created as `PENDING` after onboarding Day 1 or after the prior plan day completes. Moves to `IN_PROGRESS` when training starts, `COMPLETED` when all exercises are done. Completing a day unlocks Day N+1; finishing the last day awaits node assessment before the next node’s Day 1.

Stretch sessions use the same table with `workout.kind = STRETCH`, no plan enrollment, and no assessment.

### WorkoutPlan / WorkoutPlanDay

Curated multi-day template for a skill node (`kind = SKILL`) or a daily routine (`kind = DAILY_ROUTINE`, e.g. `code = morning_stretch`). Days point at shared catalog workouts (never cloned per user).

### UserPlanEnrollment

Per-user run of a plan: `ACTIVE` while training days, `AWAITING_VERIFY` after last day, `COMPLETED` after node PASS.

### ExerciseAttempt

Created when the user **starts** a line in the session (`IN_PROGRESS`), then `COMPLETED` / `SKIPPED` with logged sets/reps/rest.

### Assessment

Video proof for a Node. When the node’s enrollment is `AWAITING_VERIFY`, PASS unlocks the next path node’s plan Day 1. Onboarding placement Q&A does **not** create assessments.

## Relationships

```mermaid
erDiagram
  User ||--o{ UserAuthIdentity : has
  User ||--o{ RefreshToken : has
  User ||--o| Node : current_goal
  User ||--o{ UserNode : progress
  User ||--o{ Assessment : attempts
  User ||--o{ WorkoutSession : logs
  Node ||--o{ NodeEdge : as_prereq
  Node ||--o{ NodeEdge : as_next
  Exercise ||--o{ Node : measures
  Workout ||--o| Node : goal_skill
  Workout ||--o{ WorkoutExercise : contains
  Exercise ||--o{ WorkoutExercise : used_in
  Node ||--o{ WorkoutPlan : has_plan
  WorkoutPlan ||--o{ WorkoutPlanDay : days
  WorkoutPlanDay }o--|| Workout : uses
  User ||--o{ UserPlanEnrollment : enrolls
  UserPlanEnrollment }o--|| WorkoutPlan : of
  WorkoutSession }o--|| Workout : based_on
  WorkoutSession }o--o| UserPlanEnrollment : plan_day
  WorkoutSession ||--o{ ExerciseAttempt : has
  WorkoutExercise ||--o{ ExerciseAttempt : records
  WorkoutSession ||--o{ Assessment : may_verify
  Node ||--o{ Assessment : proves
  Node ||--o{ UserNode : tracks
```

## Lifecycle

```text
Admin seeds Exercises → Nodes → NodeEdges → Workouts → WorkoutExercises
User registers → picks goal
  → Onboarding questions (from goal path) → answers place UserNodes
  → Create WorkoutSession PENDING (workout for next focus node)
User starts exercise → session IN_PROGRESS + ExerciseAttempt IN_PROGRESS
User finishes exercises → attempts COMPLETED → session COMPLETED (verified=false)
User submits Assessment video for workout.goal_node_id
  → PASS → session.verified=true, UserNode COMPLETED, unlock next
  → Create next WorkoutSession PENDING
```
