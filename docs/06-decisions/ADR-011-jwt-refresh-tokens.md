# ADR-011: Access JWT + hashed refresh tokens

## Context

Need auth that validates API calls without storing every access token, while still supporting logout and “stay logged in”.

## Decision

**Hybrid JWT:**

1. **Access token** — short-lived JWT (e.g. 15–60 min). Claims: `sub` (user id), `role`. Validated by signature + expiry only. **Not stored in DB.**
2. **Refresh token** — long-lived opaque random string. Client keeps it (prefer httpOnly cookie). Server stores only **`token_hash`** in `refresh_token` table.
3. **Authorization** — after JWT validates, use `role` (`USER` / `ADMIN`) on endpoints. No separate authz table.

## Flows

| Action | Server |
| --- | --- |
| Register / login | Verify credentials → issue access JWT + refresh → INSERT hashed refresh row |
| API request | Validate access JWT only (no DB token lookup) |
| Refresh | Lookup hash, check not revoked/expired → rotate (revoke old, insert new) → new access JWT |
| Logout | Set `revoked_at` on refresh row(s) |
| Logout all devices | Revoke all refresh rows for `user_id` |

## Why

- Stateless hot path for APIs
- Revocable sessions via refresh rows
- Never store raw tokens or access JWTs in Postgres

## Consequences

- Need JWT signing secret (or key pair) in env
- Need `refresh_token` table + rotation on refresh
- Frontend must refresh before access expiry (or on 401)
