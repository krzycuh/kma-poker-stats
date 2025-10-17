# Stos technologiczny – Poker Stats Web App

Dokument opracowany na podstawie wymagań i projektu z plików `FUNCTIONALITY_DESIGN.md` oraz `UI_WIREFRAMES.md`. Aplikacja: mobilno‑pierwszy serwis do logowania domowych rozgrywek pokerowych, prezentacji statystyk i leaderboardów dla graczy z rolami: casual player oraz admin.

## Założenia domenowe istotne dla wyboru technologii
- Kluczowe encje: `User`, `Player`, `GameSession`, `SessionResult` oraz pochodne statystyki (wyliczane, nieprzechowywane).
- Wymagania integralności: spójność sum (buy‑in = cash‑out), brak wartości ujemnych, edycje sesji (bez limitu czasu), eksport danych.
- Dostępności/UX: mobilność, szybkie wprowadzanie wyników, wykresy i rankingi, caching wyświetlanych danych.

## Frontend (React)
- Język: TypeScript, React 18.
- Narzędzie startowe: Vite (szybki dev server, prosta konfiguracja SPA).
- Routing: React Router.
- UI/Styling: Tailwind CSS + Headless UI (łatwe odwzorowanie design tokens, mobile‑first). Alternatywa: Material UI.
- Tabele/wykresy: TanStack Table, Recharts (lub Chart.js) do linii/słupków/kołowych.
- Formularze/walidacje: React Hook Form + Zod.
- Stan serwera: TanStack Query (cache, retry, synchronizacja, „offline‑capable: view cached data”).
- I18n i formatowanie: Intl API (waluty, daty); `dayjs`/`date-fns`.
- Testy: Vitest + React Testing Library; E2E: Playwright.
- Budowanie/jakość: ESLint, Prettier.

## Backend (Kotlin/Java)
- Język/Runtime: Kotlin (JDK 21).
- Framework: Spring Boot 3.x.
  - Web: Spring MVC (REST, JSON via Jackson Kotlin Module).
  - Walidacja: Jakarta Bean Validation (np. @NotNull, @Positive).
  - Bezpieczeństwo: Spring Security, JWT (role: `casual_player`, `admin`), hashowanie haseł (Argon2/BCrypt).
  - Dokumentacja API: OpenAPI 3 (springdoc-openapi) + generowanie klienta TS dla frontendu.
  - Cache: Spring Cache + Redis (cache’owanie statystyk i leaderboardów, invalidacja po zapisach/edycjach sesji).
  - Observability: Spring Actuator, Micrometer → Prometheus/Grafana; logi w formacie JSON (ELK/Opentelemetry).
  - Testy: JUnit 5, MockK, Testcontainers (baza/Redis).
- Budowanie: Gradle Kotlin DSL.

## Baza danych – rekomendacja i alternatywy
- Rekomendacja główna: PostgreSQL (ACID, silne ograniczenia integralności, złożone agregacje do statystyk/leaderboardów).
  - ORM: Spring Data JPA + Hibernate.
  - Migracje: Flyway (SQL) lub Liquibase.
  - Model (skrót):
    - `users` (unikalny email, hasło hash, rola).
    - `players` (nazwa, avatar_url, opcjonalne powiązanie z `users`).
    - `game_sessions` (data/godzina, lokalizacja, min_buy_in, typ gry, timestamps, notatki).
    - `session_results` (FK: session_id, player_id, buy_in, cash_out, profit_loss jako pole wyliczane po stronie aplikacji lub widok/materialized view).
  - Kluczowe indeksy: `session_results(player_id)`, `session_results(session_id)`, `game_sessions(start_time)`, `users(email UNIQUE)`.
  - Pieniądze: `NUMERIC(12,2)` lub integer w groszach (preferowane dla dokładności; w kodzie `BigDecimal`/`Long`).
  - Spójność: constraints (CHECK dla wartości nieujemnych), transakcje na zapis sesji + wyników.

