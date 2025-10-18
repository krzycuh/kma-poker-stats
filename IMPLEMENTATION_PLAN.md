# Poker Stats Web App - Implementation Plan

## Executive Summary

This implementation plan breaks down the development of a home game poker statistics tracking platform into manageable phases. The app will serve casual poker groups to log sessions, track performance, and view leaderboards with two user roles: Casual Player and Admin.

**Tech Stack:**
- **Frontend:** React 18 + TypeScript + Vite + Tailwind CSS + TanStack Query + Recharts
- **Backend:** Kotlin + Spring Boot 3.x + Spring Security (JWT)
- **Database:** PostgreSQL + Redis (caching)
- **Infrastructure:** Docker + Docker Compose + GitHub Actions

**Timeline Estimate:** 10-14 weeks for MVP (Phase 1-8)

---

## Development Principles & Best Practices

This implementation plan is guided by industry-leading software development practices:

### 🎯 Lean Startup Methodology

**Build-Measure-Learn Cycles:**
- Each phase ends with working software that can be demonstrated
- User feedback collected at phase boundaries (especially Phase 4, 7, 8)
- Pivot opportunities identified after Phase 4 (dashboard) and Phase 7 (UAT)
- Focus on validated learning over feature completion

**Minimum Viable Product (MVP) First:**
- **Core MVP:** Phases 1-5 (Auth + Game Logging + Basic Stats)
- **Enhanced MVP:** Phases 6-7 (Polish + Testing) 
- **Launch:** Phase 8 (Deployment)
- **Future:** Phase 9+ (Deferred features based on user feedback)
- Every feature must answer: "Does this validate a core hypothesis?"

**Early User Validation:**
- Phase 4: Show dashboard to 2-3 users, gather feedback
- Phase 5: Demo stats/leaderboards, validate usefulness
- Phase 7: Formal UAT with real poker group
- Post-launch: Weekly metrics review and user interviews

**Hypotheses to Validate:**
- H1: Players want to track their poker performance digitally
- H2: Visual leaderboards increase engagement
- H3: Admins will log games within 24 hours of playing
- H4: Mobile-first design is critical for adoption

### 🏗️ Domain-Driven Design (DDD)

**Bounded Contexts:**
```
┌─────────────────────┐  ┌─────────────────────┐  ┌─────────────────────┐
│  Identity & Access  │  │   Game Management   │  │   Statistics        │
│  Context            │  │   Context           │  │   Context           │
│                     │  │                     │  │                     │
│  - User             │  │  - GameSession      │  │  - PlayerStats      │
│  - Authentication   │  │  - Player           │  │  - Leaderboard      │
│  - Authorization    │  │  - SessionResult    │  │  - Achievement      │
└─────────────────────┘  └─────────────────────┘  └─────────────────────┘
```

**Domain Model Structure:**
```kotlin
// Domain Layer (business logic, framework-independent)
domain/
├── model/              # Entities, Value Objects, Aggregates
│   ├── GameSession.kt          # Aggregate Root
│   ├── SessionResult.kt        # Entity
│   ├── Player.kt               # Aggregate Root
│   ├── Money.kt                # Value Object
│   └── GameType.kt             # Value Object (enum)
├── repository/         # Repository interfaces (ports)
├── service/            # Domain Services
│   ├── GameSessionService.kt   # Business logic
│   └── StatsCalculator.kt      # Pure calculation logic
└── event/              # Domain Events
    ├── SessionCreated.kt
    └── SessionUpdated.kt

// Application Layer (orchestration, use cases)
application/
├── usecase/
│   ├── CreateGameSession.kt    # Use case / Command Handler
│   ├── CalculatePlayerStats.kt
│   └── GenerateLeaderboard.kt
└── dto/                        # Data Transfer Objects

// Infrastructure Layer (technical details)
infrastructure/
├── persistence/
│   ├── JpaGameSessionRepository.kt  # Repository implementation
│   └── entity/                      # JPA entities (separate from domain!)
├── api/
│   └── rest/                        # Controllers
└── cache/
    └── RedisStatsCache.kt
```

**Key DDD Patterns:**
- ✅ **Aggregates:** GameSession is aggregate root containing SessionResults
- ✅ **Value Objects:** Money, GameType, Location (immutable, no identity)
- ✅ **Repository Pattern:** Abstract data access behind interfaces
- ✅ **Domain Services:** Complex business logic that doesn't fit in entities
- ✅ **Domain Events:** Publish events when sessions created/updated
- ✅ **Ubiquitous Language:** Use poker terminology consistently (buy-in, cash-out, session)
- ✅ **Anti-Corruption Layer:** Separate JPA entities from domain entities

**Aggregate Invariants:**
```kotlin
// Example: GameSession aggregate maintains its own invariants
class GameSession(
    val id: GameSessionId,
    val startTime: LocalDateTime,
    val location: Location,
    private val results: MutableList<SessionResult> = mutableListOf()
) {
    fun addResult(result: SessionResult) {
        require(results.size < 2) { "Session must have at least 2 players" }
        require(result.buyIn >= Money.ZERO) { "Buy-in must be non-negative" }
        results.add(result)
        // Publish domain event
        DomainEvents.raise(SessionResultAdded(id, result))
    }
    
    fun getTotalBuyIns(): Money = results.sumOf { it.buyIn }
    fun getTotalCashOuts(): Money = results.sumOf { it.cashOut }
    fun isBalanced(): Boolean = getTotalBuyIns() == getTotalCashOuts()
}
```

### 🧹 Clean Code Principles

**SOLID Principles:**
- **S**ingle Responsibility: Each class has one reason to change
- **O**pen/Closed: Open for extension, closed for modification
- **L**iskov Substitution: Subtypes must be substitutable
- **I**nterface Segregation: Many client-specific interfaces
- **D**ependency Inversion: Depend on abstractions, not concretions

**Clean Code Practices:**
```kotlin
// ✅ GOOD: Descriptive names, small functions, single responsibility
class GameSessionService(
    private val sessionRepository: GameSessionRepository,
    private val eventPublisher: DomainEventPublisher
) {
    fun createSession(command: CreateSessionCommand): GameSession {
        val session = buildSessionFromCommand(command)
        validateSessionRules(session)
        val savedSession = sessionRepository.save(session)
        publishSessionCreatedEvent(savedSession)
        return savedSession
    }
    
    private fun validateSessionRules(session: GameSession) {
        require(session.hasMinimumPlayers()) { 
            "Session requires at least 2 players" 
        }
    }
}

// ❌ BAD: God class, unclear names, mixed concerns
class SessionManager {
    fun doEverything(data: Map<String, Any>): Any { ... }
}
```

**Code Quality Gates:**
- [ ] Functions < 20 lines (aim for < 10)
- [ ] Classes < 200 lines
- [ ] Cyclomatic complexity < 10
- [ ] Test coverage > 80%
- [ ] No commented-out code in commits
- [ ] Meaningful variable names (no `x`, `temp`, `data`)

### 🔬 Test-Driven Development (TDD)

**Red-Green-Refactor Cycle:**
```
1. 🔴 RED:    Write failing test
2. 🟢 GREEN:  Write minimal code to pass
3. 🔵 REFACTOR: Clean up code while tests pass
```

**Testing Strategy:**
```kotlin
// Unit Tests (domain layer - pure business logic)
class GameSessionTest {
    @Test
    fun `should calculate profit correctly`() {
        val result = SessionResult(
            buyIn = Money(100),
            cashOut = Money(150)
        )
        assertThat(result.profit()).isEqualTo(Money(50))
    }
}

// Integration Tests (application layer - use cases)
class CreateGameSessionUseCaseTest {
    @Test
    fun `should create session and publish event`() {
        // Given
        val command = CreateSessionCommand(...)
        
        // When
        val session = useCase.execute(command)
        
        // Then
        assertThat(session.id).isNotNull()
        verify(eventPublisher).publish(any<SessionCreated>())
    }
}

// API Tests (infrastructure layer - controllers)
@WebMvcTest(GameSessionController::class)
class GameSessionControllerTest {
    @Test
    fun `POST should return 201 Created`() {
        mockMvc.post("/api/sessions") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"date": "2025-01-01", ...}"""
        }.andExpect {
            status { isCreated() }
        }
    }
}
```

**Test Pyramid:**
- 70% Unit tests (fast, isolated, domain logic)
- 20% Integration tests (use cases, database)
- 10% E2E tests (critical user flows)

### 🔄 Continuous Practices

**Continuous Integration:**
- All tests run on every commit
- Build must pass before merge
- Code coverage tracked and reported
- Static analysis (linting, security scans)

**Continuous Refactoring:**
- Improve code with every commit (Boy Scout Rule)
- Dedicate 20% of sprint time to technical debt
- Regular architecture reviews
- No "we'll fix it later" without a ticket

**Code Review Process:**
- Every PR requires 1 approval
- Checklist: tests, documentation, SOLID, naming
- Review for business logic correctness
- Review for security concerns

