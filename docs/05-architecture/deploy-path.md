# Deploy Path

Decide deploy before features pile up. Target: ~1k users, **one app server**.

## Recommended V1 path

| Piece | Simple option |
| --- | --- |
| Frontend | Build Vite → static host (Cloudflare Pages / Netlify / S3+CDN) |
| Backend | Spring Boot JAR on one VPS (Hetzner/DigitalOcean) or Railway/Render |
| DB | Managed Postgres (Neon / Railway / same VPS Docker) |
| Media | Cloudinary (no file server to maintain) |
| HTTPS | Host/platform TLS or Caddy/Nginx reverse proxy |

```text
Browser → Static SPA
       → API host (Spring) → Postgres
                          → Cloudinary
```

## Local first

1. Docker Compose optional: Postgres only
2. Run Spring + Vite locally
3. Cloudinary free tier for uploads

## Do later

- CI/CD (GitHub Actions: test + build + deploy)
- Separate AI service host
- CDN only if static assets need it beyond the SPA host

## Bottlenecks to watch

1. Video upload size/time → Cloudinary direct upload + size limits
2. AI latency (V2) → async jobs, not request-blocking
