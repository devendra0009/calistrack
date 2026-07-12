# ADR-009: Defer Python AI service

## Context

AI form scoring and smart recommendations are core to the long-term product, not to week-1 learning.

## Decision

Ship V1 **without** AI. Keep nullable AI columns. Add a **Python FastAPI** service in V2+.

## Why

- MVP question is path + progress, not ML
- Avoid blocking the skill tracker on model work

## Consequences

- Manual assessment review for V1
- Spring will call AI async later; do not put ML inside the Java monolith
