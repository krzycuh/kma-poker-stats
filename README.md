# Poker Stats Web App

A home game poker statistics tracking platform for casual poker groups to log sessions, track performance, and view leaderboards.

## 🎯 Project Overview

This application helps casual poker groups:
- **Log game sessions** with buy-ins, cash-outs, and results
- **Track personal statistics** including profit/loss, ROI, and win rates
- **View leaderboards** across multiple metrics
- **Analyze performance** with charts and historical data

## 🏗️ Architecture

**Tech Stack:**
- **Frontend:** React 18 + TypeScript + Vite + Tailwind CSS + TanStack Query + Recharts
- **Backend:** Kotlin + Spring Boot 3.x + Spring Security (JWT)
- **Database:** PostgreSQL + Redis (caching)
- **Infrastructure:** Docker + Docker Compose + GitHub Actions

**Design Principles:**
- Domain-Driven Design (DDD) with clear bounded contexts
- Clean Architecture with separation of concerns
- Test-Driven Development (TDD)
- SOLID principles

## 📁 Project Structure

```
poker-stats/
├── frontend/          # React + TypeScript frontend
├── backend/           # Kotlin + Spring Boot backend
├── docs/              # Documentation
│   ├── adr/          # Architecture Decision Records
│   ├── FUNCTIONALITY_DESIGN.md
│   ├── IMPLEMENTATION_PLAN.md
│   └── UI_WIREFRAMES.md
├── .github/
│   └── workflows/    # CI/CD pipelines
├── docker-compose.yml
└── README.md
```

## 🚀 Quick Start

### Prerequisites

- Node.js 18+ and npm
- Java 17+ and Gradle
- Docker and Docker Compose
- Git

### Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd poker-stats
   ```

2. **Start infrastructure (PostgreSQL + Redis)**
   ```bash
   docker-compose up -d
   ```

3. **Start the backend**
   ```bash
   cd backend
   ./gradlew bootRun
   ```
   Backend will be available at http://localhost:8080

4. **Start the frontend**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
   Frontend will be available at http://localhost:5173

## 🧪 Testing

**Frontend:**
```bash
cd frontend
npm run test          # Unit tests
npm run test:e2e      # E2E tests with Playwright
npm run lint          # Linting
```

**Backend:**
```bash
cd backend
./gradlew test        # Unit and integration tests
./gradlew ktlintCheck # Kotlin linting
```

## 📚 Documentation

- [Functionality Design](docs/FUNCTIONALITY_DESIGN.md) - Feature specifications and user stories
- [Implementation Plan](docs/IMPLEMENTATION_PLAN.md) - Development roadmap and best practices
- [UI Wireframes](docs/UI_WIREFRAMES.md) - User interface designs
- [Tech Stack](docs/stack.md) - Technology decisions

## 🔑 User Roles

**Casual Player:**
- View personal dashboard with statistics
- View session history (own sessions only)
- View leaderboards
- Edit profile

**Admin:**
- All Casual Player capabilities
- Log new game sessions
- Edit and delete sessions
- Manage players
- View all sessions

## 🎯 Current Phase

**Phase 0: Project Setup & Infrastructure** ✅

Setting up the development environment, project structure, and CI/CD pipeline.

See [Implementation Plan](docs/IMPLEMENTATION_PLAN.md) for detailed roadmap.

## 📝 Development Workflow

1. Write failing tests first (TDD)
2. Implement minimal code to pass tests
3. Refactor while keeping tests green
4. Submit PR with tests and documentation
5. Code review and approval
6. Deploy to staging

## 🤝 Contributing

1. Create a feature branch from `main`
2. Follow the code style guidelines (ESLint, ktlint)
3. Ensure all tests pass
4. Submit a pull request with clear description

## 📄 License

[Specify License]

## 👥 Team

[Team information]

## 🔗 Links

- API Documentation: http://localhost:8080/swagger-ui
- Health Check: http://localhost:8080/actuator/health
- Frontend Dev: http://localhost:5173

---

Built with ❤️ following Domain-Driven Design and Clean Architecture principles.
