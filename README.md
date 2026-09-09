
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
| POST | `/api/users/register` | — | Create account |
| POST | `/api/users/login` | — | Obtain JWT |
| GET | `/api/users/me` | Bearer | Current user profile |

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