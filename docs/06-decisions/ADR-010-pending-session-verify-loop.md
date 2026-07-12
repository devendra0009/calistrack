# ADR-010: PENDING session → train → verify → next workout

## Context

Need a clear loop from goal placement to “what am I training now” without unlocking the next skill just because sets were logged.

## Decision

1. After goal + path questions, create a `workout_session` with `status = PENDING` (not started).
2. Starting an exercise → `IN_PROGRESS` + create `exercise_attempt` rows as the user goes.
3. Finishing all exercises → `COMPLETED` with `verified = false`.
4. Unlock / assign next workout only after a **PASSED** assessment on that workout’s `goal_node_id` (sets `session.verified = true`).
5. Onboarding questions place the user; they do **not** require video. Video is for post-workout node verification.

## Why

- Session row = “this user is assigned this workout now”
- Completing volume ≠ proving the skill
- Matches strict assessment rule (ADR-007) at the right moment

## Consequences

- Home reads current `PENDING` / `IN_PROGRESS` / unverified `COMPLETED` session
- Assessment gains optional `workout_session_id`
- `workout_session.status` includes `PENDING`; `started_at` nullable
