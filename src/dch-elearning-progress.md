# DCH E-learning — Progress

## Stos technologiczny
- Java 25, Spring Boot 4.1.0 (parent zarządza wersjami zależności)
- PostgreSQL 18 (Docker/Testcontainers)
- Liquibase (migracje)
- Testcontainers 2.0.5 (zarządzane przez Spring Boot BOM)
- JUnit 5 + Mockito + AssertJ
- jjwt 0.13.0 (JWT)
- Jackson 3 (`tools.jackson.databind`) — domyślny w Spring Boot 4, współistnieje z Jackson 2 na classpath
- Docker Compose (Postgres lokalnie)

## Środowisko
- Docker Desktop skonfigurowany, storage driver `overlayfs` (containerd image store wyłączony — powodował błędy `EOF` przy pobieraniu obrazów)
- Testy integracyjne (`@Testcontainers`, `@ServiceConnection`) działają na realnym Postgresie w kontenerze
- Boot 4 modularyzacja: `@AutoConfigureMockMvc` wymaga jawnej zależności `spring-boot-starter-webmvc-test`, pakiet zmieniony na `org.springframework.boot.webmvc.test.autoconfigure`
- `docker-compose.yml` w roocie repo — Postgres 18-alpine, port 5432, wolumen `postgres_data:/var/lib/postgresql` (Postgres 18+ wymaga mountu bez `/data` na końcu — inaczej kontener nie startuje, błąd `pg_ctlcluster`-compatible layout)

## Moduł `user` — zaimplementowane

**Warstwa danych**
- `UserEntity` — pola: `id`, `email`, `password`, `role`, `version` (optimistic locking), `createdAt`/`updatedAt`
- Liquibase: sekwencja `users_seq` (increment 50) + tabela `users`
- `UserRepository extends JpaRepository<UserEntity, Long>` z `findByEmail`

**Warstwa serwisowa**
- `UserService` (z logowaniem SLF4J na każdej istotnej ścieżce):
  - `register(email, password, role)` — duplikat emaila → `UserAlreadyExistsException` (log warn)
  - `login(email, password)` — weryfikacja BCrypt, generuje JWT przez `JwtService`; błędne dane → `InvalidCredentialsException` (log warn, bez rozróżniania złego emaila vs hasła)
  - `findByEmail(email)` — używane przez endpoint `/me`
- `SecurityConfig` — bean `PasswordEncoder` (`BCryptPasswordEncoder`)
- `JwtProperties` (`@ConfigurationProperties(prefix="app.jwt")`, record) — `secret`, `expirationMs`
- `JwtService` — generowanie/parsowanie tokenów
- `JwtAuthenticationFilter` (`OncePerRequestFilter`) — wyciąga `Bearer` token z nagłówka, uwierzytelnia przez `SecurityContextHolder`; metody krótkie i nazwane (`extractToken`, `isBearerToken`, `authenticateFromToken`), stała `BEARER_PREFIX` zamiast magic number; log debug na nieprawidłowym/wygasłym tokenie
- `SecurityFilterChainConfig` — CSRF disabled, `SessionCreationPolicy.STATELESS`, `/api/users/register` i `/api/users/login` publiczne (stałe `REGISTER_PATH`/`LOGIN_PATH`), **reszta endpointów wymaga autentykacji** (`anyRequest().authenticated()`), filtr JWT podpięty przed `UsernamePasswordAuthenticationFilter`

**Warstwa REST**
- `POST /api/users/register` → 201 + `UserResponse`
- `POST /api/users/login` → 200 + `{"token": "..."}`
- `GET /api/users/me` (chroniony) → 200 + `UserResponse` bieżącego użytkownika
- `RegisterUserRequest` / `LoginRequest` (record) z walidacją: `@NotBlank`, `@Email`, `@Size(min=8)`
- `UserResponse` (record) — bez pola hasła
- `GlobalExceptionHandler` (`@RestControllerAdvice`, z logowaniem warn), `ProblemDetail` (RFC 7807):
  - `UserAlreadyExistsException` → 409
  - `InvalidCredentialsException` → 401
  - `MethodArgumentNotValidException` → 400

**Testy**
- `UserServiceTest` (Mockito): register happy path + duplikat emaila
- `ElearningApplicationTests`: kontekst Springa + Testcontainers Postgres
- `UserControllerIntegrationTest` (własna meta-adnotacja `@IntegrationTest`):
  - register → login zwraca token
  - register → login → `GET /me` z Bearer tokenem zwraca poprawnego użytkownika
- Wszystkie testy zielone

## Zasady kodowania ustalone w tej sesji (obowiązują dalej)
- Nigdy `var` — jawne typy
- Kod czytelny jak książka — krótkie metody, extract method
- Bez magicznych liczb/stringów — nazwane stałe
- Logger (SLF4J) od razu w nowych klasach, gdzie ma sens
- W testach: stałe lokalne per plik, bez wspólnej klasy stałych (premature abstraction)
- Dedykowane wyjątki + `ProblemDetail` zamiast generycznych wyjątków/map

## Otwarte tematy (odłożone, nie zrobione)
- `role` jako `String` — rozważyć enum, gdy role będą znane
- Appka jeszcze nie w tym samym `docker-compose.yml` (na razie tylko Postgres, appka z IDE)

## Następny logiczny krok
1. Dorzucić appkę jako drugi serwis w `docker-compose.yml` (Dockerfile + build)
2. Kolejny moduł z blueprintu (`course`)
