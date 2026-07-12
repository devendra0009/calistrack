# ADR-007: Strict video assessments

## Context

Want users to take skills seriously; “mark done” is easy to abuse.

## Decision

Skill proof (after a completed workout session) requires **video upload**. No mark-complete-without-video for unlocking the next workout. MVP verify is **manual** (`verified`, status `PENDING_REVIEW` / `PASSED` / `FAILED`).

Onboarding path questions do **not** require video — they only place the user and create a `PENDING` session.

Workouts log sets/reps/rest only (no attempt video required).

## Why

- Higher trust for progress and future leaderboards
- Keeps MVP honest without AI yet

## Consequences

- More friction at assessment time (accepted)
- Demo exercise videos are schema-ready but not required for V1 UX
