import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { playerApi } from '../../api/players'
import type { SessionFormData } from '../../types/sessionForm'

interface Step2Props {
  formData: SessionFormData
  updateFormData: (updates: Partial<SessionFormData>) => void
  onNext: () => void
  onPrev: () => void
}

export function Step2PlayerSelection({
  formData,
  updateFormData,
  onNext,
  onPrev,
}: Step2Props) {
  const [searchTerm, setSearchTerm] = useState('')
  const [error, setError] = useState('')
  const [spectatorPlayerIds, setSpectatorPlayerIds] = useState<string[]>(
    () => formData.results.filter((r) => r.isSpectator).map((r) => r.playerId),
  )

  const { data: players = [], isLoading } = useQuery({
    queryKey: ['players'],
    queryFn: () => playerApi.list({ includeInactive: false }),
  })

  const filteredPlayers = players.filter((player) =>
    player.name.toLowerCase().includes(searchTerm.toLowerCase()),
  )

  const togglePlayer = (playerId: string) => {
    const isSelected = formData.selectedPlayerIds.includes(playerId)
    const newSelectedPlayerIds = isSelected
      ? formData.selectedPlayerIds.filter((id) => id !== playerId)
      : [...formData.selectedPlayerIds, playerId]

    // If deselecting, also remove from spectators
    if (isSelected) {
      setSpectatorPlayerIds((prev) => prev.filter((id) => id !== playerId))
    }

    updateFormData({ selectedPlayerIds: newSelectedPlayerIds })
    setError('')
  }

  const toggleSpectator = (playerId: string) => {
    setSpectatorPlayerIds((prev) =>
      prev.includes(playerId)
        ? prev.filter((id) => id !== playerId)
        : [...prev, playerId],
    )
    setError('')
  }

  const handleNext = () => {
    // Check for minimum 2 active (non-spectator) players
    const activePlayers = formData.selectedPlayerIds.filter(
      (id) => !spectatorPlayerIds.includes(id),
    )

    if (formData.selectedPlayerIds.length < 2) {
      setError('Please select at least 2 players')
      return
    }

    if (activePlayers.length < 2) {
      setError('Please select at least 2 active (non-spectator) players')
      return
    }

    // Initialize results for selected players
    const results = formData.selectedPlayerIds.map((playerId) => {
      const existing = formData.results.find((r) => r.playerId === playerId)
      const isSpectator = spectatorPlayerIds.includes(playerId)

      return (
        existing || {
          playerId,
          buyInCents: isSpectator ? 0 : formData.minBuyInCents,
          cashOutCents: isSpectator ? 0 : formData.minBuyInCents,
          notes: '',
          isSpectator,
        }
      )
    })

    updateFormData({ results })
    onNext()
  }

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-xl font-semibold text-gray-900 mb-4">
          Select Players
        </h2>
        <p className="text-gray-600 mb-6">
          Choose who participated in this session (minimum 2 players)
        </p>
      </div>

      {/* Search */}
      <div>
        <input
          type="text"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          placeholder="Search players..."
          className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
        />
      </div>

      {/* Selected Count */}
      <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
        <div className="flex items-center justify-between">
          <span className="text-blue-900 font-medium">
            {formData.selectedPlayerIds.length} player
            {formData.selectedPlayerIds.length !== 1 ? 's' : ''} selected
          </span>
          {formData.selectedPlayerIds.length > 0 && (
            <button
              type="button"
              onClick={() => {
                updateFormData({ selectedPlayerIds: [] })
                setSpectatorPlayerIds([])
              }}
              className="text-blue-600 hover:text-blue-700 text-sm"
            >
              Clear all
            </button>
          )}
        </div>
      </div>

      {/* Selected Players - Spectator Checkboxes */}
      {formData.selectedPlayerIds.length > 0 && (
        <div className="bg-gray-50 border border-gray-200 rounded-lg p-4">
          <h3 className="text-sm font-medium text-gray-900 mb-3">
            Selected Players (Mark absent players as spectators)
          </h3>
          <div className="space-y-2">
            {formData.selectedPlayerIds.map((playerId) => {
              const player = players.find((p) => p.id === playerId)
              const isSpectator = spectatorPlayerIds.includes(playerId)
              return (
                <div
                  key={playerId}
                  className="flex items-center justify-between p-2 bg-white rounded border border-gray-200"
                >
                  <span className="text-gray-900">{player?.name}</span>
                  <label className="flex items-center gap-2 cursor-pointer">
                    <input
                      type="checkbox"
                      checked={isSpectator}
                      onChange={() => toggleSpectator(playerId)}
                      className="w-4 h-4 text-blue-600 rounded focus:ring-blue-500"
                    />
                    <span className="text-sm text-gray-700">
                      Spectator (absent)
                    </span>
                  </label>
                </div>
              )
            })}
          </div>
          <p className="text-xs text-gray-500 mt-3">
            Spectators will have 0 PLN buy-in/cash-out but can still view the
            session
          </p>
        </div>
      )}

      {/* Error Message */}
      {error && (
        <div className="bg-red-50 border border-red-200 rounded-lg p-4">
          <p className="text-red-800">{error}</p>
        </div>
      )}

      {/* Player List */}
      {isLoading ? (
        <div className="text-center py-8">
          <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
          <p className="text-gray-600 mt-2">Loading players...</p>
        </div>
      ) : filteredPlayers.length === 0 ? (
        <div className="text-center py-8 bg-gray-50 rounded-lg">
          <p className="text-gray-600">
            {searchTerm
              ? 'No players found matching your search'
              : 'No players available'}
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 max-h-96 overflow-y-auto">
          {filteredPlayers.map((player) => (
            <button
              key={player.id}
              type="button"
              onClick={() => togglePlayer(player.id)}
              className={`p-4 rounded-lg border-2 transition-all text-left ${
                formData.selectedPlayerIds.includes(player.id)
                  ? 'border-blue-500 bg-blue-50'
                  : 'border-gray-200 bg-white hover:border-gray-300'
              }`}
            >
              <div className="flex items-center gap-3">
                <div
                  className={`w-5 h-5 rounded border-2 flex items-center justify-center ${
                    formData.selectedPlayerIds.includes(player.id)
                      ? 'bg-blue-600 border-blue-600'
                      : 'border-gray-300'
                  }`}
                >
                  {formData.selectedPlayerIds.includes(player.id) && (
                    <svg
                      className="w-3 h-3 text-white"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={3}
                        d="M5 13l4 4L19 7"
                      />
                    </svg>
                  )}
                </div>
                <div className="flex-1">
                  <div className="font-medium text-gray-900">
                    {player.name}
                  </div>
                </div>
              </div>
            </button>
          ))}
        </div>
      )}

      {/* Navigation */}
      <div className="flex justify-between pt-4 border-t">
        <button
          type="button"
          onClick={onPrev}
          className="px-6 py-2 text-gray-700 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors"
        >
          Back
        </button>
        <button
          type="button"
          onClick={handleNext}
          className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
        >
          Next: Enter Results
        </button>
      </div>
    </div>
  )
}
