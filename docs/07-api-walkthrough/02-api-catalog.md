# MVP API Catalog

Build checklist. All under `/api/v1`. Auth: JWT unless marked Public.

| # | Method | Path | When UI calls it | Main tables |
| --- | --- | --- | --- | --- |
| 1 | POST | `/auth/register` | Register submit | `app_user`, `user_auth_identity` |
| 2 | POST | `/auth/login` | Login submit | `user_auth_identity`, `app_user` |
| 3 | GET | `/me` | Profile screen | `app_user`, goal `node` |
| 4 | PATCH | `/me` | Edit profile / onboarding stats | `app_user` |
| 5 | DELETE | `/me` | Delete account | cascades dynamic rows |
| 6 | PUT | `/me/goal` | Pick / change goal | `app_user`, read `node` |
| 7 | GET | `/onboarding/questions?goalNodeId=` | After goal select | `node`, `node_edge` (path) |
| 8 | POST | `/onboarding/answers` | Submit Q&A | `user_node`, create `workout_session` **PENDING** |
| 9 | GET | `/home` | Home tab | current session + goal + next |
| 10 | GET | `/progress` | Progress tab | `user_node`, `workout_session`, `node` |
| 11 | GET | `/nodes` | Skill explorer list | `node`, `user_node` |
| 12 | GET | `/nodes/{id}` | Skill detail | `node`, `node_edge`, `workout`, `user_node` |
| 13 | GET | `/workout-sessions/current` | Home / resume | PENDING or IN_PROGRESS session |
| 14 | POST | `/workout-sessions/{id}/exercises/{workoutExerciseId}/start` | Start one exercise | session → IN_PROGRESS; insert `exercise_attempt` |
| 15 | PATCH | `/exercise-attempts/{id}` | Log sets/reps; mark COMPLETED | `exercise_attempt` |
| 16 | POST | `/workout-sessions/{id}/complete` | All lines done | session → COMPLETED, `verified=false` |
| 17 | POST | `/media/upload` | Before verify video | Cloudinary |
| 18 | POST | `/assessments` | Verify goal node after session | `assessment` (+ `workout_session_id`) |
| 19 | PATCH | `/admin/assessments/{id}` | Admin PASS/FAIL | `assessment`, `workout_session.verified`, `user_node`, next PENDING session |
| 20 | GET | `/workout-sessions` | History | `workout_session`, `workout` |
| 21 | GET | `/workout-sessions/{id}` | History detail | `exercise_attempt`, … |
| 22 | GET | `/workouts/{id}` | Preview template | `workout`, `workout_exercise`, `exercise` |
| 23 | GET/POST… | `/admin/nodes` … CRUD | Admin seed/UI | catalog tables |

## Suggested implementation order

1. Auth (1–2) + me (3–4)  
2. Catalog read (11–12, 22) + seed  
3. Goal + onboarding (6–8) → first PENDING session  
4. Session train loop (13–16)  
5. Verify + admin (17–19) → next PENDING  
6. Home / progress / history (9–10, 20–21)  

## Out of MVP API surface

Leaderboard, friends, AI score webhook, streaks, notifications, OAuth.
