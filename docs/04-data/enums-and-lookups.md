# Enums and Lookups

## Rule (V1)

| Prefer | When |
| --- | --- |
| **CHECK constraint** (or app enum matching DB) | Fixed workflow values that rarely change (statuses, operators, difficulties) |
| **Lookup table** | Values admins edit as product content, or lists that grow often |

V1 uses **CHECK only**. No lookup tables yet — fewer joins, faster MVP.

## When to promote CHECK → table (learn this)

Promote when **any** of these become true:

- Non-devs need to add values without a migration
- You need extra columns per value (label, sort order, icon, i18n)
- The same value set is shared and queried as data (filters, admin UI)

Example later: `exercise_category` table if you add many categories with icons.

## V1 CHECK sets

| Domain | Allowed values |
| --- | --- |
| auth `provider` | `LOCAL` (later: `GOOGLE`, …) |
| `node_type` | `MILESTONE`, `SKILL`, `HOLD`, `MOBILITY` |
| `node.status` / `workout.status` / `exercise` difficulty sibling | `ACTIVE`, `COMING_SOON`, `DEPRECATED` |
| `difficulty` | `BEGINNER`, `INTERMEDIATE`, `ADVANCED`, `ELITE` |
| `category` (exercise only; not on node) | `PULL`, `PUSH`, `CORE`, `BALANCE`, `STATIC`, `MOBILITY`, `LEGS` |
| `metric_type` | `TIME`, `REPS`, `DISTANCE`, `ANGLE`, `WEIGHT` |
| `operator` | `>=`, `<=`, `==`, `<`, `>` |
| `relation_type` | `PREREQUISITE` |
| `assessment.status` | `NOT_ATTEMPTED`, `PENDING_REVIEW`, `PENDING_AI`, `PASSED`, `FAILED` |
| `user_node.status` | `LOCKED`, `AVAILABLE`, `IN_PROGRESS`, `COMPLETED` |
| `mastery` | `BRONZE`, `SILVER`, `GOLD`, `PLATINUM` (nullable until used) |
| `workout_session.status` | `PENDING`, `IN_PROGRESS`, `COMPLETED`, `ABANDONED` |
| `exercise_attempt.status` | `IN_PROGRESS`, `COMPLETED`, `SKIPPED` |
| `gender` | `MALE`, `FEMALE`, `OTHER`, `UNSPECIFIED` |
| `experience` | `BEGINNER`, `INTERMEDIATE`, `ADVANCED` |

### Workout session lifecycle

| Status | Meaning |
| --- | --- |
| `PENDING` | Assigned after goal Q&A (or after prior node verified). User has not started exercises yet. |
| `IN_PROGRESS` | User started at least one exercise in this session. |
| `COMPLETED` | All exercises done. **Not enough to unlock next** until `verified = true`. |
| `ABANDONED` | User quit / replaced. |

`workout_session.verified` becomes `true` only after a **PASSED** assessment on that workout’s `goal_node_id` (video + manual review in V1). Then the system may create the **next** session as `PENDING`.

Note: MVP assessment review uses `PENDING_REVIEW` + `verified` boolean. Keep `PENDING_AI` for V2 without a schema break.
