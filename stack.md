# Technology Stack – Poker Stats Web App

This document is based on `FUNCTIONALITY_DESIGN.md` and `UI_WIREFRAMES.md`. The app is a mobile‑first platform for logging home poker sessions, tracking personal/group performance, and showing leaderboards for two roles: casual player and admin.

## Domain Assumptions Relevant to Technology Choices
- Core entities: `User`, `Player`, `GameSession`, `SessionResult`, plus derived statistics (calculated, not stored).
- Integrity needs: balanced totals (buy‑in equals cash‑out across a session), non‑negative amounts, session edits allowed (no time limit), data export.
- UX priorities: mobile-first, fast data entry, charts and leaderboards, cached views.

## Frontend (React)
- Language: TypeScript; React 18.
- Dev/build: Vite (fast dev server, simple SPA setup).
- Routing: React Router.
- UI/Styling: Tailwind CSS + Headless UI for design tokens and mobile-first patterns. Alternative: Material UI.
- Tables/charts: TanStack Table; Recharts (or Chart.js) for line/bar/pie.
- Forms/validation: React Hook Form + Zod.
- Server state: TanStack Query (caching, retries, sync, “offline-capable: view cached data”).
- I18n/formatting: Intl APIs (currency, dates) with `dayjs`/`date-fns`.
- Testing: Vitest + React Testing Library; E2E: Playwright.
- Lint/format: ESLint, Prettier.

## Backend (Kotlin/Java)
- Language/Runtime: Kotlin on JDK 21.
- Framework: Spring Boot 3.x.
  - Web: Spring MVC (REST, JSON via Jackson Kotlin module).
  - Validation: Jakarta Bean Validation (e.g., @NotNull, @Positive).
  - Security: Spring Security, JWT (roles: `casual_player`, `admin`), password hashing (Argon2/BCrypt).
  - API docs: OpenAPI 3 (springdoc-openapi) + TypeScript client generation for the frontend.
  - Caching: Spring Cache + Redis (cache player stats and leaderboards; invalidate on session create/edit).
  - Observability: Spring Actuator, Micrometer → Prometheus/Grafana; JSON logs (ELK/OpenTelemetry).
  - Testing: JUnit 5, MockK, Testcontainers (database/Redis).
- Build: Gradle with Kotlin DSL.
  
  - File storage abstraction: introduce a `StorageService` with two implementations:
    - `LocalStorageService` (default on Raspberry Pi prod): stores files on the Pi’s external SSD and serves them as static resources via Spring Boot.
    - `S3StorageService` (optional): integrates with an external S3 provider (e.g., AWS S3, Backblaze B2, Cloudflare R2) if you later decide to offload storage.
    - Persist only file URLs/paths in the database; keep binaries out of the DB.

## Database
- Primary recommendation: PostgreSQL (ACID, strong integrity constraints, ergonomic aggregations for stats/leaderboards).
  - ORM: Spring Data JPA + Hibernate.
  - Migrations: Flyway (SQL) or Liquibase.
  - Model (summary):
    - `users` (unique email, password hash, role).
    - `players` (name, avatar_url, optional link to `users`).
    - `game_sessions` (start datetime, location, min_buy_in, game type, timestamps, notes).
    - `session_results` (FK: session_id, player_id, buy_in, cash_out; profit_loss computed in app or via a view/materialized view).
  - Key indexes: `session_results(player_id)`, `session_results(session_id)`, `game_sessions(start_time)`, `users(email UNIQUE)`.
  - Money types: `NUMERIC(12,2)` or integer in cents (preferred for accuracy; `BigDecimal`/`Long` in code).
  - Consistency: constraints (CHECK non-negative), transactions covering session + result inserts/edits.

## Stats Layer and Performance
- On‑demand aggregations for small volume; at scale, use materialized views (PostgreSQL) or pre‑aggregated snapshots in Redis.
- Cache per user/date range; invalidate on create/edit/delete of session results.
- Optional async jobs: scheduled leaderboard recomputations (Spring Scheduling) — not required for MVP.

## Additional Integrations
- Avatars and file storage (Raspberry Pi as prod):
  - Default: use local filesystem on the Pi’s external SSD. Store uploads under a dedicated directory and serve them as static files from Spring Boot.
  - Avoid deploying MinIO on the Pi (adds RAM/CPU without redundancy). If you need cloud durability or presigned URLs later, switch the `StorageService` to an external S3 provider.
  - Keep only URLs/paths in the model.
- Data export: CSV/JSON endpoints; stream large downloads.

## CI/CD and Infrastructure
- Containerization: Docker; dev environment via Docker Compose (backend, frontend, Postgres, Redis).
- CI: GitHub Actions (build + test backend/frontend, scans, Docker image).
- Monitoring: Prometheus + Grafana; logs to ELK/Loki; tracing via OpenTelemetry.
- Configuration: Spring profiles + `.env` for Compose; CI secrets for production.
  
  - Storage on Pi: mount an external SSD for uploads and database data to avoid SD‑card wear. Example (Compose):
    ```yaml
    services:
      backend:
        volumes:
          - /mnt/ssd/poker-uploads:/app/uploads
        environment:
          APP_STORAGE_PROVIDER: local
          APP_STORAGE_DIR: /app/uploads
    ```
    If using an external S3 provider later, set `APP_STORAGE_PROVIDER=s3` and provide S3 credentials/bucket via environment variables; no MinIO deployment is required on the Pi.

## Testing Strategy
- Backend: unit and integration tests (Testcontainers for Postgres/Redis). Validate money integrity (balanced totals, amounts ≥ 0) and role permissions.
- Frontend: unit tests for components; E2E for critical flows (add session, view stats, leaderboards).

## Minimal MVP – Proposed Decisions
- Frontend: React + Vite + Tailwind + TanStack Query + Recharts.
- Backend: Kotlin + Spring Boot (Web, Security, Validation, Data JPA, Cache, Actuator, OpenAPI).
- Database: PostgreSQL + Flyway; Redis for caching.
- Auth: JWT (httpOnly), roles `admin`/`casual_player`.
- Hosting: Docker images; basic monitoring (Actuator + Prometheus).
