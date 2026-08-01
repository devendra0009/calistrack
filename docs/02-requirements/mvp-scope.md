# MVP Scope

**One question:** Can CaliTrack guide a beginner toward their first skill?

**Week-1 goal:** Register → choose goal → path questions → PENDING workout → train → verify node → next workout.

## IN (MVP / V1)

| ID | Deliverable |
| --- | --- |
| MVP-01 | Email + password auth + basic profile |
| MVP-02 | Admin/seed: nodes, skill graph, workouts, assign workouts to nodes |
| MVP-03 | One seeded path: Australian Pull-up → … → Muscle-Up |
| MVP-04 | Goal-path onboarding questions (no video); place UserNodes; create first `workout_session` PENDING |
| MVP-05 | Train loop: PENDING → IN_PROGRESS + exercise_attempts → COMPLETED unverified |
| MVP-06 | Home shows current session (PENDING / IN_PROGRESS / needs verify) |
| MVP-07 | Post-workout video assessment on `workout.goal_node_id`; manual verify |
| MVP-08 | On plan complete + PASS: verify node, unlock next node’s Day 1 PENDING |
| MVP-09 | Progress dashboard + skill explorer |
| MVP-10 | Workout history (include verified flag) |

## OUT (do not build in week 1)

| ID | Item |
| --- | --- |
| OUT-01 | AI video analysis / form scoring |
| OUT-02 | Leaderboard, friends, feed, likes, comments |
| OUT-03 | XP, achievements, streaks, push notifications |
| OUT-04 | Smart recommendation engine |
| OUT-05 | Mark skill done without video |
| OUT-06 | Native mobile app |
| OUT-07 | Guest / OAuth / MFA / OTP auth |

## Week plan

| Day | Deliverable |
| --- | --- |
| 1 | Auth + user profile |
| 2 | Node graph + seed data |
| 3 | Goal + onboarding answers → PENDING session + UserNode |
| 4 | Home + skill explorer |
| 5 | Train loop (start exercise / complete session) |
| 6 | Verify assessment + next PENDING + history |
| 7 | Testing + polish |

## Rules locked for MVP

- Onboarding questions = placement only (no video).
- `workout_session` created as **PENDING** when next workout is assigned.
- Completing exercises → session **COMPLETED** but `verified=false`; unlocks **next plan day** (or `AWAITING_VERIFY` on last day).
- Next **node** only after **PASSED** assessment when the plan is complete → `user_node.verified=true`, next node Day 1 PENDING.
- Exercise attempts created when a line **starts** (`IN_PROGRESS` → `COMPLETED`).
- Graph: `from_node` = prerequisite, `to_node` = next skill.
