# 04 — Data

What we store and how it connects. Tech-agnostic model first; Postgres details in schema + seed.

| File | Purpose |
| --- | --- |
| [data-model.md](data-model.md) | Entities + relationships |
| [db-schema.md](db-schema.md) | Tables, columns, types, constraints |
| [enums-and-lookups.md](enums-and-lookups.md) | CHECK vs lookup table rule |
| [seed.sql](seed.sql) | CREATE + INSERT for Muscle-Up path |

## How to update

1. Change `data-model.md` when a concept changes.
2. Mirror columns in `db-schema.md` and `seed.sql`.
3. Real app migrations use Flyway in the backend — keep this seed as the docs reference until V1 migrations land.
