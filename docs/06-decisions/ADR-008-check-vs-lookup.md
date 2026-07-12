# ADR-008: CHECK constraints vs lookup tables

## Context

Need enums for statuses, difficulties, etc. Lookup tables are flexible but heavier.

## Decision

V1: store enum-like values as strings with **CHECK constraints**. No lookup tables yet.

## Why

- Fewer tables/joins for MVP
- Values are stable workflow codes

## Consequences

- Adding a new status requires a migration
- Promote to lookup tables when admins must edit lists or attach metadata (see `04-data/enums-and-lookups.md`)
