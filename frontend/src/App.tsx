import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AuthProvider } from './contexts/AuthContext';
import { ProtectedRoute } from './components/ProtectedRoute';
import { Login } from './pages/Login';
import { Register } from './pages/Register';
import { Dashboard } from './pages/Dashboard';
import { Profile } from './pages/Profile';
import Players from './pages/Players';
import LogSession from './pages/LogSession';
import EditSession from './pages/EditSession';
import { Sessions } from './pages/Sessions';
import { SessionDetail } from './pages/SessionDetail';
import Stats from './pages/Stats';
import Leaderboard from './pages/Leaderboard';
import { UserRole } from './types/auth';
import './App.css';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      retry: 1,
    },
  },
});

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <AuthProvider>
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route
              path="/"
              element={
                <ProtectedRoute>
                  <Dashboard />
                </ProtectedRoute>
              }
            />
            <Route
              path="/profile"
              element={
                <ProtectedRoute>
                  <Profile />
                </ProtectedRoute>
              }
            />
            <Route
              path="/players"
              element={
                <ProtectedRoute requireRole={UserRole.ADMIN}>
                  <Players />
                </ProtectedRoute>
              }
            />
            <Route
              path="/log-session"
              element={
                <ProtectedRoute requireRole={UserRole.ADMIN}>
                  <LogSession />
                </ProtectedRoute>
              }
            />
            <Route
              path="/sessions"
              element={
                <ProtectedRoute requireRole={UserRole.ADMIN}>
                  <Sessions />
                </ProtectedRoute>
              }
            />
            <Route
              path="/sessions/:id"
              element={
                <ProtectedRoute requireRole={UserRole.ADMIN}>
                  <SessionDetail />
                </ProtectedRoute>
              }
            />
            <Route
              path="/sessions/:id/edit"
              element={
                <ProtectedRoute requireRole={UserRole.ADMIN}>
                  <EditSession />
                </ProtectedRoute>
              }
            />
            <Route
              path="/stats"
              element={
                <ProtectedRoute>
                  <Stats />
                </ProtectedRoute>
              }
            />
            <Route
              path="/leaderboard"
              element={
                <ProtectedRoute>
                  <Leaderboard />
                </ProtectedRoute>
              }
            />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </AuthProvider>
      </BrowserRouter>
    </QueryClientProvider>
  );
}

export default App;
