# ADR-006: Graph edge direction (prereq → next)

## Context

Node edges were drawn both ways in brainstorms. Unlock logic needs one meaning.

## Decision

`node_edge.from_node_id` = **prerequisite**  
`node_edge.to_node_id` = **next skill to unlock**  
(bottom-up toward the goal)

Example: `10 Pull-ups` → `Chest To Bar` → `Muscle Up`

## Why

- Matches “what do I need before X?”
- Easier progression queries toward a goal

## Consequences

- Seed data and admin UI must follow this direction only
- Docs/examples that used reverse arrows are obsolete
