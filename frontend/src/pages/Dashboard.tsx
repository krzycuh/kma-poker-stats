import React from 'react';
import { useAuth } from '../hooks/useAuth';
import { Link } from 'react-router-dom';
import { UserRole } from '../types/auth';
import { useQuery } from '@tanstack/react-query';
import { dashboardApi } from '../api/dashboard';
import { StatsCard } from '../components/StatsCard';
import { RecentSessionsList } from '../components/RecentSessionsList';
import {
  formatCents,
  formatProfitCents,
  formatPercentage,
  formatROI,
  formatStreak,
} from '../utils/format';

/**
 * Dashboard page component
 * Main landing page after login - Shows personalized stats and recent sessions
 */
export const Dashboard: React.FC = () => {
  const { user, logout } = useAuth();

  // Fetch dashboard data based on user role
  const {
    data: casualDashboard,
    isLoading: casualLoading,
    error: casualError,
  } = useQuery({
    queryKey: ['dashboard', 'casual'],
    queryFn: dashboardApi.getCasualPlayerDashboard,
    enabled: user?.role === UserRole.CASUAL_PLAYER,
  });

  const {
    data: adminDashboard,
    isLoading: adminLoading,
    error: adminError,
  } = useQuery({
    queryKey: ['dashboard', 'admin'],
    queryFn: dashboardApi.getAdminDashboard,
    enabled: user?.role === UserRole.ADMIN,
  });

  if (!user) return null;

  const isAdmin = user.role === UserRole.ADMIN;
  const isLoading = isAdmin ? adminLoading : casualLoading;
  const error = isAdmin ? adminError : casualError;

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Navigation */}
      <nav className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between h-16">
            <div className="flex items-center">
              <h1 className="text-xl font-bold text-gray-900">Poker Stats</h1>
            </div>
            <div className="flex items-center space-x-4">
              <Link to="/profile" className="text-gray-700 hover:text-gray-900">
                Profile
              </Link>
              {isAdmin && (
                <>
                  <Link
                    to="/players"
                    className="text-gray-700 hover:text-gray-900"
                  >
                    Players
                  </Link>
                  <Link
                    to="/sessions"
                    className="text-gray-700 hover:text-gray-900"
                  >
                    Sessions
                  </Link>
                  <Link
                    to="/log-session"
                    className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
                  >
                    Log Session
                  </Link>
                </>
              )}
              <button
                onClick={logout}
                className="px-4 py-2 text-sm font-medium text-gray-700 hover:text-gray-900"
              >
                Logout
              </button>
            </div>
          </div>
        </div>
      </nav>

      {/* Main Content */}
      <div className="max-w-7xl mx-auto py-8 px-4 sm:px-6 lg:px-8">
        {/* Header */}
        <div className="mb-8">
          <h2 className="text-3xl font-bold text-gray-900">
            Welcome, {user.name}!
          </h2>
          <p className="mt-2 text-sm text-gray-600">
            {isAdmin ? 'Administrator Dashboard' : 'Your Poker Statistics'}
          </p>
        </div>

        {/* Loading State */}
        {isLoading && (
          <div className="text-center py-12">
            <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
            <p className="mt-4 text-gray-600">Loading dashboard...</p>
          </div>
        )}

        {/* Error State */}
        {error && (
          <div className="bg-red-50 border border-red-200 rounded-lg p-4">
            <p className="text-red-800">
              Error loading dashboard. Please try again.
            </p>
          </div>
        )}

        {/* Admin Dashboard */}
        {isAdmin && adminDashboard && !isLoading && (
          <div className="space-y-8">
            {/* System Stats */}
            <div>
              <h3 className="text-lg font-medium text-gray-900 mb-4">
                System Overview
              </h3>
              <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4">
                <StatsCard
                  title="Total Sessions"
                  value={adminDashboard.systemStats.totalSessions}
                />
                <StatsCard
                  title="Active Sessions"
                  value={adminDashboard.systemStats.activeSessions}
                />
                <StatsCard
                  title="Total Players"
                  value={adminDashboard.systemStats.totalPlayers}
                />
                <StatsCard
                  title="Money in Play"
                  value={formatCents(
                    adminDashboard.systemStats.totalMoneyInPlayCents
                  )}
                />
              </div>
            </div>

            {/* Admin Personal Stats (if linked to player) */}
            {adminDashboard.personalStats && (
              <div>
                <h3 className="text-lg font-medium text-gray-900 mb-4">
                  Your Performance
                </h3>
                <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4">
                  <StatsCard
                    title="Net Profit"
                    value={formatProfitCents(
                      adminDashboard.personalStats.netProfitCents
                    )}
                    colorClass={
                      adminDashboard.personalStats.netProfitCents >= 0
                        ? 'text-green-600'
                        : 'text-red-600'
                    }
                  />
                  <StatsCard
                    title="ROI"
                    value={formatROI(adminDashboard.personalStats.roi)}
                    colorClass={
                      adminDashboard.personalStats.roi >= 0
                        ? 'text-green-600'
                        : 'text-red-600'
                    }
                  />
                  <StatsCard
                    title="Win Rate"
                    value={formatPercentage(adminDashboard.personalStats.winRate)}
                  />
                  <StatsCard
                    title="Current Streak"
                    value={formatStreak(
                      adminDashboard.personalStats.currentStreak
                    )}
                  />
                </div>
              </div>
            )}

            {/* Recent Sessions */}
            <div>
              <h3 className="text-lg font-medium text-gray-900 mb-4">
                Recent Sessions
              </h3>
              <RecentSessionsList
                sessions={adminDashboard.recentSessions}
                showProfit={false}
              />
            </div>
          </div>
        )}

        {/* Casual Player Dashboard */}
        {!isAdmin && casualDashboard && !isLoading && (
          <div className="space-y-8">
            {/* Personal Stats */}
            <div>
              <h3 className="text-lg font-medium text-gray-900 mb-4">
                Your Statistics
              </h3>
              <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4">
                <StatsCard
                  title="Total Sessions"
                  value={casualDashboard.personalStats.totalSessions}
                />
                <StatsCard
                  title="Net Profit"
                  value={formatProfitCents(
                    casualDashboard.personalStats.netProfitCents
                  )}
                  colorClass={
                    casualDashboard.personalStats.netProfitCents >= 0
                      ? 'text-green-600'
                      : 'text-red-600'
                  }
                />
                <StatsCard
                  title="ROI"
                  value={formatROI(casualDashboard.personalStats.roi)}
                  colorClass={
                    casualDashboard.personalStats.roi >= 0
                      ? 'text-green-600'
                      : 'text-red-600'
                  }
                />
                <StatsCard
                  title="Win Rate"
                  value={formatPercentage(casualDashboard.personalStats.winRate)}
                />
              </div>
            </div>

            {/* More Detailed Stats */}
            <div>
              <h3 className="text-lg font-medium text-gray-900 mb-4">
                Performance Breakdown
              </h3>
              <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-3">
                <StatsCard
                  title="Winning Sessions"
                  value={casualDashboard.personalStats.winningSessionsCount}
                  subtitle={`${formatPercentage(casualDashboard.personalStats.winRate)} win rate`}
                />
                <StatsCard
                  title="Losing Sessions"
                  value={casualDashboard.personalStats.losingSessionsCount}
                />
                <StatsCard
                  title="Current Streak"
                  value={formatStreak(
                    casualDashboard.personalStats.currentStreak
                  )}
                  colorClass={
                    casualDashboard.personalStats.currentStreak > 0
                      ? 'text-green-600'
                      : casualDashboard.personalStats.currentStreak < 0
                      ? 'text-red-600'
                      : 'text-gray-900'
                  }
                />
                <StatsCard
                  title="Biggest Win"
                  value={formatProfitCents(
                    casualDashboard.personalStats.biggestWinCents
                  )}
                  colorClass="text-green-600"
                />
                <StatsCard
                  title="Biggest Loss"
                  value={formatProfitCents(
                    casualDashboard.personalStats.biggestLossCents
                  )}
                  colorClass="text-red-600"
                />
                <StatsCard
                  title="Avg Session Profit"
                  value={formatProfitCents(
                    casualDashboard.personalStats.averageSessionProfitCents
                  )}
                  colorClass={
                    casualDashboard.personalStats.averageSessionProfitCents >= 0
                      ? 'text-green-600'
                      : 'text-red-600'
                  }
                />
              </div>
            </div>

            {/* Leaderboard Position */}
            {casualDashboard.leaderboardPosition && (
              <div className="bg-gradient-to-r from-blue-500 to-purple-600 rounded-lg shadow-lg p-6 text-white">
                <h3 className="text-lg font-semibold mb-2">
                  Your Leaderboard Position
                </h3>
                <div className="flex items-baseline">
                  <span className="text-5xl font-bold">
                    #{casualDashboard.leaderboardPosition.position}
                  </span>
                  <span className="ml-4 text-lg opacity-90">
                    out of {casualDashboard.leaderboardPosition.totalPlayers}{' '}
                    players
                  </span>
                </div>
                <p className="mt-2 opacity-90">
                  {casualDashboard.leaderboardPosition.metric}:{' '}
                  {casualDashboard.leaderboardPosition.value}
                </p>
              </div>
            )}

            {/* Recent Sessions */}
            <div>
              <h3 className="text-lg font-medium text-gray-900 mb-4">
                Recent Sessions
              </h3>
              <RecentSessionsList
                sessions={casualDashboard.recentSessions}
                showProfit={true}
              />
            </div>
          </div>
        )}

        {/* Empty State - No Player Linked */}
        {!isLoading &&
          !error &&
          !isAdmin &&
          casualError instanceof Error &&
          casualError.message.includes('No player linked') && (
            <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-6">
              <h3 className="text-lg font-medium text-yellow-900 mb-2">
                No Player Profile Found
              </h3>
              <p className="text-yellow-800">
                Your account is not linked to a player profile yet. Please
                contact an administrator to set up your player profile.
              </p>
            </div>
          )}
      </div>
    </div>
  );
};
