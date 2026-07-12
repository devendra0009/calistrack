# ADR-005: Native email/password auth

## Context

MVP needs login. Options: Firebase Auth vs Spring Security email/password.

## Decision

**Native email + password** with Spring Security + JWT. Identity stored in `user_auth_identity` (`LOCAL`).

## Why

- Backend already includes Spring Security
- Full control, no extra vendor for auth in V1
- Auth identity table still allows OAuth providers later

## Consequences

- We own password hashing, reset flows, token refresh
- Firebase deferred (can revisit if auth becomes the bottleneck)
