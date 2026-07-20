# User Stories

Format: **US-xx** — As a … I want … so that …

Status: `MVP` | `Later`

---

## Auth & profile

| ID | Story | Status |
| --- | --- | --- |
| US-01 | As a new user, I want to register with email and password so I can use the app. | MVP |
| US-02 | As a user, I want to log in, refresh my session, and log out. | MVP |
| US-03 | As a user, I want to view, edit, and delete my profile (name, body stats, experience). | MVP |
| US-04 | As a user, I want guest / OTP / OAuth / MFA login. | Later |

## Onboarding & goal

| ID | Story | Status |
| --- | --- | --- |
| US-05 | As a new user, I want to choose a goal skill (node) so workouts target that path. | MVP |
| US-06 | As a new user, I want goal-path questions (reps / yes-no) so the app places me and assigns my first PENDING workout. | MVP |
| US-07 | As a user, I want to change my goal skill so my next workouts follow the new path. | MVP |

## Skills & graph

| ID | Story | Status |
| --- | --- | --- |
| US-08 | As a user, I want to see all skills with my progress % on each. | MVP |
| US-09 | As a user, I want to open a skill and see description, requirements, and a recommended workout. | MVP |
| US-10 | As a user, after completing a workout session, I want to submit a video for that workout’s goal skill so I can unlock the next workout (no mark-done without video). | MVP |
| US-11 | As a user, I want AI to auto-verify my form video. | Later |

## Home & workouts

| ID | Story | Status |
| --- | --- | --- |
| US-12 | As a user, I want a home screen with greeting, goal, and my current PENDING/IN_PROGRESS/unverified session. | MVP |
| US-13 | As a user, I want to start each exercise (creates attempt), log sets/reps/rest, complete the session as unverified. | MVP |
| US-14 | As a user, I want workout demo videos and form tips in-app. | Later |
| US-15 | As a user, I want AI to pick the best next workout. | Later |

## Progress & history

| ID | Story | Status |
| --- | --- | --- |
| US-16 | As a user, I want a progress dashboard (goal %, completed nodes, today’s workout status). | MVP |
| US-17 | As a user, I want a history of completed workout sessions. | MVP |
| US-18 | As a user, I want a GitHub-like activity grid based on workouts done. | Later |

## Social & admin

| ID | Story | Status |
| --- | --- | --- |
| US-19 | As a user, I want a leaderboard (verified attempts rank higher). | Later |
| US-20 | As an admin, I want to create nodes, edges, workouts, and assign workouts to nodes (seed/CRUD). | MVP |

---

## Happy path (MVP)

```text
Register → Pick goal → Path questions (no video)
  → WorkoutSession PENDING created
  → Start exercises → IN_PROGRESS + attempts
  → Complete session (COMPLETED, verified=false)
  → Video assess goal node → PASS → session.verified=true
  → Next WorkoutSession PENDING
  → Progress + history
```
