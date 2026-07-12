# ADR-003: PostgreSQL + Flyway

## Context

Skill graph, progress, and sessions need relational integrity and recursive path queries later.

## Decision

Use **PostgreSQL** as the only primary DB. Version schema with **Flyway**.

## Why

- Fits graph edges + FKs cleanly
- Flyway keeps schema history reviewable (no “mystery production DB”)
- Team already chose Postgres + DBeaver

## Consequences

- Docs `seed.sql` is reference; real changes go through Flyway in the backend
- No Mongo for core domain
