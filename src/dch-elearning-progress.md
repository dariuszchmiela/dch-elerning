# DCH E-learning — Progress

## Stos technologiczny
- Backend: Java 25, Spring Boot 4.1.0, PostgreSQL 18, Liquibase, Testcontainers 2.0.5, jjwt 0.13.0, Jackson 3
- Frontend: Next.js 16 (App Router), TypeScript, Tailwind — scaffolded w `/frontend`
- Docker Compose (Postgres + appka, buildowana z Dockerfile)

## Środowisko
- Docker Desktop: storage driver `overlayfs` (containerd image store wyłączony — powodował błędy `EOF`)
- `docker-compose.yml` w roocie repo:
  - `postgres` (18-alpine, wolumen `postgres_data:/var/lib/postgresql` — Postgres 18+ wymaga mountu bez `/data`)
  - `app` (build z `Dockerfile` w roocie: multi-stage `maven:3.9-eclipse-temurin-25` → `eclipse-temurin:25-jre-alpine`), port 8080, zależny od postgres
  - `docker compose up --build` odpala całość jednym poleceniem — potwierdzone działające
- Frontend: `npm run dev` w `/frontend`, Next.js na `localhost:3000`
- Node.js zainstalowany natywnie (winget), trzeba pamiętać o restarcie terminala/IDE po instalacji żeby złapał PATH

## Moduł `user` (backend) — kompletny
**Warstwa danych:** `UserEntity`, Liquibase (sekwencja + tabela `users`), `UserRepository`

**Warstwa serwisowa:** `UserService` (register/login/findByEmail, BCrypt, JWT, SLF4J logging), `SecurityConfig`, `JwtProperties`/`JwtService`/`JwtAuthenticationFilter`, `SecurityFilterChainConfig` (stateless, register/login publiczne, reszta chroniona), `CorsProperties`/`CorsConfig` (origin z `application.yml`, nie hardcoded)

**Warstwa REST:** `POST /api/users/register`, `POST /api/users/login`, `GET /api/users/me` (chroniony), walidacja, `GlobalExceptionHandler` z `ProblemDetail` (409/401/400), wszystko z logowaniem

**Testy:** `UserServiceTest`, `ElearningApplicationTests`, `UserControllerIntegrationTest` (`@IntegrationTest` — własna meta-adnotacja); wszystkie zielone

**Weryfikacja end-to-end z przeglądarki (dziś):** CORS potwierdzony działający — `fetch` z `localhost:3000` → `localhost:8080` (register → login) przeszedł poprawnie, zwrócony realny token JWT

## Frontend — status
- Next.js scaffoldowany (`create-next-app`, TypeScript + Tailwind + App Router)
- Żadnych właściwych stron/komponentów jeszcze nie napisano (tylko domyślny starter)
- CORS po stronie backendu gotowy i zweryfikowany pod `localhost:3000`

## Zasady kodowania ustalone w tej sesji (obowiązują dalej)
- Nigdy `var` — jawne typy
- Kod czytelny jak książka — krótkie metody, extract method
- Bez magicznych liczb/stringów — nazwane stałe
- Logger (SLF4J) od razu w nowych klasach
- W testach: stałe lokalne per plik, bez wspólnej klasy stałych
- Dedykowane wyjątki + `ProblemDetail` zamiast generycznych wyjątków/map
- Konfigurowalne wartości (JWT secret, CORS origin) przez `@ConfigurationProperties`, nigdy hardcoded
- Nigdy skróty — zawsze właściwe rozwiązanie, nawet jeśli wolniejsze
- Przed długim promptem do Claude Code: najpierw mały fragment do potwierdzenia

## Następny logiczny krok
1. Właściwa strona `/login` w Next.js (formularz podpięty pod już zweryfikowany `fetch`)
2. Decyzja: jak przechowywać token JWT po stronie frontendu (localStorage vs cookie)
3. Strona `/register`
4. Kolejny moduł backendu z blueprintu (`course`) — równolegle z frontendem
