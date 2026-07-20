# Stack

Locked for V1. Reasons: `06-decisions/`.

## Frontend

| Concern | Choice |
| --- | --- |
| Framework | React 19 |
| Bundler | Vite |
| Styling | Tailwind CSS |
| Routing | React Router DOM |
| Server state | TanStack Query |
| Forms | React Hook Form + Zod |
| Icons | Lucide React |
| Toasts | Sonner (or React Hot Toast) |
| Charts | Recharts (progress later) |
| Video capture | MediaRecorder API |

Repo folder: `frontend/`

## Backend

| Concern | Choice |
| --- | --- |
| Language | Java 21 (LTS) |
| Framework | Spring Boot 3.x |
| Build | Maven |
| API docs | Springdoc OpenAPI |
| Security | Spring Security + short-lived access JWT + hashed `refresh_token` rows ([ADR-011](../06-decisions/ADR-011-jwt-refresh-tokens.md)) |
| Persistence | Spring Data JPA |
| Migrations | Flyway |

Repo folder: `calistrack-backend/`

## Data & media

| Concern | Choice |
| --- | --- |
| DB | PostgreSQL 17 (or latest stable) |
| DB GUI | DBeaver |
| Files | Cloudinary |

## Future AI

| Concern | Choice |
| --- | --- |
| Service | Python + FastAPI |
| Libs | MediaPipe, OpenCV, etc. |

## Explicitly not V1

- Next.js, microservices, Firebase Auth, mobile apps, Kafka, Redis (unless a real bottleneck appears)
