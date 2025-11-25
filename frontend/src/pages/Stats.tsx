import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import {
  LineChart,
  Line,
  BarChart,
  Bar,
  PieChart,
  Pie,
  Cell,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from 'recharts';
import { statsApi } from '../api/stats';
import { formatCents, formatPercentage, formatDate } from '../utils/format';
import { useAuth } from '../hooks/useAuth';
import { EmptyState } from '../components/EmptyState';
import { PageHeader } from '../components/PageHeader';

/**
 * Personal Stats Page (Phase 5)
 * Displays comprehensive statistics with charts
 */
export default function Stats() {
  const { user } = useAuth();
  const hasPlayerLink = !!user?.linkedPlayerId;
  const [startDate, setStartDate] = useState<string>('');
  const [endDate, setEndDate] = useState<string>('');
  const [showDateFilter, setShowDateFilter] = useState(false);

  const {
    data: stats,
    isLoading,
    error,
  } = useQuery({
    queryKey: ['stats', 'personal', startDate, endDate],
    queryFn: () => statsApi.getPersonalStats(startDate || undefined, endDate || undefined),
    enabled: hasPlayerLink,
  });

  const handleClearFilters = () => {
    setStartDate('');
    setEndDate('');
  };

  const handleExportCSV = () => {
    if (!stats) return;

    // Create CSV content
    const headers = ['Date', 'Profit', 'Cumulative Profit'];
    const rows = stats.profitOverTime.map((point) => [
      point.date,
      formatCents(point.profitCents),
      formatCents(point.cumulativeProfitCents),
    ]);

    const csv = [headers, ...rows].map((row) => row.join(',')).join('\n');

    // Download CSV
    const blob = new Blob([csv], { type: 'text/csv' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `poker-stats-${new Date().toISOString().split('T')[0]}.csv`;
    link.click();
    URL.revokeObjectURL(url);
  };

  if (!hasPlayerLink) {
    return (
      <div className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
        <EmptyState
          icon="👤"
          title="Player profile required"
          description="Link this user account to a player profile to access personal statistics."
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
          <p className="font-medium">Error loading statistics</p>
          <p className="text-sm mt-1">
            {error instanceof Error ? error.message : 'An error occurred'}
          </p>
        </div>
      </div>
    );
  }

  if (!stats) {
    return null;
  }

  const { overview, profitOverTime, locationPerformance, dayOfWeekPerformance, bestSessions, worstSessions } = stats;

  // Prepare data for charts
  const profitChartData = profitOverTime.map((point) => ({
    date: new Date(point.date).toLocaleDateString('en-US', { month: 'short', day: 'numeric' }),
    profit: point.profitCents / 100,
    cumulative: point.cumulativeProfitCents / 100,
  }));

  const winLossData = [
    { name: 'Wins', value: overview.winningSessionsCount, color: '#10b981' },
    { name: 'Losses', value: overview.losingSessionsCount, color: '#ef4444' },
  ];

  const locationChartData = locationPerformance.map((loc) => ({
    name: loc.location,
    profit: loc.totalProfitCents / 100,
    sessions: loc.sessionsPlayed,
    winRate: loc.winRate,
  }));

  const dayOfWeekChartData = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'].map((day, index) => {
    const data = dayOfWeekPerformance.find((d) => d.dayOfWeek === index);
    return {
      name: day,
      profit: data ? data.totalProfitCents / 100 : 0,
      sessions: data ? data.sessionsPlayed : 0,
    };
  });

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <PageHeader
        title="Personal Statistics"
        description="Detailed performance analysis"
        actions={
          <div className="flex flex-wrap gap-3">
            <button
              onClick={() => setShowDateFilter(!showDateFilter)}
              className="px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50"
            >
              {showDateFilter ? 'Hide' : 'Filter by Date'}
            </button>
            <button
              onClick={handleExportCSV}
              className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
            >
              Export CSV
            </button>
          </div>
        }
      />

      {/* Date Filter */}
      {showDateFilter && (
        <div className="bg-white rounded-lg shadow p-4 mb-6">
          <div className="flex items-center gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Start Date</label>
              <input
                type="date"
                value={startDate}
                onChange={(e) => setStartDate(e.target.value)}
                className="border border-gray-300 rounded px-3 py-2"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">End Date</label>
              <input
                type="date"
                value={endDate}
                onChange={(e) => setEndDate(e.target.value)}
                className="border border-gray-300 rounded px-3 py-2"
              />
            </div>
            {(startDate || endDate) && (
              <button
                onClick={handleClearFilters}
                className="mt-6 px-4 py-2 text-gray-600 hover:text-gray-800"
              >
                Clear Filters
              </button>
            )}
          </div>
        </div>
      )}

      {/* Overview Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-sm font-medium text-gray-600 mb-2">Total Sessions</h3>
          <p className="text-3xl font-bold text-gray-900">{overview.totalSessions}</p>
        </div>
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-sm font-medium text-gray-600 mb-2">Net Profit</h3>
          <p
            className={`text-3xl font-bold ${
              overview.netProfitCents >= 0 ? 'text-green-600' : 'text-red-600'
            }`}
          >
            {formatCents(overview.netProfitCents)}
          </p>
        </div>
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-sm font-medium text-gray-600 mb-2">ROI</h3>
          <p className={`text-3xl font-bold ${overview.roi >= 0 ? 'text-green-600' : 'text-red-600'}`}>
            {formatPercentage(overview.roi)}
          </p>
        </div>
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-sm font-medium text-gray-600 mb-2">Win Rate</h3>
          <p className="text-3xl font-bold text-gray-900">{formatPercentage(overview.winRate)}</p>
        </div>
      </div>

      {/* Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
        {/* Profit Over Time */}
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Profit Over Time</h2>
          {profitChartData.length > 0 ? (
            <ResponsiveContainer width="100%" height={300}>
              <LineChart data={profitChartData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="date" />
                <YAxis />
                <Tooltip
                  formatter={(value: number) => [`$${value.toFixed(2)}`, '']}
                  labelStyle={{ color: '#000' }}
                />
                <Legend />
                <Line
                  type="monotone"
                  dataKey="profit"
                  stroke="#8b5cf6"
                  name="Session Profit"
                  strokeWidth={2}
                />
                <Line
                  type="monotone"
                  dataKey="cumulative"
                  stroke="#10b981"
                  name="Cumulative Profit"
                  strokeWidth={2}
                />
              </LineChart>
            </ResponsiveContainer>
          ) : (
            <p className="text-gray-500 text-center py-12">No data available</p>
          )}
        </div>

        {/* Win/Loss Distribution */}
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Win/Loss Distribution</h2>
          {overview.totalSessions > 0 ? (
            <ResponsiveContainer width="100%" height={300}>
              <PieChart>
                <Pie
                  data={winLossData}
                  cx="50%"
                  cy="50%"
                  labelLine={false}
                  label={(entry) => `${entry.name}: ${entry.value}`}
                  outerRadius={100}
                  fill="#8884d8"
                  dataKey="value"
                >
                  {winLossData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          ) : (
            <p className="text-gray-500 text-center py-12">No data available</p>
          )}
        </div>

        {/* Performance by Location */}
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Performance by Location</h2>
          {locationChartData.length > 0 ? (
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={locationChartData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" />
                <YAxis />
                <Tooltip formatter={(value: number) => [`$${value.toFixed(2)}`, '']} />
                <Legend />
                <Bar dataKey="profit" fill="#3b82f6" name="Total Profit" />
              </BarChart>
            </ResponsiveContainer>
          ) : (
            <p className="text-gray-500 text-center py-12">No data available</p>
          )}
        </div>

        {/* Performance by Day of Week */}
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Performance by Day of Week</h2>
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={dayOfWeekChartData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="name" />
              <YAxis />
              <Tooltip formatter={(value: number) => [`$${value.toFixed(2)}`, '']} />
              <Legend />
              <Bar dataKey="profit" fill="#8b5cf6" name="Total Profit" />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Best and Worst Sessions */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Best Sessions */}
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">🏆 Best Sessions</h2>
          {bestSessions.length > 0 ? (
            <div className="space-y-3">
              {bestSessions.map((session) => (
                <Link
                  key={session.sessionId}
                  to={`/sessions/${session.sessionId}`}
                  className="block p-3 border border-gray-200 rounded-lg hover:bg-gray-50 transition"
                >
                  <div className="flex justify-between items-start">
                    <div>
                      <p className="font-medium text-gray-900">{session.location}</p>
                      <p className="text-sm text-gray-600">
                        {formatDate(session.date)} • {session.gameType.replace('_', ' ')}
                      </p>
                    </div>
                    <p className="text-lg font-bold text-green-600">
                      {formatCents(session.profitCents)}
                    </p>
                  </div>
                </Link>
              ))}
            </div>
          ) : (
            <p className="text-gray-500 text-center py-8">No sessions yet</p>
          )}
        </div>

        {/* Worst Sessions */}
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">📉 Worst Sessions</h2>
          {worstSessions.length > 0 ? (
            <div className="space-y-3">
              {worstSessions.map((session) => (
                <Link
                  key={session.sessionId}
                  to={`/sessions/${session.sessionId}`}
                  className="block p-3 border border-gray-200 rounded-lg hover:bg-gray-50 transition"
                >
                  <div className="flex justify-between items-start">
                    <div>
                      <p className="font-medium text-gray-900">{session.location}</p>
                      <p className="text-sm text-gray-600">
                        {formatDate(session.date)} • {session.gameType.replace('_', ' ')}
                      </p>
                    </div>
                    <p className="text-lg font-bold text-red-600">{formatCents(session.profitCents)}</p>
                  </div>
                </Link>
              ))}
            </div>
          ) : (
            <p className="text-gray-500 text-center py-8">No sessions yet</p>
          )}
        </div>
      </div>
    </div>
  );
}
