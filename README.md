# DCH E-learning

Interactive learning platform — modular monolith backend (Java 25 / Spring Boot 4) with a Next.js frontend.

## Tech stack

**Backend**
- Java 25, Spring Boot 4.1.0
- PostgreSQL 18, Liquibase migrations
- Spring Security + JWT (jjwt 0.13.0)
- JUnit 5, Mockito, Testcontainers 2.0.5

**Frontend**
- Next.js 16 (App Router), TypeScript
- Tailwind CSS 4

**Infrastructure**
- Docker Compose (PostgreSQL + application)

## Running locally

### Backend + database

```bash
docker compose up --build
```

Backend runs on `http://localhost:8080`.

> **Podman users:** `--build` does not reliably rebuild the image.
> Use `docker compose build --no-cache app` before `docker compose up`.

### Frontend

```bash
cd frontend
cp .env.example .env.local
npm install
npm run dev
```

Frontend runs on `http://localhost:3000`.

## Configuration

| Variable | Where | Default | Purpose |
|---|---|---|---|
| `JWT_SECRET` | backend env | dev placeholder | JWT signing key |
| `app.jwt.expiration-ms` | `application.yml` | 3600000 (1h) | Token lifetime |
| `app.cors.allowed-origins` | `application.yml` | `http://localhost:3000` | Allowed frontend origin |
| `NEXT_PUBLIC_API_URL` | `frontend/.env.local` | `http://localhost:8080` | Backend base URL |

## Architecture

Package-by-feature modular monolith. Each module owns its entity, repository, service and controller.

## Environment gotchas

**Podman (Linux):** `docker compose up --build` silently runs a stale image.
Run `docker compose build --no-cache app` explicitly before starting.

**Docker Desktop (Windows):** if `docker pull` fails with `EOF` errors, disable
*"Use containerd for pulling and storing images"* in Settings → General.

**PostgreSQL 18+:** the data volume must mount at `/var/lib/postgresql`,
not `/var/lib/postgresql/data` — otherwise the container exits on startup.