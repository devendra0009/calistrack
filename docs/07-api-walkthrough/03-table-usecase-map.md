# Table → use case map

What each table (and key columns) is for. Use this to drop unused V1 columns.

## Dynamic

| Table | Powers these screens / APIs | Key columns |
| --- | --- | --- |
| `app_user` | Profile, home greeting, goal | `display_name`, body stats, `current_goal_node_id`, `role` |
| `user_auth_identity` | Register/login credentials | `provider`, `email`, `password_hash` |
| `refresh_token` | Refresh / logout / rotate | `token_hash`, `expires_at`, `revoked_at` |
| `user_node` | Skill list %, unlock state after verify | `status`, `progress_percentage`, `verified` |
| `workout_session` | “What am I training?”, train loop, history | `status` (PENDING→…), `verified`, `workout_id`, timestamps |
| `exercise_attempt` | Per-line logging inside a session | `status` (IN_PROGRESS→COMPLETED), actual sets/reps |
| `assessment` | Post-workout skill proof | `node_id`, `workout_session_id`, `video_url`, `status`, `verified` |

## Static

| Table | Powers | Key columns |
| --- | --- | --- |
| `exercise` | Workout lines, node metric link | `name`, `metric_type`, `category`, `demo_video_url` (unused UI V1) |
| `node` | Goals, questions, explorer, assessment target | `name`, `target_value`, `operator`, `node_type`, `difficulty` |
| `node_edge` | Onboarding question order / unlock | `from_node_id`, `to_node_id` |
| `workout` | Session target template | `goal_node_id`, `title` |
| `workout_exercise` | Lines to attempt | `sequence`, target sets/reps/hold/rest |

## Column keep / later

| Column | V1 needed? | Why |
| --- | --- | --- |
| `workout_session.verified` | **Yes** | Gate for next workout |
| `workout_session.status = PENDING` | **Yes** | Assigned but not started |
| `assessment.workout_session_id` | **Yes** | Ties verify to completed session |
| `assessment.ai_form_score` | No (nullable) | V2 AI |
| `user_node.verified_by_ai` | No | V2 |
| `user_node.mastery` | No | Later |
| `exercise.demo_video_url` | No UI | Keep column |
| `exercise_attempt.video_url` | No | Keep column |

## Status enums ↔ UX

| `workout_session.status` | User sees |
| --- | --- |
| PENDING | “Your next workout” (not started) |
| IN_PROGRESS | Active training |
| COMPLETED + verified=false | “Done — verify skill to continue” |
| COMPLETED + verified=true | History; next PENDING already created |
| ABANDONED | Hidden / replaced |

| `exercise_attempt.status` | User sees |
| --- | --- |
| IN_PROGRESS | Current exercise |
| COMPLETED | Line done |
| SKIPPED | Skipped line |

| `user_node.status` | User sees |
| --- | --- |
| LOCKED | Grey |
| AVAILABLE / IN_PROGRESS | Can train toward |
| COMPLETED | Checkmark (after verified assessment) |

## Optimization prompts

1. Unlock next only on `session.verified` — not on COMPLETED alone.  
2. Onboarding answers: store permanently or one-shot? (V1: one-shot OK; no answers table.)  
3. Unlock graph: all prereqs vs any for multi-parent nodes (N7).  
4. One open PENDING/IN_PROGRESS session per user — enforce in service layer.
