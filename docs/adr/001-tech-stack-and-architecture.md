# ADR 001: Tech Stack and Architecture Decisions

**Status:** Accepted  
**Date:** 2025-10-19  
**Decision Makers:** Development Team  

## Context

We need to select technologies and architectural patterns for a home game poker statistics tracking platform. The system must support:
- User authentication and authorization
- Game session logging
- Statistics calculation and visualization
- Leaderboards
- Mobile-responsive UI
- Deployment on Raspberry Pi

## Decision

### Frontend Stack
- **React 18 + TypeScript:** Type-safe modern UI development
- **Vite:** Fast build tool and dev server
- **Tailwind CSS:** Utility-first styling for rapid development
- **TanStack Query:** Declarative data fetching and caching
- **React Hook Form + Zod:** Type-safe form handling
- **Recharts:** Data visualization

**Rationale:**
- React ecosystem is mature with excellent tooling
- TypeScript provides type safety and better IDE support
- Vite offers superior DX with instant HMR
- TanStack Query simplifies data synchronization
- Tailwind enables rapid, consistent UI development

### Backend Stack
- **Kotlin + Spring Boot 3.x:** Robust, enterprise-grade framework
- **PostgreSQL:** Reliable RDBMS for transactional data
- **Redis:** High-performance caching layer
- **JWT:** Stateless authentication
- **Flyway:** Version-controlled database migrations

**Rationale:**
- Kotlin offers null safety and concise syntax
- Spring Boot provides comprehensive features and community support
- PostgreSQL is reliable, feature-rich, and free
- Redis reduces database load for frequently accessed data
- JWT enables stateless, scalable authentication

### Architecture
**Domain-Driven Design (DDD) + Clean Architecture**

```
domain/          # Pure business logic, framework-independent
application/     # Use cases, orchestration
infrastructure/  # Technical details (DB, API, security)
config/          # Framework configuration
```

**Rationale:**
- DDD aligns code with business concepts
- Clean Architecture ensures testability and maintainability
- Separation of concerns makes code easier to understand and change
- Domain layer remains pure and testable without infrastructure dependencies

### Testing Strategy
- **TDD:** Test-first development for all business logic
- **Test Pyramid:** 70% unit, 20% integration, 10% E2E
- **Tools:** JUnit 5, MockK, Kotest, Vitest, Testing Library

**Rationale:**
- TDD ensures better design and confidence in changes
- Test pyramid balances coverage with execution speed
- Modern testing tools provide excellent developer experience

### Infrastructure
- **Docker + Docker Compose:** Consistent development environment
- **GitHub Actions:** Automated CI/CD
- **Nginx:** Reverse proxy and static file serving

**Rationale:**
- Docker ensures environment consistency
- GitHub Actions provides free CI/CD for public repos
- Nginx is lightweight and performant for Raspberry Pi

## Consequences

### Positive
- Type safety across the entire stack (TypeScript + Kotlin)
- Fast development with modern tooling
- Testable architecture with clear separation of concerns
- Scalable authentication with JWT
- Efficient caching reduces database load
- Consistent development environment with Docker

### Negative
- Learning curve for DDD concepts
- Additional complexity from clean architecture layers
- Kotlin ecosystem is smaller than Java
- Redis adds operational complexity
- Docker requires more resources on Raspberry Pi

### Risks
- Raspberry Pi performance may be insufficient (mitigation: use external SSD, optimize queries)
- Redis memory usage (mitigation: configure eviction policies)
- JWT security concerns (mitigation: short expiration, refresh tokens, secure storage)

## Alternatives Considered

### Frontend
- **Next.js:** Rejected due to SSR overhead on Raspberry Pi
- **Vue.js:** Rejected due to smaller ecosystem than React
- **Vanilla JS:** Rejected due to lack of type safety

### Backend
- **Node.js + Express:** Rejected due to weaker type system and less structure
- **Java + Spring Boot:** Rejected in favor of Kotlin's conciseness
- **Python + Django:** Rejected due to performance concerns

### Database
- **MySQL:** Rejected in favor of PostgreSQL's advanced features
- **MongoDB:** Rejected due to need for ACID transactions
- **SQLite:** Rejected due to concurrency limitations

### Architecture
- **MVC:** Rejected due to tendency toward anemic domain models
- **Microservices:** Rejected due to operational complexity for small team

## References
- [Domain-Driven Design by Eric Evans](https://www.domainlanguage.com/ddd/)
- [Clean Architecture by Robert C. Martin](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [React Documentation](https://react.dev/)

## Review Schedule
This decision should be reviewed after Phase 5 (MVP completion) or if significant technical challenges emerge.