### Czy MongoDB sprawdzi się w tym zastosowaniu?
- Odpowiedź krótka: Tak, da się zrealizować appkę na MongoDB, ale do tego konkretnego problemu (silne relacje, walidacje kwot, liczne agregacje i rankingi) lepszym wyborem startowym jest PostgreSQL.
- Kiedy MongoDB ma sens:
  - Chcemy większej elastyczności schematu i łatwego osadzania danych (np. wyniki graczy w obrębie dokumentu sesji).
  - Wolumen danych jest umiarkowany, a zapisy grupowe odbywają się per sesję (co pozwala na atomowe operacje w obrębie jednego dokumentu).
  - Akceptujemy, że część integralności przeniesiemy do logiki aplikacyjnej i/lub walidatorów schematu.
- Wzorzec modelu dla MongoDB (przykładowo):
  - Kolekcja `sessions`: dokument zawiera metadane sesji + tablicę `results` z wpisami `{ playerId, buyIn, cashOut, notes }` (atomowość jednej sesji).
  - Kolekcje `players`, `users` – referencje po `id`.
  - Agregacje do statystyk: `$unwind` po `results`, `$group` po `playerId` dla sum/ROI/win rate; indeksy po `results.playerId`, `date`, `location`.
  - Edycje sesji: pojedyncza aktualizacja dokumentu; kontrola spójności sum w logice.
- Kompromisy MongoDB:
  - Trudniejsza egzekucja reguł integralności (brak FK, CHECK), co jest istotne przy obrocie pieniędzmi.
  - Złożone agregacje są możliwe, ale mogą być mniej ergonomiczne niż SQL i materialized views.
  - Transakcje wielodokumentowe są dostępne, ale bardziej kosztowne i rzadziej potrzebne przy modelu „wszystko w sesji”.

➡ Rekomendacja: zacząć z PostgreSQL. MongoDB można rozważyć jako alternatywę lub do specyficznych funkcji (np. feed aktywności), gdy priorytetem jest elastyczność schematu, a nie ścisła integralność relacyjna.

## Warstwa statystyk i wydajność
- Agregacje „na żądanie” dla małych wolumenów; dla rosnącej skali: materialized views (PostgreSQL) lub pre‑agregowane snapshoty w Redis.
- Cache’owanie per użytkownik/zakres dat; invalidacja na: utworzenie/edycja/usunięcie wyniku w sesji.
- Potencjalne joby asynchroniczne: przeliczenia leaderboardów (Spring Scheduling) – niekoniecznie w pierwszej iteracji.

## Integracje dodatkowe
- Przechowywanie avatarów: S3‑compatible (np. MinIO w dev, S3 w prod). W modelu trzymamy tylko URL.
- Eksport danych: CSV/JSON endpointy; generacja strumieniowa.

## CI/CD i infrastruktura
- Konteneryzacja: Docker; środowisko dev: Docker Compose (backend, frontend, Postgres, Redis). Alternatywnie: Compose z MongoDB, jeśli wybierzesz Mongo.
- CI: GitHub Actions (build + test backend/frontend, skany, obraz Dockera).
- Monitoring: Prometheus + Grafana; logi do ELK/Loki; tracing: OpenTelemetry.
- Konfiguracja: Spring Boot profiles + `.env` dla Compose; secrets w CI.

## Testowanie
- Backend: testy jednostkowe i integracyjne (Testcontainers dla Postgres/Redis lub Mongo). Testy walidacji integralności (sumy, wartości >= 0), uprawnień ról.
- Frontend: testy jednostkowe komponentów, krytyczne ścieżki E2E (dodawanie sesji, przegląd statystyk, leaderboardy).

## Minimalny MVP – decyzje startowe (proponowane)
- Frontend: React + Vite + Tailwind + TanStack Query + Recharts.
- Backend: Kotlin + Spring Boot (Web, Security, Validation, Data JPA, Cache, Actuator, OpenAPI).
- Baza: PostgreSQL + Flyway; Redis do cache.
- Autoryzacja: JWT (httpOnly), role `admin`/`casual_player`.
- Hosting: obrazy Dockera; monitoring podstawowy (Actuator + Prometheus).

## Podsumowanie odpowiedzi o MongoDB
- **Tak – MongoDB jest możliwe.**
- **Rekomendacja: PostgreSQL** ze względu na integralność finansów, relacje i bogate agregacje pod statystyki/leaderboardy. MongoDB można zastosować, jeżeli priorytetem jest elastyczny schemat i prosta atomowość w obrębie pojedynczych sesji.