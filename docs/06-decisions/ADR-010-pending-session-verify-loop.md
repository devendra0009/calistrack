# ADR-010: Plan day train loop → verify node → next node Day 1

## Context

Skill nodes can take months to unlock. A single workout finishing must not jump the user to the next skill. Users need a curated multi-day plan per focus node, with skill video proof only when the plan is done.

## Decision

1. After goal + path questions, enroll the user in the focus node’s **ACTIVE** `workout_plan` and create a `workout_session` for **Day 1** (`status = PENDING`).
2. Starting an exercise → `IN_PROGRESS` + create `exercise_attempt` rows as the user goes.
3. Finishing all exercises → session `COMPLETED` with `verified = false`.
4. **Next plan day** unlocks on session COMPLETED (create PENDING for Day N+1). Calendar lag is allowed.
5. Finishing the **last** plan day → enrollment `AWAITING_VERIFY`; no new PENDING until node assessment PASS.
6. Assessment PASS on that node (after plan complete) → `user_node` COMPLETED + verified; enrollment COMPLETED; optionally set last session `verified = true`; enroll next path node’s plan Day 1.
7. Onboarding questions place the user; they do **not** require video. Video gates **node** unlock after the plan, not each day.

## Why

- Session row = “this user is training Day N of this node’s plan”
- Completing volume ≠ proving the skill
- Matches strict assessment rule (ADR-007) at the node boundary, not every workout day
- Curated catalog plans avoid per-user workout row explosion

## Consequences

- Home shows Day N of M; when `AWAITING_VERIFY`, CTA to Assessment
- Catalog: `workout_plan` + `workout_plan_day` + `user_plan_enrollment`
- `workout_session` may reference `plan_enrollment_id` + `plan_day_number`
- Assessment unlocks next **node** training only when enrollment is `AWAITING_VERIFY`
