# Deploy Path

Decide deploy before features pile up. Target: ~1k users, **one app server**.

## Recommended V1 path

| Piece    | Simple option                                                       |
| -------- | ------------------------------------------------------------------- |
| Frontend | Build Vite → static host (Cloudflare Pages / Netlify / S3+CDN)      |
| Backend  | Spring Boot JAR on one VPS (Hetzner/DigitalOcean) or Railway/Render |
| DB       | Managed Postgres (Neon / Railway / same VPS Docker)                 |
| Media    | Cloudinary (no file server to maintain)                             |
| HTTPS    | Host/platform TLS or Caddy/Nginx reverse proxy                      |

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

deployment steps & creds
-> docker
-> PAT: dckr_pat_abcdjkjkjiueriwqeudummy
-> docker login -u devendra0009

Backend deployment strategy
-> created dockerhub acc and pat
-> add these pat and acc name as secrets in that repo's secrets in github
-> create a backend-cicd.yml file and push that in the repo
-> this should contain all the steps github actions have to do
-> setup the db online and get its creds
-> deploy the image on render (image will be automatically built and pushed on docker via cicd)
-> add envs and restart the service
-> check the health endpoint
-> setup a trigger using render's deploy hook in github-actions ci/cd file
-> now on every push your backend will get deployed automatically