### 📦 Other Best Practices

**YAGNI (You Aren't Gonna Need It):**
- Don't build features for "future" scenarios
- Defer Phase 9 features until validated
- Remove speculative abstractions
- Start simple, add complexity only when needed

**KISS (Keep It Simple, Stupid):**
- Prefer simple solutions over clever ones
- Avoid premature optimization
- Use standard patterns, not custom frameworks
- Write code humans can read

**Trunk-Based Development:**
- Short-lived feature branches (< 2 days)
- Integrate to main frequently
- Use feature flags for incomplete features
- No long-running development branches

**Feature Flags:**
```kotlin
// Example: Gradually roll out achievements feature
if (featureFlags.isEnabled("achievements")) {
    displayAchievements(player)
}
```

**Architecture Decision Records (ADRs):**
- Document important decisions (Why PostgreSQL? Why JWT?)
- Template: Context, Decision, Consequences
- Store in `docs/adr/` folder
- Review quarterly

---

## Phase 0: Project Setup & Infrastructure (Week 1)

### 0.1 Repository & Development Environment

**Tasks:**
- [ ] Initialize monorepo structure with frontend and backend folders
- [ ] Set up Git repository with branch protection rules
- [ ] Configure `.gitignore` for both frontend and backend
- [ ] Create README.md with project overview and setup instructions
- [ ] Set up project management board (GitHub Projects or similar)

**Deliverables:**
```
poker-stats/
├── frontend/
├── backend/
├── docs/
├── .github/
│   └── workflows/
├── docker-compose.yml
└── README.md
```

### 0.2 Frontend Setup

**Tasks:**
- [ ] Initialize React + TypeScript project with Vite
- [ ] Configure Tailwind CSS with custom design tokens (colors, spacing, typography)
- [ ] Set up ESLint and Prettier with team conventions
- [ ] Configure path aliases (@components, @utils, @api, etc.)
- [ ] Install core dependencies:
  - react-router-dom
  - @tanstack/react-query
  - react-hook-form + zod
  - recharts
  - @headlessui/react
  - dayjs
- [ ] Set up Vitest and React Testing Library
- [ ] Configure environment variables (.env.development, .env.production)
- [ ] Create basic folder structure:
  ```
  frontend/src/
  ├── api/
  ├── components/
  │   ├── common/
  │   ├── layout/
  │   └── features/
  ├── hooks/
  ├── pages/
  ├── types/
  ├── utils/
  ├── store/
  └── styles/
  ```

**Deliverables:**
- Working React development server
- Basic routing structure
- Shared components library stub
- Build and test scripts configured

### 0.3 Backend Setup (DDD + Clean Architecture)

**Tasks:**
- [ ] Initialize Spring Boot 3.x project with Kotlin
- [ ] Configure Gradle with Kotlin DSL
- [ ] Set up DDD-aligned project structure:
  ```
  backend/src/main/kotlin/com/pokerstats/
  ├── domain/                    # Core business logic (framework-independent)
  │   ├── model/                 # Entities, Value Objects, Aggregates
  │   │   ├── gamesession/
  │   │   │   ├── GameSession.kt           # Aggregate Root
  │   │   │   ├── SessionResult.kt         # Entity
  │   │   │   ├── GameType.kt              # Value Object
  │   │   │   └── Location.kt              # Value Object
  │   │   ├── player/
  │   │   │   └── Player.kt                # Aggregate Root
  │   │   └── shared/
  │   │       └── Money.kt                 # Shared Value Object
  │   ├── repository/            # Repository interfaces (ports)
  │   │   ├── GameSessionRepository.kt
  │   │   └── PlayerRepository.kt
  │   ├── service/               # Domain Services
  │   │   ├── StatsCalculator.kt
  │   │   └── LeaderboardGenerator.kt
  │   └── event/                 # Domain Events
  │       ├── SessionCreated.kt
  │       └── SessionUpdated.kt
  │
  ├── application/               # Use cases, orchestration
  │   ├── usecase/
  │   │   ├── session/
  │   │   │   ├── CreateGameSession.kt
  │   │   │   └── UpdateGameSession.kt
  │   │   └── stats/
  │   │       ├── CalculatePlayerStats.kt
  │   │       └── GenerateLeaderboard.kt
  │   └── dto/                   # Application DTOs
  │
  ├── infrastructure/            # Technical details
  │   ├── persistence/
  │   │   ├── entity/            # JPA entities (separate from domain!)
  │   │   ├── mapper/            # Domain <-> JPA mappers
  │   │   └── JpaGameSessionRepositoryImpl.kt
  │   ├── cache/
  │   │   └── RedisStatsCache.kt
  │   ├── api/
  │   │   └── rest/
  │   │       ├── controller/
  │   │       └── dto/           # API request/response DTOs
  │   └── security/
  │
  └── config/                    # Spring configuration
  ```
- [ ] Add dependencies:
  - Spring Web
  - Spring Data JPA
  - Spring Security
  - Spring Boot Actuator
  - springdoc-openapi (OpenAPI 3 docs)
  - Flyway
  - PostgreSQL driver
  - Redis (Lettuce)
  - Jackson Kotlin module
  - JWT library (jjwt)
  - Arrow-kt (functional programming for Kotlin)
- [ ] Configure application.yml for multiple profiles (dev, test, prod)
- [ ] Set up JUnit 5, MockK, Kotest, and Testcontainers
- [ ] Configure logging (JSON format for production)
- [ ] Set up ktlint for code style enforcement
- [ ] Configure ArchUnit for architecture testing (enforce DDD layers)

**Deliverables:**
- Working Spring Boot application with DDD structure
- Health check endpoint (/actuator/health)
- API documentation at /swagger-ui
- Test infrastructure ready (unit, integration, architecture tests)
- Sample domain model with tests demonstrating TDD

**DDD Setup Validation:**
- [ ] Architecture tests pass (ArchUnit verifies layer dependencies)
- [ ] Domain layer has no Spring dependencies
- [ ] Value objects are immutable
- [ ] Aggregates enforce invariants

### 0.4 Database Setup

**Tasks:**
- [ ] Create PostgreSQL database schema design document
- [ ] Set up Docker Compose with PostgreSQL and Redis
- [ ] Create initial Flyway migration (V1__initial_schema.sql):
  - users table
  - players table
  - game_sessions table
  - session_results table
  - Indexes and constraints
- [ ] Create database diagram/ER model
- [ ] Set up connection pooling (HikariCP)
- [ ] Configure Redis connection

**Deliverables:**
- Docker Compose running PostgreSQL (port 5432) and Redis (port 6379)
- Initial database schema created
- Sample seed data script for development

### 0.5 CI/CD Pipeline

**Tasks:**
- [ ] Create GitHub Actions workflow for frontend:
  - Lint and format check
  - Unit tests
  - Build production bundle
  - Type checking
- [ ] Create GitHub Actions workflow for backend:
  - Kotlin lint (ktlint)
  - Unit and integration tests with Testcontainers
  - Build JAR
  - Security scans
- [ ] Set up Docker image builds
- [ ] Configure secrets management
- [ ] Set up code coverage reporting (Codecov or similar)

**Deliverables:**
- Automated CI pipeline running on pull requests
- Docker images published to registry
- Code coverage reports

---

## Development Workflow (Applied to All Phases)

### Daily Development Cycle

**For Each Feature:**
1. **📋 Understand:** Review user story and acceptance criteria
2. **🎨 Design:** Sketch domain model and API contract (5-10 min)
3. **🔴 Test First:** Write failing test (TDD)
4. **🟢 Implement:** Write minimal code to pass test
5. **🔵 Refactor:** Clean up while maintaining green tests
6. **📝 Document:** Update API docs, add code comments for complex logic
7. **👀 Review:** Submit PR with tests and documentation
8. **🚀 Deploy:** Merge to main, deploy to staging

### Code Review Checklist

**Before Submitting PR:**
- [ ] All tests pass (unit, integration, E2E where applicable)
- [ ] Code follows SOLID principles
- [ ] No code smells (long functions, god classes, magic numbers)
- [ ] Domain logic is framework-independent
- [ ] Meaningful variable/function names
- [ ] No commented-out code
- [ ] Updated API documentation (OpenAPI)

**Reviewer Checks:**
- [ ] Business logic correctness
- [ ] Test coverage adequate
- [ ] Security concerns addressed
- [ ] Performance implications considered
- [ ] DDD patterns followed correctly

### Weekly Rhythm

**Monday:**
- Sprint planning (if using sprints)
- Review last week's metrics
- Prioritize tasks for the week

**Wednesday:**
- Mid-week check-in
- Pair programming session
- Address blockers

**Friday:**
- Demo completed features
- Retrospective (what went well, what to improve)
- Deploy to staging
- Plan next week

### Lean Validation Checkpoints

**After Phase 1:** ✋ VALIDATE
- Demo auth flow to 1-2 potential users
- Is the login process smooth?
- Adjust UX if needed before building more

**After Phase 3:** ✋ VALIDATE  
- Demo game logging to admin users
- Can they log a game in < 5 minutes?
- Is the multi-step flow intuitive?
- Gather feedback, iterate if needed

**After Phase 4:** ✋ VALIDATE  
- Demo dashboard to casual players
- Do they understand their stats?
- Is the information useful?
- **PIVOT DECISION POINT:** Are we building the right thing?

**After Phase 5:** ✋ VALIDATE  
- Demo leaderboards and charts
- Does this increase engagement?
- What metrics matter most to users?

**After Phase 7:** ✋ VALIDATE  
- Full UAT with real poker group
- Observe real usage for 1-2 weeks
- Measure: adoption rate, errors, time to log game
- Decide: Launch vs iterate more

### Definition of Done (DoD)

**For Each Task:**
- [ ] Code written following clean code principles
- [ ] Unit tests written and passing (TDD)
- [ ] Integration tests for happy path and edge cases
- [ ] Code reviewed and approved
- [ ] No linter warnings
- [ ] Documentation updated
- [ ] Deployed to dev/staging environment

**For Each Phase:**
- [ ] All tasks completed with DoD met
- [ ] E2E tests for critical flows passing
- [ ] API documentation up-to-date
- [ ] Demo prepared and presented
- [ ] User feedback collected (where applicable)
- [ ] Retrospective completed
- [ ] Decision made: proceed, pivot, or iterate

---

## Phase 1: Authentication & User Management (Week 2-3)

> **Lean Hypothesis:** Users need secure login and profile management.  
> **Validation Method:** Demo to 2-3 users, measure time to complete registration.  
> **Success Criteria:** Registration takes < 2 minutes, no confusion.

### Development Approach
- **TDD:** Write tests first for all auth logic
- **DDD:** User is an Aggregate Root in Identity & Access bounded context
- **Clean Code:** Separate domain (User entity) from infrastructure (JWT, password hashing)

### 1.1 Backend: Authentication Infrastructure

**Tasks:**
- [ ] Implement User entity and repository
- [ ] Create JWT token generation and validation service
- [ ] Implement Spring Security configuration:
  - JWT filter
  - Role-based authorization (CASUAL_PLAYER, ADMIN)
  - CORS configuration
  - Password encoder (BCrypt/Argon2)
- [ ] Create authentication endpoints:
  - POST /api/auth/register
  - POST /api/auth/login
  - POST /api/auth/refresh
  - POST /api/auth/logout
- [ ] Implement password validation rules
- [ ] Create user profile endpoints:
  - GET /api/users/me
  - PUT /api/users/me
  - PATCH /api/users/me/password
- [ ] Add comprehensive tests for auth flows

**Deliverables:**
- JWT-based authentication working
- User registration and login endpoints
- Password hashing and validation
- 401/403 error handling

### 1.2 Frontend: Authentication UI

**Tasks:**
- [ ] Create authentication context/provider
- [ ] Implement login page with form validation (React Hook Form + Zod)
- [ ] Implement registration page
- [ ] Create protected route wrapper component
- [ ] Implement token storage (httpOnly cookies or localStorage)
- [ ] Add axios/fetch interceptor for JWT attachment
- [ ] Handle token refresh logic
- [ ] Create error boundary for auth errors
- [ ] Implement "Remember me" functionality
- [ ] Add password strength indicator
- [ ] Create "Forgot password" UI (stub for now)

**Deliverables:**
- Login and registration pages fully functional
- Protected routes working
- Token refresh automatic
- Error handling for auth failures

### 1.3 Profile Management

**Tasks:**
- [ ] Backend: Avatar upload endpoint (POST /api/users/me/avatar)
- [ ] Backend: File storage service (LocalStorageService for Raspberry Pi)
- [ ] Frontend: Profile page UI (from wireframes)
- [ ] Frontend: Avatar upload component with preview
- [ ] Frontend: Profile edit form
- [ ] Frontend: Password change form
- [ ] Add file size and type validation
- [ ] Image compression/resizing

**Deliverables:**
- Users can view and edit their profile
- Avatar upload working
- Password change functional

---

## Phase 2: Core Data Models & Player Management (Week 3-4)

> **Lean Hypothesis:** Admins need to manage players separately from user accounts.  
> **Validation Method:** Show player management to admin, observe workflow.  
> **Success Criteria:** Can add/edit player in < 30 seconds.

### Development Approach
- **DDD Focus:** 
  - Player is Aggregate Root
  - SessionResult is Entity within GameSession aggregate
  - Money is Value Object (immutable, business logic for currency)
- **TDD:** Write domain tests first (e.g., profit calculation)
- **Clean Architecture:** Domain models have zero Spring dependencies

### 2.1 Backend: Player Entity & Management

**Tasks:**
- [ ] Create Player entity and repository
- [ ] Implement player CRUD endpoints (admin only):
  - GET /api/players (list all)
  - GET /api/players/{id}
  - POST /api/players (create)
  - PUT /api/players/{id} (update)
  - DELETE /api/players/{id} (soft delete)
- [ ] Link Player to User (nullable user_id foreign key)
- [ ] Create player search/filter endpoint
- [ ] Add player avatar upload
- [ ] Implement validation (unique names, etc.)
- [ ] Add pagination for player list

**Deliverables:**
- Player CRUD operations working
- Player-User linking functional
- Admin-only access enforced

### 2.2 Backend: Game Session Entity

**Tasks:**
- [ ] Create GameSession entity and repository
- [ ] Implement session CRUD endpoints:
  - GET /api/sessions (list with filters)
  - GET /api/sessions/{id}
  - POST /api/sessions (admin only)
  - PUT /api/sessions/{id} (admin only)
  - DELETE /api/sessions/{id} (admin only, soft delete)
- [ ] Add filtering by date range, location, player
- [ ] Add sorting options
- [ ] Implement permission checks (users see only their sessions)
- [ ] Add session validation rules

**Deliverables:**
- Game session CRUD functional
- Filtering and sorting working
- Permission model enforced

### 2.3 Backend: Session Results

**Tasks:**
- [ ] Create SessionResult entity and repository
- [ ] Implement result endpoints:
  - GET /api/sessions/{id}/results
  - POST /api/sessions/{id}/results (batch)
  - PUT /api/results/{id}
  - DELETE /api/results/{id}
- [ ] Add validation:
  - Non-negative amounts
  - Buy-in/cash-out balance check (warning, not blocking)
  - At least 2 players per session
- [ ] Calculate profit/loss automatically
- [ ] Add transaction management for session + results

**Deliverables:**
- Session results CRUD working
- Validation rules enforced
- Transactional integrity maintained

### 2.4 Frontend: Player Management (Admin)

**Tasks:**
- [ ] Create player list page (admin only)
- [ ] Create add/edit player form
- [ ] Implement player search
- [ ] Add player cards with avatar display
- [ ] Create player detail modal
- [ ] Implement delete confirmation modal

**Deliverables:**
- Admin can manage players
- Player list searchable and paginated
- CRUD operations functional

---

## Phase 3: Game Logging (Admin Feature) (Week 5-6)

> **Lean Hypothesis:** Admins will log games immediately after playing if the process is < 5 minutes.  
> **Validation Method:** Time admin users logging real game data.  
> **Success Criteria:** Complete game logging in < 5 minutes, < 2 errors.

### Development Approach
- **DDD Focus:**
  - GameSession is Aggregate Root
  - SessionResult is part of GameSession aggregate
  - Enforce invariants: minimum 2 players, non-negative amounts
  - Publish domain event: SessionCreated
- **TDD:** Test aggregate invariants, test use case orchestration
- **Clean Code:** Multi-step form components < 200 lines each

### Key Domain Logic (TDD Example)

```kotlin
// Test First (Red)
@Test
fun `should reject session with less than 2 players`() {
    val session = GameSession(...)
    
    assertThrows<IllegalArgumentException> {
        session.addResult(SessionResult(...))
        // Only 1 player - should fail
        session.validate()
    }
}

// Then Implement (Green)
class GameSession {
    fun validate() {
        require(results.size >= 2) { 
            "Session must have at least 2 players" 
        }
    }
}

// Then Refactor (Blue)
// Extract to domain service if logic becomes complex
```

### 3.1 Multi-Step Form: Session Details

**Tasks:**
- [ ] Create multi-step form wrapper with progress indicator
- [ ] Implement Step 1: Session details form
  - Date/time picker (default: now)
  - Location dropdown with "Add new" option
  - Game type dropdown
  - Min buy-in currency input
- [ ] Add form state management (React Hook Form)
- [ ] Implement draft saving (localStorage or backend)
- [ ] Add validation for each step
- [ ] Create "Duplicate last session" feature

**Deliverables:**
- Step 1 of game logging form working
- Validation functional
- Draft auto-save working

### 3.2 Multi-Step Form: Player Selection

**Tasks:**
- [ ] Create Step 2: Player selection UI
- [ ] Implement multi-select with checkboxes
- [ ] Add player search/filter
- [ ] Show selected player count
- [ ] Add "Add new player" quick action
- [ ] Implement minimum 2 players validation

**Deliverables:**
- Player selection step functional
- Search and filter working
- Validation enforced

### 3.3 Multi-Step Form: Results Entry

**Tasks:**
- [ ] Create Step 3: Results entry for each player
- [ ] Implement swipe/pagination between players
- [ ] Create large currency input fields
- [ ] Add auto-calculated profit/loss display
- [ ] Implement optional notes field
- [ ] Add summary panel (sticky):
  - Total buy-ins
  - Total cash-outs
  - Discrepancy warning
- [ ] Add session-level notes
- [ ] Implement numeric keyboard on mobile

**Deliverables:**
- Results entry form working
- Real-time calculations functional
- Discrepancy warnings shown

### 3.4 Multi-Step Form: Review & Submit

**Tasks:**
- [ ] Create Step 4: Review and confirmation page
- [ ] Display summary of all entered data
- [ ] Add "Edit" buttons to go back to specific steps
- [ ] Implement submit action
- [ ] Show success confirmation screen
- [ ] Add quick actions:
  - View session
  - Log another session
  - Back to dashboard

**Deliverables:**
- Complete game logging flow functional
- Session creation working end-to-end
- Success feedback provided

### 3.5 Session Editing (Admin)

**Tasks:**
- [ ] Add edit button on session detail page (admin only)
- [ ] Pre-populate form with existing data
- [ ] Implement update logic
- [ ] Add edit history tracking (optional)
- [ ] Show "Last edited" timestamp

**Deliverables:**
- Admins can edit existing sessions
- Changes saved correctly
- Edit history visible

---

## Phase 4: Dashboard & Session Views (Week 7-8)

> **Lean Hypothesis:** Players want to see their performance at a glance on the dashboard.  
> **Validation Method:** Show dashboard to 3-5 casual players, ask "what does this tell you?"  
> **Success Criteria:** 80%+ understand their stats without explanation.  
> **🚨 PIVOT DECISION POINT:** If users don't find dashboard useful, reconsider what stats to show.

### Development Approach
- **DDD Focus:**
  - StatsCalculator is Domain Service (complex calculation logic)
  - PlayerStats is computed, not stored (derived from SessionResults)
- **TDD:** Test stats calculations with various scenarios (winning streak, losing streak, breakeven)
- **Performance:** Cache calculated stats in Redis, invalidate on new session

### 4.1 Dashboard for Casual Players

**Tasks:**
- [ ] Backend: Create stats calculation service
- [ ] Backend: Implement dashboard endpoint (GET /api/dashboard)
  - Personal overall stats
  - Recent sessions (last 5-10)
  - Current leaderboard position
- [ ] Frontend: Create dashboard layout from wireframes
- [ ] Frontend: Stats summary card component
- [ ] Frontend: Recent sessions list component
- [ ] Frontend: Leaderboard position badge
- [ ] Add trend indicators (up/down arrows)
- [ ] Implement empty state for new users
- [ ] Add loading skeletons

**Deliverables:**
- Dashboard showing personal stats
- Recent sessions displayed
- Loading and empty states

### 4.2 Dashboard for Admins

**Tasks:**
- [ ] Backend: Add admin dashboard stats endpoint
  - Total sessions count
  - Active players count
  - Recent activity
- [ ] Frontend: Add admin-specific sections to dashboard
- [ ] Add floating action button (FAB) for "Log New Session"
- [ ] Display system-wide stats

**Deliverables:**
- Admin dashboard with additional features
- FAB for quick game logging
- System stats visible

### 4.3 Session History / Timeline

**Tasks:**
- [ ] Backend: Enhance session list endpoint with advanced filters
- [ ] Frontend: Create session history page from wireframes
- [ ] Implement filters:
  - Date range picker
  - Location multi-select
  - Player multi-select
  - Min profit/loss threshold
- [ ] Add sort options (date, profit, session size)
- [ ] Create session card component
- [ ] Group sessions by month
- [ ] Add infinite scroll or pagination
- [ ] Implement search functionality

**Deliverables:**
- Session history page functional
- Filters and sorting working
- Performance optimized for large lists

### 4.4 Session Detail View

**Tasks:**
- [ ] Frontend: Create session detail page from wireframes
- [ ] Display session metadata
- [ ] Show all player results in sortable table
- [ ] Highlight current user's result
- [ ] Display session notes
- [ ] Show summary panel with totals
- [ ] Add edit button (admin only)
- [ ] Implement share functionality (future: screenshot/link)
- [ ] Add delete action with confirmation (admin only)

**Deliverables:**
- Session detail page complete
- All session data visible
- Admin actions available

---

## Phase 5: Statistics & Analytics (Week 9-10)

> **Lean Hypothesis:** Leaderboards and charts will increase user engagement and friendly competition.  
> **Validation Method:** Track dashboard views before/after, survey users about competitiveness.  
> **Success Criteria:** 70%+ users check leaderboard at least once per week.

### Development Approach
- **DDD Focus:**
  - LeaderboardGenerator is Domain Service
  - Leaderboard is Value Object (immutable snapshot)
  - Consider: Domain events for achievement unlocks (future)
- **TDD:** Test leaderboard ranking logic with edge cases (ties, new players)
- **Performance:** Pre-calculate and cache leaderboards, recalculate on session create/update
- **YAGNI:** Defer achievement system to Phase 9 unless users explicitly ask for it

### Refactoring Checkpoint

**After Phase 5, allocate 2-3 days for:**
- [ ] Refactor duplicate code
- [ ] Improve test coverage to 80%+
- [ ] Address technical debt tickets
- [ ] Update architecture documentation
- [ ] Performance optimization based on metrics

### 5.1 Backend: Stats Calculation Engine

**Tasks:**
- [ ] Create StatsService with methods for calculating:
  - Total sessions played
  - Total buy-in, cash-out, net profit/loss
  - ROI percentage
  - Win rate (sessions with profit / total sessions)
  - Biggest win/loss
  - Average session profit
  - Current streak
- [ ] Implement caching layer (Redis) for stats
- [ ] Create cache invalidation strategy
- [ ] Add stats endpoints:
  - GET /api/stats/personal
  - GET /api/stats/personal/history (time-series data)
  - GET /api/stats/personal/by-location
  - GET /api/stats/personal/by-day-of-week
- [ ] Optimize database queries (use aggregations, indexes)
- [ ] Add date range filtering for stats

**Deliverables:**
- Stats calculation service working
- Caching implemented
- Performance optimized

### 5.2 Frontend: Personal Stats Page

**Tasks:**
- [ ] Create stats overview page from wireframes
- [ ] Implement metric cards (total sessions, net profit, ROI, win rate)
- [ ] Add chart components:
  - Profit/loss over time (line chart)
  - Win/loss distribution (pie chart)
  - Performance by location (bar chart)
  - Cumulative profit curve
- [ ] Create best/worst sessions section
- [ ] Implement achievements/badges section (stub for now)
- [ ] Add date range selector for charts
- [ ] Make charts responsive and interactive
- [ ] Add export stats button (CSV)

**Deliverables:**
- Personal stats page complete
- Charts rendering correctly
- Interactive elements working

### 5.3 Backend: Leaderboard System

**Tasks:**
- [ ] Create leaderboard endpoints:
  - GET /api/leaderboards/profit
  - GET /api/leaderboards/roi
  - GET /api/leaderboards/win-rate
  - GET /api/leaderboards/streak
- [ ] Implement filtering (only show players from shared sessions)
- [ ] Add caching for leaderboards
- [ ] Calculate current user's position
- [ ] Add pagination/top N results

**Deliverables:**
- Leaderboard calculations working
- Multiple metric types supported
- User position calculated

### 5.4 Frontend: Leaderboard Page

**Tasks:**
- [ ] Create leaderboard page from wireframes
- [ ] Implement metric selector (toggle between metrics)
- [ ] Create podium section for top 3 (special styling)
- [ ] Display rest of leaderboard as list
- [ ] Highlight current user's position
- [ ] Add rank indicators (medals, badges)
- [ ] Implement "Load more" or infinite scroll
- [ ] Add player profile link (view public stats)

**Deliverables:**
- Leaderboard page functional
- Metric switching working
- User position highlighted

---

## Phase 6: Polish & Mobile Optimization (Week 11-12)

### 6.1 Mobile Responsiveness

**Tasks:**
- [ ] Audit all pages on mobile devices (320px - 767px)
- [ ] Ensure touch targets are minimum 44px
- [ ] Test navigation on mobile (bottom nav, swipe gestures)
- [ ] Optimize forms for mobile (large inputs, numeric keyboards)
- [ ] Test charts on small screens (make scrollable if needed)
- [ ] Implement responsive tables (card view on mobile)
- [ ] Add pull-to-refresh on list views
- [ ] Test offline caching (TanStack Query)

**Deliverables:**
- All pages mobile-optimized
- Touch interactions working smoothly
- Offline view of cached data working

### 6.2 UI/UX Improvements

**Tasks:**
- [ ] Implement all empty states from wireframes
- [ ] Add loading skeletons for all async operations
- [ ] Create consistent error handling and display
- [ ] Add success/error toast notifications
- [ ] Implement micro-interactions:
  - Button press animations
  - Profit/loss color coding
  - Card hover effects (desktop)
  - Page transitions
- [ ] Add form validation error messages
- [ ] Implement confirmation modals for destructive actions
- [ ] Add tooltips for complex features

**Deliverables:**
- Consistent UX across all pages
- Animations and transitions polished
- Error handling user-friendly

### 6.3 Performance Optimization

**Tasks:**
- [ ] Backend: Add database query optimization
  - Review slow query log
  - Add missing indexes
  - Optimize N+1 queries
- [ ] Backend: Implement pagination for large lists
- [ ] Backend: Add rate limiting
- [ ] Frontend: Code splitting and lazy loading
- [ ] Frontend: Optimize bundle size
- [ ] Frontend: Image optimization (avatar compression)
- [ ] Frontend: Implement virtual scrolling for long lists
- [ ] Add performance monitoring (Actuator metrics, Prometheus)

**Deliverables:**
- App loading time < 3 seconds
- Smooth scrolling on large datasets
- API response times < 500ms (p95)

### 6.4 Accessibility

**Tasks:**
- [ ] Add proper ARIA labels to all interactive elements
- [ ] Ensure keyboard navigation works throughout app
- [ ] Test with screen reader
- [ ] Add focus indicators
- [ ] Ensure color contrast meets WCAG AA standards
- [ ] Add alt text to all images
- [ ] Make forms accessible
- [ ] Test with accessibility tools (Lighthouse, axe)

**Deliverables:**
- WCAG AA compliance
- Keyboard navigation functional
- Screen reader compatible

---

## Phase 7: Testing & Quality Assurance (Week 13)

### 7.1 Backend Testing

**Tasks:**
- [ ] Achieve 80%+ code coverage for backend
- [ ] Write integration tests for all API endpoints
- [ ] Test authentication and authorization flows
- [ ] Test data validation and constraints
- [ ] Test edge cases (empty data, invalid inputs)
- [ ] Test concurrent session edits
- [ ] Test caching behavior
- [ ] Load testing (simulate 100+ concurrent users)

**Deliverables:**
- Comprehensive test suite
- All critical paths covered
- Load test results documented

### 7.2 Frontend Testing

**Tasks:**
- [ ] Write unit tests for utility functions and hooks
- [ ] Write component tests for all major components
- [ ] Write E2E tests with Playwright:
  - User registration and login
  - Admin game logging flow
  - View personal stats
  - View leaderboard
  - Edit profile
- [ ] Test on multiple browsers (Chrome, Firefox, Safari)
- [ ] Test on multiple devices (phone, tablet, desktop)
- [ ] Test error scenarios
- [ ] Visual regression testing (optional)

**Deliverables:**
- E2E test suite covering critical flows
- Cross-browser compatibility verified
- Mobile device testing complete

### 7.3 Security Audit

**Tasks:**
- [ ] Review authentication implementation
- [ ] Test JWT token security (expiration, refresh, revocation)
- [ ] Test authorization (users can't access others' data)
- [ ] Check for SQL injection vulnerabilities
- [ ] Check for XSS vulnerabilities
- [ ] Test CORS configuration
- [ ] Review secrets management
- [ ] Test password security (hashing, strength requirements)
- [ ] Run security scanner (OWASP ZAP or similar)

**Deliverables:**
- Security vulnerabilities identified and fixed
- Security best practices documented

### 7.4 User Acceptance Testing

**Tasks:**
- [ ] Deploy to staging environment
- [ ] Create test accounts (admin and casual player)
- [ ] Prepare test script with scenarios
- [ ] Conduct UAT with 3-5 poker players
- [ ] Gather feedback on usability and features
- [ ] Document bugs and improvement suggestions
- [ ] Prioritize and fix critical issues

**Deliverables:**
- UAT feedback document
- Critical bugs fixed
- User-approved MVP

---

## Phase 8: Deployment & Launch (Week 14)

### 8.1 Production Environment Setup

**Tasks:**
- [ ] Set up Raspberry Pi production environment:
  - Mount external SSD for database and uploads
  - Install Docker and Docker Compose
  - Configure PostgreSQL with persistent volume
  - Configure Redis with persistent volume
  - Set up nginx as reverse proxy
  - Configure SSL/TLS certificates (Let's Encrypt)
- [ ] Create production Docker Compose file
- [ ] Set up environment variables and secrets
- [ ] Configure backup strategy:
  - Daily database backups to external storage
  - Backup rotation policy
- [ ] Set up monitoring:
  - Prometheus + Grafana
  - Health check endpoints
  - Log aggregation
  - Disk space alerts

**Deliverables:**
- Production environment ready
- SSL certificates configured
- Monitoring dashboards set up

### 8.2 Deployment Pipeline

**Tasks:**
- [ ] Create deployment script
- [ ] Set up GitHub Actions for CD:
  - Build Docker images on release tags
  - Push images to registry
  - Deploy to production (optional: manual approval)
- [ ] Create rollback procedure
- [ ] Document deployment process
- [ ] Test deployment on staging first

**Deliverables:**
- Automated deployment pipeline
- Rollback procedure documented
- Deployment runbook created

### 8.3 Data Migration & Seeding

**Tasks:**
- [ ] Prepare production database with Flyway migrations
- [ ] Create initial admin user account
- [ ] (Optional) Migrate any existing poker data
- [ ] Create sample/demo data for testing
- [ ] Verify data integrity after migration

**Deliverables:**
- Production database initialized
- Admin account created
- Sample data available

### 8.4 Launch Preparation

**Tasks:**
- [ ] Create user onboarding guide
- [ ] Write FAQ documentation
- [ ] Create admin guide for game logging
- [ ] Set up support channel (email or chat)
- [ ] Prepare announcement/invitation emails
- [ ] Create demo video or tutorial
- [ ] Final cross-browser and mobile testing

**Deliverables:**
- User documentation complete
- Support channel active
- Ready for user invitations

### 8.5 Post-Launch Monitoring

**Tasks:**
- [ ] Monitor application health (uptime, errors)
- [ ] Track performance metrics (response times, database load)
- [ ] Monitor disk space and resource usage on Raspberry Pi
- [ ] Set up alerting for critical issues
- [ ] Gather user feedback
- [ ] Create bug tracking process
- [ ] Plan first post-launch iteration

**Deliverables:**
- Monitoring and alerting active
- Feedback collection process in place
- Post-launch support plan

---

## Phase 9: Future Enhancements (Post-MVP)

### 9.1 Achievements & Gamification
- [ ] Implement badge system with rules
- [ ] Create achievement unlock notifications
- [ ] Add progress tracking for badges
- [ ] Design badge icons and UI

### 9.2 Social Features
- [ ] Session comments and reactions
- [ ] Share session results (screenshot/link)
- [ ] Player-to-player messaging (optional)
- [ ] Session invitations with RSVP

### 9.3 Advanced Analytics
- [ ] Player head-to-head statistics
- [ ] Performance by game type
- [ ] Session size vs. profitability analysis
- [ ] Variance calculations
- [ ] Predictive analytics (ML-based)

### 9.4 Additional Features
- [ ] Expense splitting (food, drinks)
- [ ] Tournament tracking mode
- [ ] Multi-table session support
- [ ] Mobile app (React Native)
- [ ] Push notifications
- [ ] Export reports (PDF)
- [ ] Dark mode toggle

### 9.5 Internationalization
- [ ] Add i18n support (react-i18next)
- [ ] Support multiple currencies
- [ ] Translate UI to other languages
- [ ] Locale-specific date/number formatting

---

## Risk Management

### Technical Risks

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Raspberry Pi performance issues | High | Medium | Load testing early; plan for cloud migration if needed |
| Data loss due to SD card failure | High | Medium | Use external SSD; implement automated backups |
| JWT security vulnerabilities | High | Low | Follow security best practices; regular audits |
| Mobile performance issues | Medium | Medium | Performance testing on real devices; optimization early |
| Database scaling issues | Medium | Low | Monitor query performance; add indexes proactively |

### Non-Technical Risks

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Users don't adopt the app | High | Medium | Involve users early in UAT; gather feedback frequently |
| Admin data entry errors | Medium | High | Add validation, warnings, and edit functionality |
| Scope creep | Medium | High | Strict phase management; defer enhancements to post-MVP |
| Timeline delays | Medium | Medium | Weekly progress reviews; adjust scope if needed |

---

## Success Metrics (MVP) - Lean Startup KPIs

### Validated Learning Metrics (Most Important!)

**Activation:**
- [ ] 80%+ of invited users complete registration
- [ ] Average time to first login: < 3 minutes

**Engagement:**
- [ ] 70%+ of admins log a game within 24 hours of playing
- [ ] Average game logging time: < 5 minutes
- [ ] 60%+ of casual players check dashboard weekly
- [ ] 50%+ of users check leaderboard weekly

**Retention:**
- [ ] Week 2 retention: 70%+ users return
- [ ] Week 4 retention: 50%+ users return
- [ ] Monthly active users (MAU) growing

**Value Delivered:**
- [ ] 80%+ users understand their stats without help
- [ ] 70%+ users report friendly competition increased
- [ ] < 5% error rate in data entry
- [ ] 80%+ user satisfaction score (NPS)

### Technical Health Metrics

**Performance:**
- [ ] API response time p95 < 500ms
- [ ] Frontend load time (LCP) < 3 seconds
- [ ] Time to Interactive (TTI) < 4 seconds

**Quality:**
- [ ] Test coverage: > 80% backend, > 70% frontend
- [ ] Code quality: Maintainability index > 70
- [ ] Zero critical security vulnerabilities
- [ ] Linter warnings: 0

**Reliability:**
- [ ] 99% uptime in first month
- [ ] Mean time to recovery (MTTR) < 1 hour
- [ ] Error rate < 0.1%

### Feature Completeness (MVP Scope)
- [ ] User authentication working
- [ ] Game logging flow complete (admin) - validated with real users
- [ ] Personal stats and dashboard functional - users understand without help
- [ ] Leaderboards accurate and up-to-date
- [ ] Session history browsing smooth
- [ ] Mobile experience optimized - tested on real devices

### Build-Measure-Learn Tracking

**Implement Tracking for:**
```typescript
// Frontend Analytics Events
analytics.track('user_registered', { method: 'email' })
analytics.track('game_logged', { players: 6, duration_seconds: 180 })
analytics.track('dashboard_viewed', { user_type: 'casual_player' })
analytics.track('leaderboard_viewed', { metric: 'profit' })
analytics.track('stats_viewed', { period: 'all_time' })

// Backend Metrics (Micrometer)
meterRegistry.counter("sessions.created").increment()
meterRegistry.timer("session.log.duration").record(duration)
```

**Weekly Review:**
- Are users engaging with key features?
- Which features are unused? (Consider removing)
- Where do users get stuck? (UX improvements)
- What's the next hypothesis to test?

---

## Team Roles & Responsibilities

### Full-Stack Developer (Primary)
- Backend API development (Kotlin/Spring Boot)
- Frontend UI development (React/TypeScript)
- Database design and migrations
- Integration testing
- Deployment and DevOps

### UI/UX Designer (if available)
- High-fidelity mockups
- Design system and component library
- User flow validation
- Usability testing

### QA/Tester (part-time or self)
- Test plan creation
- Manual testing
- E2E test development
- Bug tracking and regression testing

### Product Owner (self or stakeholder)
- Requirements validation
- Feature prioritization
- UAT coordination
- User feedback gathering

---

## Communication & Reporting

### Weekly Progress Updates
- Completed tasks from current phase
- Blockers and risks
- Plan for next week
- Updated timeline if needed

### Phase Reviews
- Demo of completed features
- Review against acceptance criteria
- Adjust plan for next phase
- Celebrate milestones! 🎉

### Documentation
- Keep technical documentation up-to-date
- Document architecture decisions (ADRs)
- Update API documentation automatically (OpenAPI)
- Maintain deployment runbook

---

## Best Practices Summary Checklist

### Before Starting Development ✅

- [ ] **Lean Startup:** Hypotheses documented for each phase
- [ ] **DDD:** Bounded contexts identified and documented
- [ ] **Clean Code:** Code style guide agreed upon (ESLint, ktlint configs)
- [ ] **TDD:** Test infrastructure set up
- [ ] **Git Workflow:** Branch strategy defined (trunk-based vs feature branches)
- [ ] **Definition of Done:** Team agreement on what "done" means

### During Development (Every Sprint) ✅

- [ ] **TDD:** Write tests before code (Red-Green-Refactor)
- [ ] **SOLID:** Review each class for single responsibility
- [ ] **YAGNI:** Challenge every "we might need this later"
- [ ] **Code Reviews:** Every PR reviewed for business logic and clean code
- [ ] **Refactoring:** Dedicate 20% time to improve existing code
- [ ] **Validation:** Demo to users at phase boundaries

### Before Each Deployment ✅

- [ ] **Tests:** All tests passing (unit, integration, E2E)
- [ ] **Coverage:** Code coverage meets targets (80%+)
- [ ] **Linting:** Zero linter errors/warnings
- [ ] **Security:** Security scan passed
- [ ] **Performance:** Load time and API response within targets
- [ ] **Documentation:** API docs and README updated

### After Launch (Continuous) ✅

- [ ] **Metrics:** Track key metrics weekly (engagement, performance, errors)
- [ ] **Feedback:** Collect user feedback continuously
- [ ] **Monitoring:** Review dashboards daily
- [ ] **Iteration:** Plan next improvements based on data
- [ ] **Technical Debt:** Address one tech debt item per sprint

---

## Anti-Patterns to Avoid 🚫

### DDD Anti-Patterns
- ❌ **Anemic Domain Model:** Entities with only getters/setters, no business logic
- ❌ **Fat Services:** All logic in services, domain models are just data containers
- ❌ **Leaking Abstractions:** Domain depending on infrastructure (e.g., JPA annotations on domain entities)
- ❌ **Transaction Script:** Procedural code instead of object-oriented domain model

### Clean Code Anti-Patterns
- ❌ **God Class:** One class doing everything
- ❌ **Long Methods:** Functions > 20 lines
- ❌ **Magic Numbers:** Hard-coded values without explanation
- ❌ **Unclear Names:** Variables named `data`, `temp`, `x`
- ❌ **Commented-Out Code:** Delete it, Git remembers
- ❌ **Deep Nesting:** More than 3 levels of if/for nesting

### Lean Startup Anti-Patterns
- ❌ **Building Without Validation:** Don't build Phase 9 before validating Phase 1-8
- ❌ **Analysis Paralysis:** Don't over-design before user feedback
- ❌ **Vanity Metrics:** Tracking totals instead of actionable metrics
- ❌ **Ignoring Feedback:** Users tell you something doesn't work, but you keep building

### Testing Anti-Patterns
- ❌ **No Tests:** "We'll add tests later" = never
- ❌ **Testing Implementation:** Testing private methods instead of behavior
- ❌ **Flaky Tests:** Tests that sometimes fail
- ❌ **Slow Tests:** Unit tests taking > 100ms each

---

## Conclusion

This implementation plan provides a comprehensive roadmap for building the Poker Stats Web App MVP following **industry-leading best practices**:

✅ **Domain-Driven Design (DDD):** Clean separation of domain, application, and infrastructure layers  
✅ **Lean Startup:** Build-Measure-Learn cycles with validation checkpoints  
✅ **MVP First:** Core features (Phases 1-5) before polish and enhancements  
✅ **Clean Code:** SOLID principles, meaningful names, small functions  
✅ **TDD:** Test-first development with comprehensive coverage  
✅ **Continuous Practices:** Integration, refactoring, delivery  

**Key Success Factors:**
1. **Start small:** MVP features only, defer everything else
2. **Test first:** TDD for all business logic
3. **Validate early:** Get user feedback after Phases 3, 4, 5, 7
4. **Refactor continuously:** Boy Scout Rule - leave code better than you found it
5. **Measure everything:** Track metrics to validate hypotheses
6. **Pivot if needed:** Be ready to change direction based on data

**Next Steps:**
1. ✅ Review this plan - validate DDD structure and lean hypotheses
2. 📋 Set up project board with all tasks and DoD checklist
3. 🎯 Document key hypotheses and success metrics
4. 🏗️ Begin Phase 0: Project setup with DDD structure
5. 📅 Schedule weekly demos and retrospectives
6. 🚀 Kick off development with TDD!

**Remember:** 
> "Make it work, make it right, make it fast" - Kent Beck  
> "The best architecture emerges from self-organizing teams" - Agile Manifesto  
> "If you can't measure it, you can't improve it" - Peter Drucker

---

## Appendix A: TDD Example Walkthrough

### Example: Implementing "Calculate Profit/Loss" Feature

**Step 1: Write the Test First (RED 🔴)**

```kotlin
// domain/model/gamesession/SessionResultTest.kt
class SessionResultTest {
    
    @Test
    fun `should calculate positive profit when cash out exceeds buy in`() {
        // Given
        val buyIn = Money.ofPLN(100)
        val cashOut = Money.ofPLN(150)
        
        // When
        val result = SessionResult(
            playerId = PlayerId("player-1"),
            buyIn = buyIn,
            cashOut = cashOut
        )
        
        // Then
        assertThat(result.profit()).isEqualTo(Money.ofPLN(50))
    }
    
    @Test
    fun `should calculate negative profit when cash out is less than buy in`() {
        // Given
        val buyIn = Money.ofPLN(100)
        val cashOut = Money.ofPLN(75)
        
        // When
        val result = SessionResult(
            playerId = PlayerId("player-1"),
            buyIn = buyIn,
            cashOut = cashOut
        )
        
        // Then
        assertThat(result.profit()).isEqualTo(Money.ofPLN(-25))
    }
    
    @Test
    fun `should not allow negative buy in`() {
        // When/Then
        assertThrows<IllegalArgumentException> {
            SessionResult(
                playerId = PlayerId("player-1"),
                buyIn = Money.ofPLN(-10),
                cashOut = Money.ofPLN(0)
            )
        }
    }
}
```

**Test fails** ❌ - Classes don't exist yet!

---

**Step 2: Write Minimal Code to Pass (GREEN 🟢)**

```kotlin
// domain/model/shared/Money.kt (Value Object)
@JvmInline
value class Money(val amount: Long) { // Store as cents to avoid floating point issues
    
    init {
        require(amount >= 0) { "Money amount must be non-negative" }
    }
    
    operator fun plus(other: Money) = Money(amount + other.amount)
    operator fun minus(other: Money) = Money(amount - other.amount)
    
    companion object {
        val ZERO = Money(0)
        fun ofPLN(pln: Int) = Money(pln.toLong() * 100) // Convert to cents
    }
}

// domain/model/gamesession/SessionResult.kt (Entity)
class SessionResult(
    val id: SessionResultId = SessionResultId.generate(),
    val playerId: PlayerId,
    val buyIn: Money,
    val cashOut: Money,
    val notes: String? = null
) {
    init {
        require(buyIn.amount >= 0) { "Buy-in must be non-negative" }
        require(cashOut.amount >= 0) { "Cash-out must be non-negative" }
    }
    
    fun profit(): Money = cashOut - buyIn
    
    fun isWinning() = profit().amount > 0
}
```

**Tests pass** ✅ - But code could be cleaner!

---

**Step 3: Refactor (BLUE 🔵)**

```kotlin
// Refactor: Extract validation to Money value object
@JvmInline
value class Money private constructor(val amountInCents: Long) {
    
    operator fun plus(other: Money) = Money(amountInCents + other.amountInCents)
    operator fun minus(other: Money) = Money(amountInCents - other.amountInCents)
    operator fun compareTo(other: Money) = amountInCents.compareTo(other.amountInCents)
    
    fun isPositive() = amountInCents > 0
    fun isNegative() = amountInCents < 0
    fun isZero() = amountInCents == 0L
    
    companion object {
        val ZERO = Money(0)
        
        fun ofPLN(pln: Int): Money {
            require(pln >= 0) { "Amount must be non-negative" }
            return Money(pln.toLong() * 100)
        }
        
        fun ofCents(cents: Long): Money {
            require(cents >= 0) { "Amount must be non-negative" }
            return Money(cents)
        }
    }
}

// Refactor: Simplify SessionResult validation
class SessionResult(
    val id: SessionResultId = SessionResultId.generate(),
    val playerId: PlayerId,
    val buyIn: Money,  // Money already guarantees non-negative
    val cashOut: Money, // Money already guarantees non-negative
    val notes: String? = null
) {
    fun profit(): Money = cashOut - buyIn
    fun isWinning() = profit().isPositive()
    fun isBreakEven() = profit().isZero()
}
```

**Tests still pass** ✅ - Code is now cleaner and more expressive!

---

### Example: Use Case with TDD

**Step 1: Write Integration Test (RED 🔴)**

```kotlin
// application/usecase/session/CreateGameSessionTest.kt
@ExtendWith(MockKExtension::class)
class CreateGameSessionTest {
    
    @MockK
    private lateinit var sessionRepository: GameSessionRepository
    
    @MockK
    private lateinit var playerRepository: PlayerRepository
    
    @MockK
    private lateinit var eventPublisher: DomainEventPublisher
    
    private lateinit var useCase: CreateGameSession
    
    @BeforeEach
    fun setup() {
        useCase = CreateGameSession(sessionRepository, playerRepository, eventPublisher)
    }
    
    @Test
    fun `should create game session and publish domain event`() {
        // Given
        val command = CreateGameSessionCommand(
            startTime = LocalDateTime.now(),
            location = Location("John's House"),
            minBuyIn = Money.ofPLN(50),
            gameType = GameType.TEXAS_HOLDEM,
            results = listOf(
                CreateSessionResultCommand(
                    playerId = PlayerId("player-1"),
                    buyIn = Money.ofPLN(100),
                    cashOut = Money.ofPLN(150)
                ),
                CreateSessionResultCommand(
                    playerId = PlayerId("player-2"),
                    buyIn = Money.ofPLN(100),
                    cashOut = Money.ofPLN(50)
                )
            )
        )
        
        every { sessionRepository.save(any()) } answers { firstArg() }
        every { eventPublisher.publish(any()) } just Runs
        
        // When
        val session = useCase.execute(command)
        
        // Then
        assertThat(session.results).hasSize(2)
        assertThat(session.isBalanced()).isTrue()
        verify { sessionRepository.save(any()) }
        verify { eventPublisher.publish(ofType<SessionCreated>()) }
    }
    
    @Test
    fun `should reject session with less than 2 players`() {
        // Given
        val command = CreateGameSessionCommand(
            startTime = LocalDateTime.now(),
            location = Location("John's House"),
            minBuyIn = Money.ofPLN(50),
            gameType = GameType.TEXAS_HOLDEM,
            results = listOf(
                CreateSessionResultCommand(
                    playerId = PlayerId("player-1"),
                    buyIn = Money.ofPLN(100),
                    cashOut = Money.ofPLN(100)
                )
            )
        )
        
        // When/Then
        assertThrows<IllegalArgumentException> {
            useCase.execute(command)
        }
    }
}
```

**Step 2: Implement Use Case (GREEN 🟢)**

```kotlin
// application/usecase/session/CreateGameSession.kt
class CreateGameSession(
    private val sessionRepository: GameSessionRepository,
    private val playerRepository: PlayerRepository,
    private val eventPublisher: DomainEventPublisher
) {
    fun execute(command: CreateGameSessionCommand): GameSession {
        // Validate
        require(command.results.size >= 2) { 
            "Session must have at least 2 players" 
        }
        
        // Create domain model
        val session = GameSession(
            id = GameSessionId.generate(),
            startTime = command.startTime,
            location = command.location,
            minBuyIn = command.minBuyIn,
            gameType = command.gameType,
            results = command.results.map { it.toDomainModel() }
        )
        
        // Save
        val savedSession = sessionRepository.save(session)
        
        // Publish event
        eventPublisher.publish(
            SessionCreated(
                sessionId = savedSession.id,
                occurredAt = Instant.now()
            )
        )
        
        return savedSession
    }
}
```

**Step 3: Refactor (BLUE 🔵)**

```kotlin
// Extract validation to domain model
class GameSession(
    val id: GameSessionId = GameSessionId.generate(),
    val startTime: LocalDateTime,
    val location: Location,
    val minBuyIn: Money,
    val gameType: GameType,
    val results: List<SessionResult>
) {
    init {
        validateMinimumPlayers()
    }
    
    private fun validateMinimumPlayers() {
        require(results.size >= 2) { 
            "Session must have at least 2 players" 
        }
    }
    
    fun isBalanced(): Boolean {
        val totalBuyIns = results.sumOf { it.buyIn.amountInCents }
        val totalCashOuts = results.sumOf { it.cashOut.amountInCents }
        return totalBuyIns == totalCashOuts
    }
}

// Simplify use case (orchestration only)
class CreateGameSession(
    private val sessionRepository: GameSessionRepository,
    private val eventPublisher: DomainEventPublisher
) {
    fun execute(command: CreateGameSessionCommand): GameSession {
        val session = command.toDomainModel() // Validation happens in constructor
        val savedSession = sessionRepository.save(session)
        
        eventPublisher.publish(SessionCreated(savedSession.id))
        
        return savedSession
    }
}
```

**Result:** 
- ✅ Business logic in domain model (GameSession)
- ✅ Use case is thin orchestration layer
- ✅ Tests are maintainable and readable
- ✅ Code follows SOLID principles

---

## Appendix B: Useful Commands

**Frontend:**
```bash
# Development
npm run dev

# Build
npm run build

# Test
npm run test
npm run test:e2e

# Lint
npm run lint
npm run format
```

**Backend:**
```bash
# Run locally
./gradlew bootRun

# Test
./gradlew test

# Build
./gradlew build

# Lint
./gradlew ktlintCheck
```

**Docker:**
```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Rebuild
docker-compose up -d --build

# Stop
docker-compose down
```

### Key Dependencies

**Frontend:**
- react: 18.x
- typescript: 5.x
- vite: 5.x
- @tanstack/react-query: 5.x
- react-hook-form: 7.x
- zod: 3.x
- recharts: 2.x
- tailwindcss: 3.x

**Backend:**
- Kotlin: 1.9.x
- Spring Boot: 3.2.x
- PostgreSQL: 16.x
- Redis: 7.x
- Flyway: 10.x

---

## Appendix C: DDD Patterns Quick Reference

### When to Use Each Pattern

| Pattern | Use When | Example in Poker Stats |
|---------|----------|------------------------|
| **Entity** | Object has identity that persists over time | Player, GameSession |
| **Value Object** | Object defined by its attributes, immutable | Money, Location, GameType |
| **Aggregate** | Group of objects that must be consistent | GameSession (root) + SessionResults |
| **Repository** | Need to abstract data access | GameSessionRepository |
| **Domain Service** | Logic doesn't belong to one entity | StatsCalculator, LeaderboardGenerator |
| **Domain Event** | Something important happened | SessionCreated, PlayerJoined |
| **Factory** | Complex object creation | GameSessionFactory |

### Common DDD Questions

**Q: Should I use JPA entities or domain entities?**  
A: **Both, but separate!** Domain entities are pure business logic. JPA entities are for persistence. Use mappers to convert between them.

```kotlin
// Domain entity (no JPA!)
class Player(
    val id: PlayerId,
    val name: PlayerName,
    val userId: UserId?
) {
    fun linkToUser(userId: UserId): Player {
        require(this.userId == null) { "Player already linked to user" }
        return this.copy(userId = userId)
    }
}

// JPA entity (infrastructure layer)
@Entity
@Table(name = "players")
class PlayerJpaEntity(
    @Id val id: String,
    val name: String,
    val userId: String?
)

// Mapper
class PlayerMapper {
    fun toDomain(jpa: PlayerJpaEntity) = Player(
        id = PlayerId(jpa.id),
        name = PlayerName(jpa.name),
        userId = jpa.userId?.let { UserId(it) }
    )
    
    fun toJpa(domain: Player) = PlayerJpaEntity(
        id = domain.id.value,
        name = domain.name.value,
        userId = domain.userId?.value
    )
}
```

**Q: What should go in the Aggregate Root vs Entity?**  
A: **Aggregate Root** enforces invariants for the whole aggregate. **Entities** within the aggregate are accessed through the root.

```kotlin
// GameSession is Aggregate Root
class GameSession(
    val id: GameSessionId,
    private val results: MutableList<SessionResult> = mutableListOf()
) {
    // Access results through aggregate root
    fun addResult(result: SessionResult) {
        validateCanAddResult(result)
        results.add(result)
    }
    
    fun removeResult(resultId: SessionResultId) {
        require(results.size > 2) { "Cannot remove - minimum 2 players required" }
        results.removeIf { it.id == resultId }
    }
    
    // Aggregate root enforces invariants
    private fun validateCanAddResult(result: SessionResult) {
        require(!results.any { it.playerId == result.playerId }) {
            "Player already has a result in this session"
        }
    }
}
```

**Q: When should I use a Domain Service vs putting logic in an Entity?**  
A: Use **Domain Service** when:
- Logic spans multiple aggregates
- Logic is stateless
- Logic doesn't naturally belong to one entity

```kotlin
// Domain Service: Spans multiple sessions and players
class StatsCalculator {
    fun calculatePlayerStats(
        playerId: PlayerId,
        sessions: List<GameSession>
    ): PlayerStats {
        val playerResults = sessions
            .flatMap { it.results }
            .filter { it.playerId == playerId }
        
        val totalProfit = playerResults.sumOf { it.profit().amountInCents }
        val totalBuyIn = playerResults.sumOf { it.buyIn.amountInCents }
        val roi = if (totalBuyIn > 0) {
            (totalProfit.toDouble() / totalBuyIn) * 100
        } else 0.0
        
        return PlayerStats(
            playerId = playerId,
            totalSessions = playerResults.size,
            netProfit = Money.ofCents(totalProfit),
            roi = ROI(roi)
        )
    }
}
```

**Q: How do I handle cross-aggregate references?**  
A: Use **IDs only**, not object references. Query through repository if needed.

```kotlin
// ✅ GOOD: Reference by ID
class SessionResult(
    val playerId: PlayerId,  // ID reference
    val sessionId: GameSessionId  // ID reference
)

// ❌ BAD: Direct object reference
class SessionResult(
    val player: Player,  // Don't do this across aggregates!
    val session: GameSession
)
```

---

## Appendix D: Clean Code Examples

### Before & After Refactoring

**❌ BEFORE: Code Smells**

```kotlin
// Too many parameters, unclear purpose
fun createSession(d: String, l: String, m: Int, t: String, p: List<Any>): Any {
    // God method - doing too much
    val session = Session()
    session.date = parseDate(d)
    session.location = l
    session.minBuyIn = m
    session.gameType = t
    
    var totalIn = 0
    var totalOut = 0
    for (player in p) {
        val r = Result()
        r.playerId = (player as Map<String, Any>)["id"] as String
        r.buyIn = (player["buyIn"] as Int)
        r.cashOut = (player["cashOut"] as Int)
        totalIn += r.buyIn!!
        totalOut += r.cashOut!!
        session.results.add(r)
    }
    
    if (totalIn != totalOut) {
        println("Warning: not balanced")  // Side effect!
    }
    
    return sessionRepository.save(session)  // Mixing concerns
}
```

**✅ AFTER: Clean Code**

```kotlin
// Clear naming, single responsibility, domain model
class CreateGameSession(
    private val sessionRepository: GameSessionRepository,
    private val eventPublisher: DomainEventPublisher
) {
    fun execute(command: CreateGameSessionCommand): GameSession {
        val session = buildSessionFromCommand(command)
        return saveAndNotify(session)
    }
    
    private fun buildSessionFromCommand(command: CreateGameSessionCommand): GameSession {
        return GameSession(
            startTime = command.startTime,
            location = Location(command.location),
            minBuyIn = Money.ofPLN(command.minBuyIn),
            gameType = GameType.valueOf(command.gameType),
            results = command.results.map { it.toSessionResult() }
        )
    }
    
    private fun saveAndNotify(session: GameSession): GameSession {
        val savedSession = sessionRepository.save(session)
        eventPublisher.publish(SessionCreated(savedSession.id))
        return savedSession
    }
}

// Domain model enforces business rules
class GameSession(
    val startTime: LocalDateTime,
    val location: Location,
    val minBuyIn: Money,
    val gameType: GameType,
    val results: List<SessionResult>
) {
    init {
        require(results.size >= 2) { "Minimum 2 players required" }
    }
    
    fun getBalanceDiscrepancy(): Money {
        return totalCashOut() - totalBuyIn()
    }
    
    fun isBalanced(): Boolean = getBalanceDiscrepancy() == Money.ZERO
    
    private fun totalBuyIn(): Money = results.fold(Money.ZERO) { acc, r -> acc + r.buyIn }
    private fun totalCashOut(): Money = results.fold(Money.ZERO) { acc, r -> acc + r.cashOut }
}
```

### SOLID Principles Examples

**Single Responsibility Principle:**
```kotlin
// ❌ BAD: Class has multiple responsibilities
class PlayerService {
    fun calculateStats(playerId: PlayerId): PlayerStats { ... }
    fun sendEmail(playerId: PlayerId, message: String) { ... }
    fun uploadAvatar(playerId: PlayerId, file: File) { ... }
    fun validatePassword(password: String): Boolean { ... }
}

// ✅ GOOD: Each class has one responsibility
class StatsCalculator {
    fun calculate(playerId: PlayerId): PlayerStats { ... }
}

class EmailNotificationService {
    fun send(playerId: PlayerId, message: String) { ... }
}

class AvatarStorageService {
    fun upload(playerId: PlayerId, file: File): AvatarUrl { ... }
}

class PasswordValidator {
    fun validate(password: String): ValidationResult { ... }
}
```

**Dependency Inversion Principle:**
```kotlin
// ❌ BAD: High-level depends on low-level
class GameSessionService {
    private val repository = JpaGameSessionRepository() // Concrete dependency!
    
    fun create(session: GameSession) = repository.save(session)
}

// ✅ GOOD: Both depend on abstraction
interface GameSessionRepository {  // Abstraction
    fun save(session: GameSession): GameSession
}

class GameSessionService(
    private val repository: GameSessionRepository  // Depend on interface
) {
    fun create(session: GameSession) = repository.save(session)
}

class JpaGameSessionRepository : GameSessionRepository {  // Implementation
    override fun save(session: GameSession): GameSession { ... }
}
```

---

## Reference Links
- [FUNCTIONALITY_DESIGN.md](./FUNCTIONALITY_DESIGN.md)
- [UI_WIREFRAMES.md](./UI_WIREFRAMES.md)
- [stack.md](./stack.md)

### Further Reading
- **DDD:** "Domain-Driven Design" by Eric Evans (Blue Book)
- **Clean Code:** "Clean Code" by Robert C. Martin
- **Lean Startup:** "The Lean Startup" by Eric Ries
- **TDD:** "Test Driven Development: By Example" by Kent Beck
- **Architecture:** "Clean Architecture" by Robert C. Martin
