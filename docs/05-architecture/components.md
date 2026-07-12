# Components

```text
React SPA (Vite)
        │  REST HTTPS
Spring Boot modular monolith
        │
   PostgreSQL
        │
   Cloudinary (videos, avatars)
        │
Python AI service (V2+)
```

## Frontend (`frontend/`)

- Screens: auth, onboarding, home, workout, skills, progress, history, profile
- Talks only to Spring REST API
- MediaRecorder for assessment capture; upload via backend → Cloudinary

## Backend (`calistrack-backend/`)

Modular monolith (packages/modules, one deployable):

| Module | Responsibility |
| --- | --- |
| auth | Register/login, JWT, Spring Security |
| user | Profile CRUD, goal skill |
| catalog | Exercise, Node, NodeEdge, Workout (admin + read) |
| progress | UserNode, progress % |
| assessment | Create assessment, video URL, manual verify |
| workout | Start/complete session, exercise attempts |
| media | Cloudinary upload helpers |

## Database

PostgreSQL — source of truth. Schema changes via Flyway (in app), docs seed in `04-data/seed.sql`.

## Storage

Cloudinary — assessment videos, profile pictures, thumbnails/demo videos later.

## AI (future)

Separate Python FastAPI service (MediaPipe / OpenCV). Spring calls it asynchronously; assessment stays `PENDING_AI` until scored.
