# Poker Stats Frontend

React + TypeScript + Vite frontend for the Poker Stats application.

## Tech Stack

- **React 19** - UI framework
- **TypeScript** - Type safety
- **Vite** - Build tool and dev server
- **Tailwind CSS** - Styling
- **TanStack Query** - Data fetching and caching
- **React Router** - Routing
- **React Hook Form + Zod** - Form handling and validation
- **Recharts** - Charts and visualizations
- **Headless UI** - Accessible UI components
- **Vitest** - Unit testing
- **Testing Library** - Component testing

## Development

```bash
# Install dependencies
npm install

# Start dev server
npm run dev

# Run tests
npm run test

# Run linter
npm run lint

# Build for production
npm run build
```

## Project Structure

```
src/
├── api/              # API client and endpoints
├── components/
│   ├── common/       # Reusable components (Button, Card, Modal, etc.)
│   ├── layout/       # Layout components (Header, Footer, Sidebar)
│   └── features/     # Feature-specific components
├── hooks/            # Custom React hooks
├── pages/            # Page components
├── types/            # TypeScript type definitions
├── utils/            # Utility functions
├── store/            # State management (if needed)
├── styles/           # Global styles and Tailwind config
└── test/             # Test setup and utilities
```

## Environment Variables

- `VITE_API_BASE_URL` - Backend API base URL
- `VITE_APP_NAME` - Application name

## Path Aliases

The following path aliases are configured:

- `@/` → `src/`
- `@components/` → `src/components/`
- `@pages/` → `src/pages/`
- `@api/` → `src/api/`
- `@hooks/` → `src/hooks/`
- `@utils/` → `src/utils/`
- `@types/` → `src/types/`
- `@store/` → `src/store/`
- `@styles/` → `src/styles/`

## Code Style

- Use functional components with hooks
- Follow the principle of single responsibility
- Keep components small and focused (< 200 lines)
- Write tests for all business logic
- Use TypeScript strictly - no `any` types
- Follow the project's ESLint configuration
