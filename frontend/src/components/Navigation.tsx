import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { UserRole } from '../types/auth';

/**
 * Main navigation component with accessibility features
 */
export function Navigation() {
  const { user, logout } = useAuth();
  const location = useLocation();
  const isAdmin = user?.role === UserRole.ADMIN;

  const isActive = (path: string) => location.pathname === path;

  const linkClasses = (path: string) =>
    `px-3 py-2 rounded-md text-sm font-medium transition-colors focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 ${
      isActive(path)
        ? 'bg-blue-100 text-blue-700'
        : 'text-gray-700 hover:bg-gray-100 hover:text-gray-900'
    }`;

  return (
    <nav className="bg-white shadow-sm" role="navigation" aria-label="Main navigation">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between h-16">
          <div className="flex items-center">
            <Link
              to="/"
              className="text-xl font-bold text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 rounded"
              aria-label="Poker Stats Home"
            >
              🎲 Poker Stats
            </Link>
          </div>
          <div className="flex items-center space-x-2 md:space-x-4">
            <Link to="/stats" className={linkClasses('/stats')} aria-label="View Statistics">
              📊 Stats
            </Link>
            <Link
              to="/leaderboard"
              className={linkClasses('/leaderboard')}
              aria-label="View Leaderboard"
            >
              🏆 Leaderboard
            </Link>
            <Link to="/profile" className={linkClasses('/profile')} aria-label="View Profile">
              👤 Profile
            </Link>
            {isAdmin && (
              <>
                <Link
                  to="/players"
                  className={linkClasses('/players')}
                  aria-label="Manage Players"
                >
                  👥 Players
                </Link>
                <Link
                  to="/sessions"
                  className={linkClasses('/sessions')}
                  aria-label="View Sessions"
                >
                  📋 Sessions
                </Link>
                <Link
                  to="/log-session"
                  className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
                  aria-label="Log New Session"
                >
                  + Log Session
                </Link>
              </>
            )}
            <button
              onClick={logout}
              className="px-4 py-2 text-sm font-medium text-gray-700 hover:text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 rounded"
              aria-label="Logout"
            >
              Logout
            </button>
          </div>
        </div>
      </div>
    </nav>
  );
}
