# Story: Dave’s Muscle-Up path (MVP)

One continuous example. After each step, only the tables that changed are shown.

Assume catalog seed already loaded (N1…N10, W1…W3).

**Core loop:** goal → path questions → `workout_session` PENDING → train → COMPLETED (unverified) → video assess goal node → verified → next PENDING session.

---

## Step 0 — Static world (before Dave)

**No user API.** Seed/admin already ran.

### `node` (excerpt)

| id | name | target | operator | unit |
| --- | --- | --- | --- | --- |
| N1 | Australian Pull-up | 10 | >= | REPS |
| N4 | First Pull-up | 1 | >= | REP |
| N5 | 5 Pull-ups | 5 | >= | REPS |
| N6 | 10 Pull-ups | 10 | >= | REPS |
| N10 | Muscle Up | 1 | >= | REP |

### `node_edge` (excerpt)

| from (prereq) | to (next) |
| --- | --- |
| N1 | N2 |
| … | … |
| N5 | N6 |
| N6 | N7 |
| N6 | N8 |
| N7 | N10 |

### `workout`

| workout | goal_node |
| --- | --- |
| W1 Pull Strength Beginner | N6 |
| W2 Chest To Bar Prep | N7 |
| W3 Muscle Up Transition | N10 |

---

## Step 1 — Register

**API** `POST /api/v1/auth/register`

### `app_user` / `user_auth_identity`

| app_user | auth |
| --- | --- |
| U-DAVE, Dave, USER, goal=null | LOCAL, dave@example.com, hash… |

Also issues access JWT + refresh token → INSERT `refresh_token` (hash only).

**Stories:** US-01

---

## Step 2 — Login / refresh / logout

**API** `POST /api/v1/auth/login`  
→ access JWT (not in DB) + refresh → new `refresh_token` row

| id | user_id | token_hash | expires_at | revoked_at |
| --- | --- | --- | --- | --- |
| RT1 | U-DAVE | a1b2c3… | +30d | null |

**API** `POST /api/v1/auth/refresh` `{ "refreshToken": "…" }`  
→ find hash, not revoked → revoke RT1, insert RT2, new access JWT

**API** `POST /api/v1/auth/logout`  
→ set `revoked_at` on current refresh row

Protected APIs: `Authorization: Bearer <access JWT>` — signature check only.

**Stories:** US-02

---

## Step 3 — Optional profile stats

**API** `PATCH /api/v1/me` → height/weight/age/gender/experience

**Stories:** US-03

---

## Step 4 — Choose goal skill

**API** `PUT /api/v1/me/goal` `{ "goalNodeId": "N10" }`

| id | current_goal_node_id |
| --- | --- |
| U-DAVE | N10 |

**Stories:** US-05

---

## Step 5 — Path questions → place Dave → PENDING session

**UI →** Questions from path to Muscle-Up (no video):

1. How many Australian Pull-ups (N1)? → `12`
2. Can you do 5 Pull-ups (N5)? → `Yes`
3. Can you do 10 Pull-ups (N6)? → `No`

**API** `GET /api/v1/onboarding/questions?goalNodeId=N10`  
**API** `POST /api/v1/onboarding/answers`

```json
{
  "goalNodeId": "N10",
  "answers": [
    { "nodeId": "N1", "type": "REPS", "value": 12 },
    { "nodeId": "N5", "type": "YES_NO", "value": true },
    { "nodeId": "N6", "type": "YES_NO", "value": false }
  ]
}
```

**Logic**
1. Place `user_node` rows (self-report; `verified=false` until video later).
2. Focus skill = N6 → pick W1 (`goal_node_id = N6`).
3. `INSERT workout_session` **PENDING**, `verified=false`, `started_at=null`.
4. Return S1 to client.

### `user_node`

| nodes | status | verified |
| --- | --- | --- |
| N1…N5 | COMPLETED | false |
| N6 | AVAILABLE | false |
| N7…N10 | LOCKED | false |

### `workout_session`

| id | workout | status | verified | started_at |
| --- | --- | --- | --- | --- |
| S1 | W1 | PENDING | false | null |

**Stories:** US-06

---

## Step 6 — Home

**API** `GET /api/v1/home`

Shows goal N10 + current session S1 (PENDING) + W1 title / goal N6.

**Stories:** US-12

---

## Step 7 — Start an exercise → IN_PROGRESS

**API** `POST /api/v1/workout-sessions/S1/exercises/WE1/start`

1. Session PENDING → **IN_PROGRESS**, set `started_at`.
2. Insert `exercise_attempt` EA1 `status=IN_PROGRESS`.

| session | status | verified |
| --- | --- | --- |
| S1 | IN_PROGRESS | false |

| attempt | WE | status |
| --- | --- | --- |
| EA1 | WE1 | IN_PROGRESS |

**Stories:** US-13

---

## Step 8 — Finish lines → session COMPLETED (not verified)

**API** `PATCH /api/v1/exercise-attempts/{id}` → sets/reps + `COMPLETED` (repeat per line)

**API** `POST /api/v1/workout-sessions/S1/complete`

| S1 | COMPLETED | verified=**false** |

| attempts | all COMPLETED with logged volume |

N6 `user_node` → IN_PROGRESS (trained, not proved).  
**No next workout yet.** UI: “Verify 10 Pull-ups”.

---

## Step 9 — Verify workout goal node (video)

**API** `POST /api/v1/assessments`

```json
{
  "nodeId": "N6",
  "workoutSessionId": "S1",
  "videoUrl": "https://res.cloudinary.com/.../dave-10pu.mp4"
}
```

| assessment | node | session | status | verified |
| --- | --- | --- | --- | --- |
| A1 | N6 | S1 | PENDING_REVIEW | false |

**Stories:** US-10

---

## Step 10 — Admin PASS → verified → next PENDING

**API** `PATCH /api/v1/admin/assessments/A1` → PASSED

1. S1.`verified = true`
2. N6 user_node → COMPLETED, verified=true
3. Unlock next (e.g. N8 AVAILABLE)
4. `INSERT` S2 **PENDING** for next workout

| S1 | COMPLETED | verified=true |
| S2 | PENDING | verified=false |

**Only this step creates the next workout session.**

---

## Step 11 — Progress / history

**API** `GET /api/v1/progress`  
**API** `GET /api/v1/workout-sessions`

List shows `verified` so UX can separate “trained” vs “proved”.

**Stories:** US-16, US-17

---

## Step 12 — Skill explorer / profile

Unchanged in spirit: `GET /nodes`, `GET /nodes/{id}`, `PUT /me/goal`, `DELETE /me`.

Changing goal may abandon open PENDING session.

**Stories:** US-07, US-08, US-09, US-03

---

## End-state (Dave)

| Table | Rows |
| --- | --- |
| workout_session | S1 verified + S2 PENDING |
| exercise_attempt | 4 on S1 |
| assessment | 1 (N6 ↔ S1) |
| user_node | 10 |

---

## Enum cheat sheet

| Event | workout_session | exercise_attempt | assessment | focus user_node |
| --- | --- | --- | --- | --- |
| Onboarding answers | INSERT PENDING | — | — | placement |
| Start exercise | → IN_PROGRESS | INSERT IN_PROGRESS | — | → IN_PROGRESS |
| Finish line | — | → COMPLETED | — | — |
| All lines done | → COMPLETED, verified=false | — | — | unproven |
| Submit video | — | — | PENDING_REVIEW | — |
| Admin PASS | verified=true | — | PASSED | → COMPLETED |
| After PASS | INSERT next PENDING | — | — | unlock next |
