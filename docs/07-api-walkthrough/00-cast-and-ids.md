# Cast and IDs

Example athlete: **Dave**. Goal: **Muscle-Up**. Catalog = Muscle-Up path from [`../04-data/seed.sql`](../04-data/seed.sql).

Short IDs below map to seed UUIDs. Use short IDs in walkthrough tables; use UUIDs in real code.

## People

| Short | Name | Role | UUID (seed) |
| --- | --- | --- | --- |
| U-DAVE | Dave | USER | `55555555-5555-5555-5555-555555550001` |
| U-ADMIN | Admin | ADMIN | `44444444-4444-4444-4444-444444440001` |

## Nodes (N#)

| Short | Name | Seed UUID suffix |
| --- | --- | --- |
| N1 | Australian Pull-up | `…20001` |
| N2 | Band Assisted Pull-up | `…20002` |
| N3 | Negative Pull-up | `…20003` |
| N4 | First Pull-up | `…20004` |
| N5 | 5 Pull-ups | `…20005` |
| N6 | 10 Pull-ups | `…20006` |
| N7 | Chest To Bar | `…20007` |
| N8 | Explosive Pull-up | `…20008` |
| N9 | 15 Dips | `…20009` |
| N10 | Muscle Up | `…20010` |

Full UUID pattern: `22222222-2222-2222-2222-22222222` + last 4 from suffix above.

## Workouts (W#)

| Short | Title | Goal node |
| --- | --- | --- |
| W1 | Pull Strength Beginner | N6 (10 Pull-ups) |
| W2 | Chest To Bar Prep | N7 |
| W3 | Muscle Up Transition | N10 |

## Exercises used in W1

| Short | Name | Role in W1 |
| --- | --- | --- |
| E-SCAP | Scapular Pull | seq 1 |
| E-PU | Pull-up | seq 2 |
| E-ROW | Rows | seq 3 |
| E-HANG | Dead Hang | seq 4 |

W1 line IDs (imagine): `WE1`…`WE4` for `workout_exercise` rows.

## Graph (prereq → next)

```text
N1 → N2 → N3 → N4 → N5 → N6 → N7 → N10
                         ↘ N8 ↗
N9 → N10
```

## Static catalog (already in DB before Dave joins)

Admin/seed already filled: `exercise`, `node`, `node_edge`, `workout`, `workout_exercise`.

Dave’s journey writes **dynamic** tables: `app_user`, `user_auth_identity`, `refresh_token`, `user_node`, `assessment`, `workout_session`, `exercise_attempt`.

Access JWTs are never stored (see [ADR-011](../06-decisions/ADR-011-jwt-refresh-tokens.md)).
