# MVP API Catalog

Build checklist. All under `/api/v1`. Auth: JWT unless marked Public.

| #   | Method    | Path                      | When UI calls it                                                | Main tables                                                                 |
| --- | --------- | ------------------------- | --------------------------------------------------------------- | --------------------------------------------------------------------------- | ---------------------------------------------------------------------------- |
| V   | 1         | POST                      | `/auth/register`                                                | Register submit                                                             | `app_user`, `user_auth_identity`, `refresh_token`                            |
| V   | 2         | POST                      | `/auth/login`                                                   | Login submit                                                                | `user_auth_identity`, `app_user`, `refresh_token`                            |
| V   | 2a        | POST                      | `/auth/refresh`                                                 | Access JWT expired                                                          | `refresh_token` (rotate hash)                                                |
| V   | 2b        | POST                      | `/auth/logout`                                                  | Logout                                                                      | `refresh_token.revoked_at`                                                   |
| V   | 3         | GET                       | `/me`                                                           | Profile screen                                                              | `app_user`, goal `node`                                                      |
| V   | 4         | PATCH                     | `/me`                                                           | Edit profile / onboarding stats                                             | `app_user`                                                                   |
| V   | 5         | DELETE                    | `/me`                                                           | Delete account                                                              | soft delete (`app_user.deleted_at`); revoke sessions; disable Firebase       |
| V   | 6         | PUT                       | `/me/goal`                                                      | Pick / change goal                                                          | `app_user`, read `node`                                                      |
| V   | 7         | GET                       | `/onboarding/questions?goalNodeId=`                             | After goal select                                                           | `path_question`, `node_edge` (path walk)                                     |
| V   | 7a        | GET                       | `/onboarding/status`                                            | Route: questionnaire vs home                                                | `workout_session` exists?                                                    |
| V   | 8         | POST                      | `/onboarding/answers`                                           | Submit Q&A                                                                  | `user_node`, create `workout_session` **PENDING**                            |
| 9   | GET       | `/home`                   | Home tab                                                        | current session + goal + next                                               |
| 10  | GET       | `/progress`               | Progress tab                                                    | `user_node`, `workout_session`, `node`                                      |
| 11  | GET       | `/nodes`                  | Skill explorer list                                             | `node`, `user_node`                                                         |
| 12  | GET       | `/nodes/{id}`             | Skill detail                                                    | `node`, `node_edge`, `workout`, `user_node`                                 |
| V   | 13        | GET                       | `/workout-sessions/current`                                     | Home / resume                                                               | PENDING or IN_PROGRESS session                                               |
| V   | 13a       | POST                      | `/workout-sessions/{id}/begin`                                  | Start Training                                                              | PENDING → IN_PROGRESS + `startedAt`                                          |
| V   | 14        | POST                      | `/workout-sessions/{id}/exercises/{workoutExerciseId}/complete` | Mark exercise done                                                          | insert COMPLETED `exercise_attempt`                                          |
| V   | 15        | PATCH                     | `/exercise-attempts/{id}`                                       | Optional log sets/reps                                                      | `exercise_attempt` (unused in simplified UX)                                 |
| V   | 16        | POST                      | `/workout-sessions/{id}/complete`                               | Finish session                                                              | complete current; unlock next path node with a workout + new PENDING session |
| 17  | POST      | `/media/upload`           | Before verify video                                             | Cloudinary                                                                  |
| 18  | POST      | `/assessments`            | Verify goal node after session                                  | `assessment` (+ `workout_session_id`)                                       |
| 19  | PATCH     | `/admin/assessments/{id}` | Admin PASS/FAIL                                                 | `assessment`, `workout_session.verified`, `user_node`, next PENDING session |
| 20  | GET       | `/workout-sessions`       | History                                                         | `workout_session`, `workout`                                                |
| V   | 21        | GET                       | `/workout-sessions/{id}`                                        | Session train / history detail                                              | `workout_exercise`, `exercise_attempt`, …                                    |
| 22  | GET       | `/workouts/{id}`          | Preview template                                                | `workout`, `workout_exercise`, `exercise`                                   |
| 23  | GET/POST… | `/admin/nodes` … CRUD     | Admin seed/UI                                                   | catalog tables                                                              |

## Suggested implementation order

1. Auth (1–2, 2a–2b) + me (3–4)
2. Catalog read (11–12, 22) + seed
3. Goal + onboarding (6–8) → first PENDING session
4. Session train loop (13–16)
5. Verify + admin (17–19) → next PENDING
6. Home / progress / history (9–10, 20–21)

**Auth note:** Access JWT is validated in-memory (signature/expiry). Only refresh tokens hit `refresh_token`. See [ADR-011](../06-decisions/ADR-011-jwt-refresh-tokens.md).

## Out of MVP API surface

Leaderboard, friends, AI score webhook, streaks, notifications, OAuth.

Todo 15Jul:
V--create admin apis to create goalNodes, workouts, exercises, node_edge connecting, workout_exercises connecting, questions too
V--create workout for each node in muscle-up path(not complete but get the gist)
showing path from curr_node means how up user had come and what user can easily do (later)
for each node, submit assesment to rank on leaderboard and get verified (later) -> if u achieved one goal-node you are our celebrity and verified user

Todo 16Jul:
add a media service
v-> tomorrow create a whole flow for handstand and practice on that !! -> remove existing workout session/user_node if wanted to
v-> check on why next workout was not created for that user -> i think because nexxt node's workout was not created by admin thats why -> (PS: yes this was the issue!!) -> i think for mvp this is ok!!
not doing-> make the timer of workout pause/play (try completing it in one go) or maybenot!!
v-> while register, user can upload their dp
v-> create a media service
v-> creating placement questions for each node + completing handstand workouts & relation

20Jul
->1st phase live!!

todo phase2
-> After today’s workout for skill X, what should tomorrow (and later days) show?
-> if user left a workout for a long time -> just make it aborted !! -> give option to restart again
-> video to be uploaded for a exercise -> so admin can add a video of how to perform that exercise
-> todo: work on assessment part too !! -> user had a option to give assesment for all the goals in the path we assumed they can do !!
-> todo: github/strava like tracker(calendar)
-> compressing images/videos and then storing to save storage service