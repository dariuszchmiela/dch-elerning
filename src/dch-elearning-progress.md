# DCH E-learning — Progress

## Stos technologiczny
- Java 25, Spring Boot 4.1.0 (parent zarządza wersjami zależności)
- PostgreSQL 18 (Docker/Testcontainers)
- Liquibase (migracje)
- Testcontainers 2.0.5 (zarządzane przez Spring Boot BOM)
- JUnit 5 + Mockito + AssertJ
- jjwt 0.13.0 (JWT)
- Jackson 3 (`tools.jackson.databind`) — domyślny w Spring Boot 4, współistnieje z Jackson 2 na classpath

## Środowisko
- Docker Desktop skonfigurowany, storage driver `overlayfs` (containerd image store wyłączony — powodował błędy `EOF` przy pobieraniu obrazów)
- Testy integracyjne (`@Testcontainers`, `@ServiceConnection`) działają na realnym Postgresie w kontenerze
- Boot 4 modularyzacja: `@AutoConfigureMockMvc` wymaga jawnej zależności `spring-boot-starter-webmvc-test` (nie jest już ciągnięta przez `spring-boot-starter-test`), pakiet zmieniony na `org.springframework.boot.webmvc.test.autoconfigure`

## Moduł `user` — zaimplementowane

**Warstwa danych**
- `UserEntity` — pola: `id`, `email`, `password`, `role`, `version` (optimistic locking), `createdAt`/`updatedAt` (Hibernate `@CreationTimestamp`/`@UpdateTimestamp`)
- Liquibase: sekwencja `users_seq` (increment 50) + tabela `users`
- `UserRepository extends JpaRepository<UserEntity, Long>` z `findByEmail`

**Warstwa serwisowa**
- `UserService`:
  - `register(email, password, role)` — sprawdza duplikat emaila → `UserAlreadyExistsException`, hashuje hasło (BCrypt), zapisuje
  - `login(email, password)` — weryfikuje przez `PasswordEncoder.matches()`, generuje token JWT przez `JwtService`; błędne dane → `InvalidCredentialsException` (bez rozróżniania czy zły email czy hasło)
- `SecurityConfig` — bean `PasswordEncoder` (`BCryptPasswordEncoder`)
- `JwtProperties` (`@ConfigurationProperties(prefix="app.jwt")`, record) — `secret`, `expirationMs`; włączone przez `@ConfigurationPropertiesScan` na klasie głównej
- `JwtService` — generowanie (`generateToken`) i parsowanie (`extractEmail`) tokenów, klucz z `JwtProperties`
- `SecurityFilterChainConfig` — CSRF wyłączony, `/api/users/register` i `/api/users/login` publiczne; **tymczasowo `anyRequest().permitAll()`** — brak jeszcze filtra weryfikującego JWT na innych endpointach

**Warstwa REST**
- `POST /api/users/register` → 201 + `UserResponse`
- `POST /api/users/login` → 200 + `{"token": "..."}`
- `RegisterUserRequest` / `LoginRequest` (record) z walidacją: `@NotBlank`, `@Email`, `@Size(min=8)` na hasło
- `UserResponse` (record) — response DTO bez pola hasła
- `GlobalExceptionHandler` (`@RestControllerAdvice`), format `ProblemDetail` (RFC 7807):
  - `UserAlreadyExistsException` → 409 Conflict
  - `InvalidCredentialsException` → 401 Unauthorized
  - `MethodArgumentNotValidException` (błędy walidacji) → 400 Bad Request

**Testy**
- `UserServiceTest` (Mockito): register happy path + duplikat emaila; wszystkie zielone
- `ElearningApplicationTests`: kontekst Springa + Testcontainers Postgres
- `UserControllerIntegrationTest` (`@IntegrationTest` — własna meta-adnotacja łącząca `@SpringBootTest`+`@AutoConfigureMockMvc`+`@Testcontainers`+`@ActiveProfiles("test")`): pełny flow register → login zwraca token, na realnym Postgresie; zielony
- Response DTO w testach mapowane przez `JsonMapper` (Jackson 3), nie stary `ObjectMapper`

## Otwarte tematy (odłożone, nie zrobione)
- `role` jako `String` — rozważyć enum (`STUDENT`/`INSTRUCTOR`/`ADMIN`), gdy role będą znane
- Filtr JWT chroniący endpointy — obecnie wszystko publiczne (`anyRequest().permitAll()`)
- Docker Compose do lokalnego odpalania całości (Postgres + appka) — nieporuszone

## Następny logiczny krok
1. Filtr weryfikujący JWT (`OncePerRequestFilter`) i podłączenie go do `SecurityFilterChain`
2. Pierwszy chroniony endpoint, np. `GET /api/users/me` (profil zalogowanego użytkownika)
3. Alternatywnie: przejście do kolejnego modułu z blueprintu (`course`)
