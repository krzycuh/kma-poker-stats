import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { leaderboardApi, LeaderboardMetric } from '../api/leaderboard';
import { useAuth } from '../hooks/useAuth';
import { EmptyState } from '../components/EmptyState';

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
      <div className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
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
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
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
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Header */}
      <div className="flex justify-between items-center mb-8">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Leaderboard</h1>
          <p className="text-gray-600 mt-1">
            {leaderboard.totalEntries} players ranked
          </p>
        </div>
        <Link
          to="/dashboard"
          className="px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50"
        >
          Back to Dashboard
        </Link>
      </div>

      {/* Metric Selector */}
      <div className="bg-white rounded-lg shadow p-4 mb-6">
        <label className="block text-sm font-medium text-gray-700 mb-2">Select Metric</label>
        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-2">
          {metrics.map((metric) => (
            <button
              key={metric.value}
              onClick={() => setSelectedMetric(metric.value)}
              className={`px-4 py-2 rounded-lg border transition ${
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

      {/* Podium for Top 3 */}
      {topThree.length > 0 && (
        <div className="mb-8">
          <h2 className="text-xl font-semibold text-gray-900 mb-4">🏆 Top 3</h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {topThree.map((entry, index) => (
              <div
                key={entry.playerId}
                className={`${getRankColor(
                  entry.rank
                )} rounded-lg shadow-lg p-6 border-2 text-center transform transition ${
                  index === 0 ? 'md:scale-110' : ''
                }`}
              >
                <div className="text-5xl mb-2">{getRankBadge(entry.rank)}</div>
                <h3 className="text-xl font-bold text-gray-900 mb-1">
                  {entry.playerName}
                  {entry.isCurrentUser && (
                    <span className="ml-2 text-sm font-normal text-blue-600">(You)</span>
                  )}
                </h3>
                <p className="text-3xl font-bold text-gray-900 mb-2">{entry.valueFormatted}</p>
                <p className="text-sm text-gray-600">{entry.sessionsPlayed} sessions</p>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Rest of Leaderboard */}
      {restOfLeaderboard.length > 0 && (
        <div className="bg-white rounded-lg shadow overflow-hidden">
          <h2 className="text-xl font-semibold text-gray-900 p-6 border-b border-gray-200">
            All Rankings
          </h2>
          <div className="divide-y divide-gray-200">
            {restOfLeaderboard.map((entry) => (
              <div
                key={entry.playerId}
                className={`px-6 py-4 hover:bg-gray-50 transition ${
                  entry.isCurrentUser ? 'bg-blue-50' : ''
                }`}
              >
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-4">
                    <div className="w-12 text-center">
                      <span className="text-lg font-bold text-gray-600">#{entry.rank}</span>
                    </div>
                    <div>
                      <h3 className="font-medium text-gray-900">
                        {entry.playerName}
                        {entry.isCurrentUser && (
                          <span className="ml-2 text-sm font-normal text-blue-600">(You)</span>
                        )}
                      </h3>
                      <p className="text-sm text-gray-600">{entry.sessionsPlayed} sessions</p>
                    </div>
                  </div>
                  <div className="text-right">
                    <p className="text-xl font-bold text-gray-900">{entry.valueFormatted}</p>
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
          <div className="mt-6 bg-blue-50 border-2 border-blue-300 rounded-lg p-6">
            <h3 className="font-semibold text-blue-900 mb-2">Your Position</h3>
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-4">
                <span className="text-2xl font-bold text-blue-900">
                  #{leaderboard.currentUserEntry.rank}
                </span>
                <div>
                  <p className="font-medium text-blue-900">
                    {leaderboard.currentUserEntry.playerName}
                  </p>
                  <p className="text-sm text-blue-700">
                    {leaderboard.currentUserEntry.sessionsPlayed} sessions
                  </p>
                </div>
              </div>
              <p className="text-2xl font-bold text-blue-900">
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
