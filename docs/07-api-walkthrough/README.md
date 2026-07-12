# 07 — API Walkthrough (pen & paper)

Click → API → what happens → which tables change → example rows.

Use this before coding controllers. If a column is never read or written here, question whether you need it in V1.

| File | Purpose |
| --- | --- |
| [00-cast-and-ids.md](00-cast-and-ids.md) | Actors, short IDs, static catalog snapshot |
| [01-story-dave.md](01-story-dave.md) | Full MVP journey step by step with dummy data |
| [02-api-catalog.md](02-api-catalog.md) | All MVP APIs in one list (build checklist) |
| [03-table-usecase-map.md](03-table-usecase-map.md) | Which table / column powers which screen |

## How to read

1. Skim **cast** (short IDs).
2. Walk **story-dave** — goal questions → PENDING session → train → verify → next.
3. Use **api-catalog** when implementing Spring controllers.
4. Use **table-usecase-map** when cutting unused columns.

## Legend

| Symbol | Meaning |
| --- | --- |
| `→` | User action / UI click |
| `API` | HTTP call the frontend makes |
| `Logic` | What backend does |
| `Write` | INSERT / UPDATE |
| `Read` | SELECT used for response |

Auth: `Authorization: Bearer <jwt>` on all routes except register/login.
