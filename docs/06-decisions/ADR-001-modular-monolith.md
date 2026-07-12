# ADR-001: Modular monolith (Spring Boot)

## Context

Need a backend that can grow (auth, catalog, workouts, assessments, later AI) without ops overhead of many services.

## Decision

Build one Spring Boot app as a **modular monolith** (clear packages/modules, single deployable JAR).

## Why

- Fits one developer / hobby pace
- One deploy path for ~1k users
- Modules can split later if needed

## Consequences

- Keep module boundaries clean (no random cross-package DB access)
- Do not invent microservices until a real scale/team reason appears
