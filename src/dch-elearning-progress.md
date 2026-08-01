# DCH E-learning — Progress

## Stos technologiczny
- Java 25, Spring Boot 4.1.0 (parent zarządza wersjami zależności)
- PostgreSQL 18 (Docker/Testcontainers)
- Liquibase (migracje)
- Testcontainers 2.0.5 (zarządzane przez Spring Boot BOM)
- JUnit 5 + Mockito + AssertJ

## Środowisko
- Docker Desktop skonfigurowany, storage driver `overlayfs` (containerd image store wyłączony — powodował błędy `EOF` przy pobieraniu obrazów)
- Testy integracyjne (`@Testcontainers`, `@ServiceConnection`) działają na realnym Postgresie w kontenerze

## Moduł `user` — zaimplementowane

**Warstwa danych**
- `UserEntity` — pola: `id`, `email`, `password`, `role`, `version` (optimistic locking), `createdAt`/`updatedAt` (Hibernate `@CreationTimestamp`/`@UpdateTimestamp`)
- Liquibase: sekwencja `users_seq` (increment 50) + tabela `users`
- `UserRepository extends JpaRepository<UserEntity, Long>` z `findByEmail`

**Warstwa serwisowa**
- `UserService.register(email, password, role)`:
  - sprawdza duplikat emaila → `UserAlreadyExistsException`
  - hashuje hasło przez `PasswordEncoder` (BCrypt, `spring-security-crypto`)
  - zapisuje przez `UserRepository`
- `SecurityConfig` — bean `PasswordEncoder` (`BCryptPasswordEncoder`)

**Warstwa REST**
- `POST /api/users/register`
- `RegisterUserRequest` (record) z walidacją: `@NotBlank`, `@Email`, `@Size(min=8)` na hasło
- `UserResponse` (record) — response DTO bez pola hasła
- `GlobalExceptionHandler` (`@RestControllerAdvice`), format `ProblemDetail` (RFC 7807):
  - `UserAlreadyExistsException` → 409 Conflict
  - `MethodArgumentNotValidException` (błędy walidacji) → 400 Bad Request

**Testy**
- `UserServiceTest` (Mockito): happy path (hasło zahashowane, dane poprawne) + duplikat emaila rzuca wyjątek
- `ElearningApplicationTests`: kontekst Springa + Testcontainers Postgres

## Otwarte tematy (odłożone, nie zrobione)
- `role` jako `String` — rozważyć enum (`STUDENT`/`INSTRUCTOR`/`ADMIN`), gdy role będą znane
- Brak testu integracyjnego/kontrolerowego dla `POST /api/users/register` (na razie tylko unit testy serwisu)
- Docker Compose do lokalnego odpalania całości (Postgres + appka) — nieporuszone

## Następny logiczny krok: logowanie
Po rejestracji naturalnym krokiem jest **uwierzytelnianie**:
1. Endpoint `POST /api/users/login` — przyjmuje email + hasło, weryfikuje przez `PasswordEncoder.matches()`
2. Decyzja: sesje (Spring Session) vs JWT (stateless, typowe dla API + SPA/mobile)
3. Jeśli JWT: dodanie zależności (np. `jjwt`), generowanie/parsowanie tokenu, filtr weryfikujący token na chronionych endpointach
4. Dopiero to otwiera drogę do pełnego `spring-boot-starter-security` z konfiguracją `SecurityFilterChain` (obecnie mamy tylko `spring-security-crypto`, bez mechanizmu autentykacji HTTP)
