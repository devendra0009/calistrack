# 06 — Decisions (ADRs)

Short Architecture Decision Records. One decision per file.

| ADR | Decision |
| --- | --- |
| [ADR-001](ADR-001-modular-monolith.md) | Spring modular monolith |
| [ADR-002](ADR-002-react-vite-spa.md) | React + Vite SPA |
| [ADR-003](ADR-003-postgres-flyway.md) | PostgreSQL + Flyway |
| [ADR-004](ADR-004-cloudinary.md) | Cloudinary for media |
| [ADR-005](ADR-005-native-auth.md) | Native email/password auth |
| [ADR-006](ADR-006-graph-edge-direction.md) | Prereq → next edge direction |
| [ADR-007](ADR-007-strict-video-assessment.md) | Video required for skills |
| [ADR-008](ADR-008-check-vs-lookup.md) | CHECK enums for V1 |
| [ADR-009](ADR-009-defer-ai-service.md) | Defer Python AI |
| [ADR-010](ADR-010-pending-session-verify-loop.md) | PENDING session → train → verify → next |

## How to update

1. Add `ADR-0xx-slug.md` with Context / Decision / Why / Consequences.
2. Link it in this README.
3. Update `05-architecture/stack.md` if the stack changed.
4. Never rewrite history — add a new ADR that supersedes an old one.
