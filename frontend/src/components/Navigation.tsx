import { useEffect, useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { UserRole } from '../types/auth';
import logoCard from '../assets/logo-card.svg';
import { APP_VERSION_LABEL } from '../utils/appInfo';

/**
 * Main navigation component with accessibility features
 */
export function Navigation() {
  const { user, logout } = useAuth();
  const location = useLocation();
  const isAdmin = user?.role === UserRole.ADMIN;
  const hasPlayerLink = !!user?.linkedPlayerId;
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const versionLabel = APP_VERSION_LABEL;

  const isActive = (path: string) => location.pathname === path;

  const linkClasses = (path: string) =>
    `flex items-center gap-2 rounded-md px-3 py-2 text-sm font-medium transition-colors focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 ${
      isActive(path)
        ? 'bg-blue-100 text-blue-700'
        : 'text-gray-700 hover:bg-gray-100 hover:text-gray-900'
    }`;

  useEffect(() => {
    setIsMenuOpen(false);
  }, [location.pathname]);

  const sharedLinks = (
    <>
      {hasPlayerLink && (
        <>
          <Link to="/stats" className={linkClasses('/stats')} aria-label="View Statistics">
            📊 <span>Stats</span>
          </Link>
          <Link to="/leaderboard" className={linkClasses('/leaderboard')} aria-label="View Leaderboard">
            🏆 <span>Leaderboard</span>
          </Link>
        </>
      )}
      <Link to="/profile" className={linkClasses('/profile')} aria-label="View Profile">
        👤 <span>Profile</span>
      </Link>
      {isAdmin && (
        <>
          <Link to="/players" className={linkClasses('/players')} aria-label="Manage Players">
            👥 <span>Players</span>
          </Link>
          <Link to="/sessions" className={linkClasses('/sessions')} aria-label="View Sessions">
            📋 <span>Sessions</span>
          </Link>
        </>
      )}
    </>
  );

  return (
    <nav
      className="sticky top-0 z-40 bg-white shadow-sm"
      role="navigation"
      aria-label="Main navigation"
    >
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="flex h-16 items-center justify-between">
          <div className="flex items-center">
            <Link
              to="/"
              className="rounded text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
              aria-label="Poker Stats Home"
            >
              <span className="flex items-center gap-2">
                <img src={logoCard} alt="" className="h-8 w-auto" />
                <span className="flex flex-col leading-tight">
                  <span className="text-xl font-bold tracking-tight">Poker Stats</span>
                  <span className="text-xs font-medium text-gray-500">{versionLabel}</span>
                </span>
              </span>
            </Link>
          </div>
          <div className="hidden items-center space-x-2 md:flex">
            {sharedLinks}
            {isAdmin && (
              <Link
                to="/log-session"
                className="rounded-lg bg-blue-600 px-4 py-2 text-sm font-semibold text-white transition-colors hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
                aria-label="Log New Session"
              >
                + Log Session
              </Link>
            )}
            <button
              onClick={logout}
              className="rounded px-4 py-2 text-sm font-medium text-gray-700 hover:text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
              aria-label="Logout"
            >
              Logout
            </button>
          </div>
          <button
            type="button"
            className="inline-flex items-center rounded-md p-2 text-gray-700 hover:bg-gray-100 hover:text-gray-900 focus:outline-none focus:ring-2 focus:ring-inset focus:ring-blue-500 md:hidden"
            onClick={() => setIsMenuOpen((prev) => !prev)}
            aria-controls="primary-navigation"
            aria-expanded={isMenuOpen}
          >
            <span className="sr-only">Toggle navigation menu</span>
            {isMenuOpen ? (
              <svg className="h-6 w-6" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            ) : (
              <svg className="h-6 w-6" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
              </svg>
            )}
          </button>
        </div>
      </div>

      <div
        id="primary-navigation"
        className={`md:hidden ${isMenuOpen ? 'block' : 'hidden'} border-t border-gray-200 bg-white shadow-inner`}
      >
        <div className="space-y-1 px-4 py-4">
          {sharedLinks}
          {isAdmin && (
            <Link
              to="/log-session"
              className="flex items-center justify-center rounded-md bg-blue-600 px-3 py-2 text-sm font-semibold text-white"
              aria-label="Log New Session"
            >
              + Log Session
            </Link>
          )}
          <button
            onClick={logout}
            className="w-full rounded-md px-3 py-2 text-left text-sm font-medium text-gray-700 hover:bg-gray-100 hover:text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-500"
            aria-label="Logout"
          >
            Logout
          </button>
          <p className="pt-2 text-xs font-medium text-gray-400">Frontend {versionLabel}</p>
        </div>
      </div>
    </nav>
  );
}
