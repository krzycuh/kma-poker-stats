import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { leaderboardApi, LeaderboardMetric } from '../api/leaderboard';
import { useAuth } from '../hooks/useAuth';
import { EmptyState } from '../components/EmptyState';
import { PageHeader } from '../components/PageHeader';

/**
 * Leaderboard Page (Phase 5)
 * Displays rankings across different metrics with podium for top 3
 */
export default function Leaderboard() {
  const { user } = useAuth();
  const hasPlayerLink = !!user?.linkedPlayerId;
  const [selectedMetric, setSelectedMetric] = useState<LeaderboardMetric>(
    LeaderboardMetric.NET_PROFIT
  );

  const {
    data: leaderboard,
    isLoading,
    error,
  } = useQuery({
    queryKey: ['leaderboard', selectedMetric],
    queryFn: () => leaderboardApi.getLeaderboard(selectedMetric, 50),
    enabled: hasPlayerLink,
  });

  const metrics = [
    { value: LeaderboardMetric.NET_PROFIT, label: 'Net Profit' },
    { value: LeaderboardMetric.ROI, label: 'ROI %' },
    { value: LeaderboardMetric.WIN_RATE, label: 'Win Rate %' },
    { value: LeaderboardMetric.CURRENT_STREAK, label: 'Current Streak' },
    { value: LeaderboardMetric.TOTAL_SESSIONS, label: 'Total Sessions' },
    { value: LeaderboardMetric.AVERAGE_PROFIT, label: 'Avg Profit' },
  ];

  const getRankBadge = (rank: number) => {
    switch (rank) {
      case 1:
        return '🥇';
      case 2:
        return '🥈';
      case 3:
        return '🥉';
      default:
        return rank;
    }
  };

  const getRankColor = (rank: number) => {
    switch (rank) {
      case 1:
        return 'bg-yellow-100 text-yellow-800 border-yellow-300';
      case 2:
        return 'bg-gray-100 text-gray-800 border-gray-300';
      case 3:
        return 'bg-orange-100 text-orange-800 border-orange-300';
      default:
        return 'bg-white';
    }
  };

  if (!hasPlayerLink) {
    return (
      <div className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 pt-8 pb-16">
        <EmptyState
          icon="🏅"
          title="Link required to view leaderboard"
          description="Only linked players can browse leaderboard positions. Ask your admin to connect your account."
        />
      </div>
    );
  }

  if (isLoading) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pt-4 pb-8">
        <div className="bg-red-50 border border-red-200 rounded-lg p-4 text-red-800">
          <p className="font-medium">Error loading leaderboard</p>
          <p className="text-sm mt-1">
            {error instanceof Error ? error.message : 'An error occurred'}
          </p>
        </div>
      </div>
    );
  }

  if (!leaderboard) {
    return null;
  }

  const topThree = leaderboard.entries.slice(0, 3);
  const restOfLeaderboard = leaderboard.entries.slice(3);

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pt-4 pb-8">
      <PageHeader
        title="Leaderboard"
        description={`${leaderboard.totalEntries} players ranked`}
      />

      {/* Metric Selector */}
      <div className="bg-white rounded-lg shadow p-2 sm:p-4 mb-3 sm:mb-6">
        <label className="block text-xs sm:text-sm font-medium text-gray-700 mb-1.5 sm:mb-2 px-1">Select Metric</label>
        <div className="grid grid-cols-3 sm:grid-cols-3 lg:grid-cols-6 gap-1 sm:gap-2">
          {metrics.map((metric) => (
            <button
              key={metric.value}
              onClick={() => setSelectedMetric(metric.value)}
              className={`px-2 sm:px-4 py-1.5 sm:py-2 rounded-lg border transition text-xs sm:text-sm ${
                selectedMetric === metric.value
                  ? 'bg-blue-600 text-white border-blue-600'
                  : 'bg-white text-gray-700 border-gray-300 hover:bg-gray-50'
              }`}
            >
              {metric.label}
            </button>
          ))}
        </div>
      </div>

      {/* Top 3 - compact inline rows on mobile */}
      {topThree.length > 0 && (
        <div className="mb-4 sm:mb-8">
          <h2 className="text-base sm:text-xl font-semibold text-gray-900 mb-2 sm:mb-4">🏆 Top 3</h2>
          <div className="space-y-1.5 sm:space-y-0 sm:grid sm:grid-cols-3 sm:gap-4">
            {topThree.map((entry, index) => (
              <div
                key={entry.playerId}
                className={`${getRankColor(
                  entry.rank
                )} rounded-lg shadow-sm sm:shadow-lg border sm:border-2 transition`}
              >
                {/* Mobile: compact inline row */}
                <div className="flex items-center gap-2 px-3 py-2 sm:hidden">
                  <span className="text-xl flex-shrink-0">{getRankBadge(entry.rank)}</span>
                  <Link
                    to={`/stats?playerId=${entry.playerId}`}
                    className="font-semibold text-sm text-gray-900 hover:text-blue-600 truncate min-w-0 flex-1"
                  >
                    {entry.playerName}
                    {entry.isCurrentUser && (
                      <span className="ml-1 text-xs font-normal text-blue-600">(You)</span>
                    )}
                  </Link>
                  <span className="font-bold text-sm text-gray-900 flex-shrink-0">{entry.valueFormatted}</span>
                  <span className="text-xs text-gray-500 flex-shrink-0">{entry.sessionsPlayed}s</span>
                </div>
                {/* Desktop: centered card */}
                <div className={`hidden sm:block p-4 text-center ${index === 0 ? 'md:scale-105' : ''}`}>
                  <div className="text-3xl mb-1">{getRankBadge(entry.rank)}</div>
                  <h3 className="text-base font-bold text-gray-900 mb-0.5">
                    <Link
                      to={`/stats?playerId=${entry.playerId}`}
                      className="hover:text-blue-600 hover:underline transition"
                    >
                      {entry.playerName}
                    </Link>
                    {entry.isCurrentUser && (
                      <span className="ml-1 text-xs font-normal text-blue-600">(You)</span>
                    )}
                  </h3>
                  <p className="text-xl font-bold text-gray-900 mb-1">{entry.valueFormatted}</p>
                  <p className="text-xs text-gray-600">{entry.sessionsPlayed} sessions</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Rest of Leaderboard */}
      {restOfLeaderboard.length > 0 && (
        <div className="bg-white rounded-lg shadow overflow-hidden">
          <h2 className="text-sm sm:text-xl font-semibold text-gray-900 px-3 sm:px-6 py-2 sm:py-4 border-b border-gray-200">
            All Rankings
          </h2>
          <div className="divide-y divide-gray-200">
            {restOfLeaderboard.map((entry) => (
              <div
                key={entry.playerId}
                className={`px-3 sm:px-6 py-2 sm:py-3 hover:bg-gray-50 transition ${
                  entry.isCurrentUser ? 'bg-blue-50' : ''
                }`}
              >
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2 sm:gap-4 min-w-0 flex-1">
                    <div className="w-8 sm:w-12 text-center flex-shrink-0">
                      <span className="text-sm sm:text-lg font-bold text-gray-600">#{entry.rank}</span>
                    </div>
                    <div className="min-w-0 flex-1">
                      <h3 className="text-sm sm:text-base font-medium text-gray-900 truncate">
                        <Link
                          to={`/stats?playerId=${entry.playerId}`}
                          className="hover:text-blue-600 hover:underline transition"
                        >
                          {entry.playerName}
                        </Link>
                        {entry.isCurrentUser && (
                          <span className="ml-1 text-xs font-normal text-blue-600">(You)</span>
                        )}
                      </h3>
                      <p className="text-xs text-gray-500">{entry.sessionsPlayed} sessions</p>
                    </div>
                  </div>
                  <div className="text-right flex-shrink-0 ml-2">
                    <p className="text-sm sm:text-xl font-bold text-gray-900">{entry.valueFormatted}</p>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Current User Position if not in top results */}
      {leaderboard.currentUserEntry &&
        !leaderboard.entries.some((e) => e.isCurrentUser) && (
          <div className="mt-3 sm:mt-6 bg-blue-50 border-2 border-blue-300 rounded-lg px-3 sm:px-6 py-2.5 sm:py-4">
            <h3 className="font-semibold text-blue-900 text-sm sm:text-base mb-1 sm:mb-2">Your Position</h3>
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2 sm:gap-4 min-w-0">
                <span className="text-lg sm:text-2xl font-bold text-blue-900">
                  #{leaderboard.currentUserEntry.rank}
                </span>
                <div className="min-w-0">
                  <p className="text-sm sm:text-base font-medium text-blue-900 truncate">
                    <Link
                      to={`/stats?playerId=${leaderboard.currentUserEntry.playerId}`}
                      className="hover:text-blue-700 hover:underline transition"
                    >
                      {leaderboard.currentUserEntry.playerName}
                    </Link>
                  </p>
                  <p className="text-xs sm:text-sm text-blue-700">
                    {leaderboard.currentUserEntry.sessionsPlayed} sessions
                  </p>
                </div>
              </div>
              <p className="text-lg sm:text-2xl font-bold text-blue-900 flex-shrink-0">
                {leaderboard.currentUserEntry.valueFormatted}
              </p>
            </div>
          </div>
        )}

      {/* Empty State */}
      {leaderboard.entries.length === 0 && (
        <div className="bg-white rounded-lg shadow p-12 text-center">
          <p className="text-gray-600 text-lg">No rankings available yet</p>
          <p className="text-gray-500 text-sm mt-2">Play some sessions to appear on the leaderboard!</p>
        </div>
      )}
    </div>
  );
}
