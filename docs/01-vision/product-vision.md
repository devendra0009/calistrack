# Product Vision

CaliTrack helps people learn calisthenics through a clear skill path — not by logging "Workout #42".

## Why this exists

1. **Real users** — beginners need a path from "I can do some pull-ups" to a goal skill (e.g. muscle-up).
2. **Learn engineering** — modular monolith, skill graph, video pipeline, later AI.

## Product questions (always answer these)

| Question | What the app shows |
| --- | --- |
| Where am I today? | Current node + progress % |
| What can I already do? | Completed / verified nodes |
| What am I closest to unlocking? | Next node(s) on the goal path |
| What prerequisite am I missing? | Locked prereqs on the graph |
| Which workout next? | Workout for current focus node (simple rules in V1; AI later) |
| How long have I been progressing? | History + activity |
| What PRs have I hit? | Passed assessments / completed nodes |
| What should I train this week? | Goal skill + next node |
| Am I improving? | Progress % over time |

## Core idea

Every advanced move is a **node**. Nodes connect in a **skill graph** (prerequisites → next skill). Users prove nodes with **assessments** (video), train with **workouts**, and watch **progress** toward a **goal skill**.

## Principles

- Skill-first
- Graph-driven progression (no hardcoded paths in app logic)
- Strict proof for skills (video required)
- Simple MVP first
- Business rules live in the backend, not only in the UI

## MVP success

A beginner can register, pick a goal, complete assessment + workouts, and see clear progress toward that skill.
