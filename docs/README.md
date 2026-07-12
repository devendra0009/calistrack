# CaliTrack Docs

Single source of truth for product, requirements, data, and architecture.

Keep docs short. Prefer trackable IDs (`US-01`, `MVP-01`, `ADR-001`). Update the matching folder when something changes — do not duplicate facts across folders.

## Folders

| Folder | What lives here |
| --- | --- |
| [01-vision](01-vision/) | Why we build this, product questions, future |
| [02-requirements](02-requirements/) | User stories, MVP scope, roadmap |
| [03-ux](03-ux/) | User flows and simple wireframes |
| [04-data](04-data/) | Data model, DB schema, seed SQL |
| [05-architecture](05-architecture/) | Components, stack, deploy path |
| [06-decisions](06-decisions/) | ADRs — why we chose X |
| [07-api-walkthrough](07-api-walkthrough/) | Click → API → DB with example data (Dave story) |

## How to use

1. Start with **vision** if you need the product why.
2. Use **requirements** to track what to build this week.
3. Use **data** before writing entities or Flyway migrations.
4. Use **api-walkthrough** before coding endpoints (see exact tables per click).
5. Use **decisions** when revisiting a tech or design choice.

## Repo apps (code)

- `frontend/` — React 19 + Vite
- `calistrack-backend/` — Spring Boot modular monolith
