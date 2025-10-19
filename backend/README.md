# Poker Stats Backend

Kotlin + Spring Boot backend for the Poker Stats application following Domain-Driven Design (DDD) principles.

## Tech Stack

- **Kotlin 1.9.23** - Programming language
- **Spring Boot 3.2.5** - Application framework
- **Spring Data JPA** - Data persistence
- **Spring Security** - Authentication and authorization
- **PostgreSQL** - Primary database
- **Redis** - Caching layer
- **Flyway** - Database migrations
- **JWT** - Token-based authentication
- **OpenAPI 3** - API documentation
- **JUnit 5 + MockK + Kotest** - Testing
- **Testcontainers** - Integration testing
- **ArchUnit** - Architecture testing

## Architecture

This project follows **Domain-Driven Design (DDD)** with **Clean Architecture**:

```
src/main/kotlin/com/pokerstats/
├── domain/                    # Core business logic (framework-independent)
│   ├── model/                 # Entities, Value Objects, Aggregates
│   ├── repository/            # Repository interfaces (ports)
│   ├── service/               # Domain Services
│   └── event/                 # Domain Events
├── application/               # Use cases, orchestration
│   ├── usecase/               # Application use cases
│   └── dto/                   # Application DTOs
├── infrastructure/            # Technical details
│   ├── persistence/           # JPA implementation
│   ├── cache/                 # Redis caching
│   ├── api/rest/              # REST controllers
│   └── security/              # Security configuration
└── config/                    # Spring configuration
```

### Key DDD Patterns

- **Aggregates:** GameSession (with SessionResults), Player
- **Value Objects:** Money, Location, GameType (immutable, no identity)
- **Repository Pattern:** Abstract data access behind interfaces
- **Domain Services:** Complex business logic (StatsCalculator, LeaderboardGenerator)
- **Domain Events:** SessionCreated, SessionUpdated
- **Anti-Corruption Layer:** Separate JPA entities from domain entities

## Development

### Prerequisites

- Java 17+
- PostgreSQL 16+
- Redis 7+
- Docker and Docker Compose (recommended)

### Setup

1. **Start infrastructure (PostgreSQL + Redis)**
   ```bash
   docker-compose up -d
   ```

2. **Run the application**
   ```bash
   ./gradlew bootRun
   ```

3. **Run tests**
   ```bash
   ./gradlew test
   ```

4. **Run linter**
   ```bash
   ./gradlew ktlintCheck
   ./gradlew ktlintFormat  # Auto-fix issues
   ```

5. **Build JAR**
   ```bash
   ./gradlew build
   ```

### API Documentation

Once the application is running:
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- Health Check: http://localhost:8080/actuator/health

## Testing Strategy

- **Unit Tests (70%):** Domain logic, pure business rules
- **Integration Tests (20%):** Use cases with database
- **API Tests (10%):** Controller endpoints
- **Architecture Tests:** Enforce DDD layer dependencies with ArchUnit

## Database Migrations

Flyway migrations are located in `src/main/resources/db/migration/`:
- `V1__initial_schema.sql` - Initial database schema
- `V2__...` - Subsequent migrations

## Configuration

Environment-specific configuration in `application.yml`:
- `dev` - Development (default)
- `test` - Testing
- `prod` - Production

## Code Style

- Follow Kotlin coding conventions
- Use ktlint for code style enforcement
- Keep domain layer framework-independent
- Write tests first (TDD)
- SOLID principles
- Functions < 20 lines, classes < 200 lines

## Security

- JWT-based authentication
- Role-based authorization (CASUAL_PLAYER, ADMIN)
- BCrypt password hashing
- CORS configuration for frontend
- Security headers enabled

## Performance

- Redis caching for expensive calculations (stats, leaderboards)
- Database query optimization with proper indexes
- Connection pooling with HikariCP
- Response time target: p95 < 500ms
