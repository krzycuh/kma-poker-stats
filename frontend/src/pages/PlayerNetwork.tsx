import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { playerNetworkApi } from '../api/playerNetwork'
import { PageHeader } from '../components/PageHeader'
import { EmptyState } from '../components/EmptyState'
import { useAuth } from '../hooks/useAuth'
import { formatCents, formatDate, formatPercentage, formatProfitCents } from '../utils/format'

export default function PlayerNetwork() {
  const { user } = useAuth()
  const hasPlayerLink = !!user?.linkedPlayerId

  const [searchTerm, setSearchTerm] = useState('')
  const [selectedPlayerId, setSelectedPlayerId] = useState<string | null>(null)
  const [startDate, setStartDate] = useState('')
  const [endDate, setEndDate] = useState('')
  const [showFilters, setShowFilters] = useState(false)

  const {
    data: players,
    isLoading: playersLoading,
    error: playersError,
  } = useQuery({
    queryKey: ['shared-players', searchTerm],
    queryFn: () =>
      playerNetworkApi.searchPlayers({
        searchTerm: searchTerm || undefined,
      }),
    enabled: hasPlayerLink,
  })

  useEffect(() => {
    if (!players) return
    if (players.length === 0) {
      setSelectedPlayerId(null)
      return
    }

    const stillVisible = players.some((player) => player.playerId === selectedPlayerId)
    if (!selectedPlayerId || !stillVisible) {
      setSelectedPlayerId(players[0].playerId)
    }
  }, [players, selectedPlayerId])

  const {
    data: sharedStats,
    isLoading: statsLoading,
    error: statsError,
  } = useQuery({
    queryKey: ['shared-player-stats', selectedPlayerId, startDate, endDate],
    queryFn: () =>
      playerNetworkApi.getSharedStats(
        selectedPlayerId as string,
        startDate || undefined,
        endDate || undefined,
      ),
    enabled: hasPlayerLink && !!selectedPlayerId,
  })

  const overview = sharedStats?.stats.overview
  const rangeActive = Boolean(startDate || endDate)

  const sharedSessionsLabel = useMemo(() => {
    const count = sharedStats?.stats.overview.totalSessions ?? 0
    return `${count} shared ${count === 1 ? 'session' : 'sessions'}`
  }, [sharedStats])

  if (!hasPlayerLink) {
    return (
      <div className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 pt-8 pb-16">
        <EmptyState
          icon="🤝"
          title="Link required to view players"
          description="Ask an admin to connect your user account to a player profile to explore shared stats."
        />
      </div>
    )
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pt-4 pb-8 space-y-6">
      <PageHeader
        title="Player Network"
        description="Search for players who have shared a table with you and review your mutual performance."
      />

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        <div className="space-y-4">
          <div className="rounded-lg bg-white p-4 shadow">
            <label className="block text-sm font-medium text-gray-700 mb-2">Search players</label>
            <input
              type="text"
              placeholder="Start typing a name..."
              value={searchTerm}
              onChange={(event) => setSearchTerm(event.target.value)}
              className="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
            />
          </div>

          <div className="rounded-lg bg-white shadow">
            <div className="border-b border-gray-200 px-4 py-3">
              <p className="text-sm font-semibold text-gray-700">Shared players</p>
            </div>
            <div className="max-h-[520px] overflow-y-auto p-3 space-y-3">
              {playersLoading && (
                <p className="text-sm text-gray-500 px-1">Loading players...</p>
              )}
              {playersError && (
                <div className="rounded-md bg-red-50 p-3 text-sm text-red-700">
                  Unable to load player list.
                </div>
              )}
              {!playersLoading && !playersError && players && players.length === 0 && (
                <div className="rounded-lg border border-dashed border-gray-200 px-3 py-6 text-center text-sm text-gray-500">
                  No shared players yet. Once you log sessions with others, they will appear here.
                </div>
              )}
              {players &&
                players.map((player) => {
                  const isActive = player.playerId === selectedPlayerId
                  return (
                    <button
                      key={player.playerId}
                      type="button"
                      onClick={() => setSelectedPlayerId(player.playerId)}
                      className={`w-full rounded-xl border px-3 py-3 text-left transition ${
                        isActive
                          ? 'border-blue-500 bg-blue-50 shadow-sm'
                          : 'border-gray-200 bg-white hover:border-blue-200'
                      }`}
                    >
                      <div className="flex items-center gap-3">
                        <div className="h-10 w-10 rounded-full bg-blue-100 text-blue-700 flex items-center justify-center font-semibold">
                          {player.avatarUrl ? (
                            <img
                              src={player.avatarUrl}
                              alt={player.name}
                              className="h-10 w-10 rounded-full object-cover"
                            />
                          ) : (
                            player.name.charAt(0).toUpperCase()
                          )}
                        </div>
                        <div className="flex-1">
                          <p className="font-medium text-gray-900">{player.name}</p>
                          <p className="text-sm text-gray-600">
                            {player.sharedSessionsCount}{' '}
                            {player.sharedSessionsCount === 1 ? 'shared session' : 'shared sessions'}
                          </p>
                          {player.lastSharedSessionAt && (
                            <p className="text-xs text-gray-400">
                              Last game {formatDate(player.lastSharedSessionAt)}
                            </p>
                          )}
                        </div>
                      </div>
                    </button>
                  )
                })}
            </div>
          </div>
        </div>

        <div className="lg:col-span-2 space-y-4">
          {!selectedPlayerId && (
            <EmptyState
              icon="📉"
              title="Select a player"
              description="Pick a player on the left to see your head-to-head performance."
            />
          )}

          {selectedPlayerId && statsError && (
            <div className="rounded-lg bg-red-50 border border-red-200 p-4 text-red-800">
              Unable to load shared statistics. Please try again later.
            </div>
          )}

          {selectedPlayerId && statsLoading && (
            <div className="rounded-lg bg-white p-10 text-center shadow">
              <p className="text-sm text-gray-500">Preparing shared stats...</p>
            </div>
          )}

          {selectedPlayerId && !statsLoading && !statsError && sharedStats && overview && (
            <div className="space-y-4">
              <div className="rounded-lg bg-white shadow p-5">
                <div className="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
                  <div>
                    <h2 className="text-xl font-semibold text-gray-900">
                      Shared stats with {sharedStats.playerName}
                    </h2>
                    <p className="text-sm text-gray-600">
                      Based on {sharedSessionsLabel}
                      {rangeActive ? ' (filtered)' : ''}.
                    </p>
                  </div>
                  <div className="flex gap-2">
                    <button
                      type="button"
                      onClick={() => setShowFilters((prev) => !prev)}
                      className="rounded-md border border-gray-300 px-3 py-2 text-sm text-gray-700 hover:bg-gray-50"
                    >
                      {showFilters ? 'Hide Filters' : 'Filter by date'}
                    </button>
                    {rangeActive && (
                      <button
                        type="button"
                        onClick={() => {
                          setStartDate('')
                          setEndDate('')
                        }}
                        className="rounded-md border border-blue-200 px-3 py-2 text-sm text-blue-700 hover:bg-blue-50"
                      >
                        Clear
                      </button>
                    )}
                  </div>
                </div>

                {showFilters && (
                  <div className="mt-4 grid grid-cols-1 gap-3 sm:grid-cols-2">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">Start date</label>
                      <input
                        type="date"
                        value={startDate}
                        onChange={(event) => setStartDate(event.target.value)}
                        className="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">End date</label>
                      <input
                        type="date"
                        value={endDate}
                        onChange={(event) => setEndDate(event.target.value)}
                        className="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                      />
                    </div>
                  </div>
                )}
              </div>

              <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
                <div className="rounded-lg bg-white p-4 shadow">
                  <p className="text-sm text-gray-500">Shared Sessions</p>
                  <p className="text-3xl font-bold text-gray-900">{overview.totalSessions}</p>
                </div>
                <div className="rounded-lg bg-white p-4 shadow">
                  <p className="text-sm text-gray-500">Net Profit vs. {sharedStats.playerName}</p>
                  <p
                    className={`text-3xl font-bold ${
                      overview.netProfitCents >= 0 ? 'text-green-600' : 'text-red-600'
                    }`}
                  >
                    {formatCents(overview.netProfitCents)}
                  </p>
                </div>
                <div className="rounded-lg bg-white p-4 shadow">
                  <p className="text-sm text-gray-500">Win Rate</p>
                  <p className="text-3xl font-bold text-gray-900">{formatPercentage(overview.winRate)}</p>
                </div>
                <div className="rounded-lg bg-white p-4 shadow">
                  <p className="text-sm text-gray-500">Average Session Profit</p>
                  <p
                    className={`text-3xl font-bold ${
                      overview.averageSessionProfitCents >= 0 ? 'text-green-600' : 'text-red-600'
                    }`}
                  >
                    {formatCents(overview.averageSessionProfitCents)}
                  </p>
                </div>
              </div>

              <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
                <div className="rounded-lg bg-white shadow">
                  <div className="border-b border-gray-200 px-4 py-3">
                    <h3 className="font-semibold text-gray-900">Top Shared Sessions</h3>
                  </div>
                  <div className="p-4 space-y-3">
                    {sharedStats.stats.bestSessions.length === 0 && (
                      <p className="text-sm text-gray-500 text-center py-4">
                        No shared sessions in this timeframe.
                      </p>
                    )}
                    {sharedStats.stats.bestSessions.slice(0, 3).map((session) => (
                      <Link
                        key={session.sessionId}
                        to={`/sessions/${session.sessionId}`}
                        className="block rounded-md border border-gray-200 px-3 py-2 hover:bg-gray-50"
                      >
                        <p className="font-medium text-gray-900">{session.location}</p>
                        <p className="text-xs text-gray-500">{formatDate(session.date)}</p>
                        <p className="text-sm font-semibold text-green-600">
                          {formatProfitCents(session.profitCents)}
                        </p>
                      </Link>
                    ))}
                  </div>
                </div>

                <div className="rounded-lg bg-white shadow">
                  <div className="border-b border-gray-200 px-4 py-3">
                    <h3 className="font-semibold text-gray-900">Tough Sessions</h3>
                  </div>
                  <div className="p-4 space-y-3">
                    {sharedStats.stats.worstSessions.length === 0 && (
                      <p className="text-sm text-gray-500 text-center py-4">
                        No downswings recorded in this range.
                      </p>
                    )}
                    {sharedStats.stats.worstSessions.slice(0, 3).map((session) => (
                      <Link
                        key={session.sessionId}
                        to={`/sessions/${session.sessionId}`}
                        className="block rounded-md border border-gray-200 px-3 py-2 hover:bg-gray-50"
                      >
                        <p className="font-medium text-gray-900">{session.location}</p>
                        <p className="text-xs text-gray-500">{formatDate(session.date)}</p>
                        <p className="text-sm font-semibold text-red-600">
                          {formatProfitCents(session.profitCents)}
                        </p>
                      </Link>
                    ))}
                  </div>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
