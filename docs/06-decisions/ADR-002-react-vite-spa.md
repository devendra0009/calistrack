# ADR-002: React + Vite SPA

## Context

Need a web UI for auth, graph, workouts, video capture. Backend is already a separate Spring API.

## Decision

Use **React 19 + Vite** SPA (not Next.js for V1).

## Why

- Matches REST + JWT API style
- Vite is fast for local MVP
- Avoid SSR complexity until SEO/marketing site needs it

## Consequences

- Host as static files + API elsewhere
- Routing is client-side (React Router)
