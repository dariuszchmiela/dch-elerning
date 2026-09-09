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

### Frontend

```bash
cd frontend
cp .env.example .env.local
npm install
npm run dev
```

Frontend runs on `http://localhost:3000`.

## Environment gotchas

**Podman (Linux):** `docker compose up --build` silently runs a stale image.
Run `docker compose build --no-cache app` explicitly before starting.

**Docker Desktop (Windows):** if `docker pull` fails with `EOF` errors, disable
*"Use containerd for pulling and storing images"* in Settings → General.

**PostgreSQL 18+:** the data volume must mount at `/var/lib/postgresql`,
not `/var/lib/postgresql/data` — otherwise the container exits on startup.

## Configuration

| Variable | Where | Default | Purpose |
|---|---|---|---|
| `JWT_SECRET` | backend env | dev placeholder | JWT signing key |
| `app.jwt.expiration-ms` | `application.yml` | 3600000 (1h) | Token lifetime |
| `app.cors.allowed-origins` | `application.yml` | `http://localhost:3000` | Allowed frontend origin |
| `NEXT_PUBLIC_API_URL` | `frontend/.env.local` | `http://localhost:8080` | Backend base URL |

## Architecture

Package-by-feature modular monolith. Each module owns its entity, repository, service and controller.

### Package structure

```
src/main/java/com/dch/dchelearning/
├── config/     cross-cutting: security, JWT, CORS, exception handling
└── user/       user module: registration, login, profile
```

### Authentication flow

1. `POST /api/users/register` — creates account, password hashed with BCrypt
2. `POST /api/users/login` — verifies credentials, returns JWT
3. `GET /api/users/me` — protected; requires `Authorization: Bearer <token>`

`JwtAuthenticationFilter` reads the token on every request and populates the
`SecurityContext`. Sessions are stateless — no server-side session state.

### Error responses

All errors use RFC 7807 `ProblemDetail`:

| Exception | Status |
|---|---|
| `UserAlreadyExistsException` | 409 Conflict |
| `InvalidCredentialsException` | 401 Unauthorized |
| `MethodArgumentNotValidException` | 400 Bad Request |

## API

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/api/v1/users/register` | — | Create account |
| POST | `/api/v1/users/login` | — | Obtain JWT |
| GET | `/api/v1/users/me` | Bearer | Current user profile |

## Frontend pages

| Route | Description |
|---|---|
| `/` | Landing page |
| `/register` | Account creation, redirects to `/login` |
| `/login` | Authentication, stores JWT, redirects to `/me` |
| `/me` | Protected profile view with logout |

## Testing

```bash
mvn test
```

Integration tests use the `@IntegrationTest` meta-annotation
(`@SpringBootTest` + `@AutoConfigureMockMvc` + `@Testcontainers` + test profile)
and run against a real PostgreSQL container.

## Conventions

- No `var` — explicit types everywhere
- Short, extracted methods — code reads top-down
- No magic values — named constants
- SLF4J logger in services, filters and controllers
- Configuration from properties, never hardcoded
- Dedicated exceptions mapped to `ProblemDetail`
- All user-facing text in English

## Known limitations

- **JWT stored in `localStorage`** — vulnerable to XSS. Production should use
  httpOnly cookies with CSRF protection, short-lived access tokens and refresh tokens.
- `role` is a plain `String` on the backend — should become an enum once roles settle.