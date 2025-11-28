import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { playerApi } from '../../api/players'
import type { SessionFormData } from '../../types/sessionForm'
import { formatCents } from '../../utils/format'

interface Step3Props {
  formData: SessionFormData
  updateFormData: (updates: Partial<SessionFormData>) => void
  onNext: () => void
  onPrev: () => void
}

export function Step3ResultsEntry({
  formData,
  updateFormData,
  onNext,
  onPrev,
}: Step3Props) {
  const [currentPlayerIndex, setCurrentPlayerIndex] = useState(0)
  const [errors, setErrors] = useState<Record<string, string>>({})

  const { data: allPlayers = [] } = useQuery({
    queryKey: ['players'],
    queryFn: () => playerApi.list({ includeInactive: false }),
  })

  const getPlayerName = (playerId: string): string => {
    const player = allPlayers.find((p) => p.id === playerId)
    return player?.name || 'Unknown Player'
  }

  const currentResult = formData.results[currentPlayerIndex]

  const updateResult = (
    playerId: string,
    updates: {
      buyInCents?: number
      cashOutCents?: number
      notes?: string
    },
  ) => {
    const newResults = formData.results.map((r) =>
      r.playerId === playerId ? { ...r, ...updates } : r,
    )
    updateFormData({ results: newResults })
  }

  const formatCurrency = (cents: number): string => {
    return (cents / 100).toFixed(2)
  }

  const parseCurrency = (value: string): number => {
    const num = parseFloat(value)
    return isNaN(num) ? 0 : Math.round(num * 100)
  }

  const calculateProfit = (
    buyInCents: number,
    cashOutCents: number,
  ): number => {
    return cashOutCents - buyInCents
  }

  const calculateTotals = () => {
    // Exclude spectators from totals
    const activeResults = formData.results.filter((r) => !r.isSpectator)
    const totalBuyIn = activeResults.reduce((sum, r) => sum + r.buyInCents, 0)
    const totalCashOut = activeResults.reduce(
      (sum, r) => sum + r.cashOutCents,
      0,
    )
    const discrepancy = totalCashOut - totalBuyIn
    return { totalBuyIn, totalCashOut, discrepancy }
  }

  const { totalBuyIn, totalCashOut, discrepancy } = calculateTotals()

  const validate = (): boolean => {
    const newErrors: Record<string, string> = {}

    formData.results.forEach((result, index) => {
      // Skip validation for spectators
      if (result.isSpectator) {
        return
      }

      if (result.buyInCents <= 0) {
        newErrors[`buyIn-${index}`] = 'Buy-in must be greater than 0'
      }
      if (result.cashOutCents < 0) {
        newErrors[`cashOut-${index}`] = 'Cash-out cannot be negative'
      }
    })

    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const handleNext = () => {
    if (validate()) {
      onNext()
    }
  }

  const goToPlayer = (index: number) => {
    setCurrentPlayerIndex(index)
  }

  const nextPlayer = () => {
    if (currentPlayerIndex < formData.results.length - 1) {
      setCurrentPlayerIndex(currentPlayerIndex + 1)
    }
  }

  const prevPlayer = () => {
    if (currentPlayerIndex > 0) {
      setCurrentPlayerIndex(currentPlayerIndex - 1)
    }
  }

  if (!currentResult) {
    return <div>No players selected</div>
  }

  const profit = calculateProfit(
    currentResult.buyInCents,
    currentResult.cashOutCents,
  )

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-xl font-semibold text-gray-900 mb-4">
          Enter Results
        </h2>
        <p className="text-gray-600 mb-6">
          Enter buy-in and cash-out amounts for each player
        </p>
      </div>

      {/* Player Navigation */}
      <div className="flex items-center justify-between bg-gray-50 rounded-lg p-4">
        <button
          type="button"
          onClick={prevPlayer}
          disabled={currentPlayerIndex === 0}
          className="p-2 text-gray-600 hover:text-gray-900 disabled:opacity-30 disabled:cursor-not-allowed"
        >
          <svg
            className="w-6 h-6"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M15 19l-7-7 7-7"
            />
          </svg>
        </button>

        <div className="text-center">
          <div className="text-lg font-semibold text-gray-900 flex items-center justify-center gap-2">
            {getPlayerName(currentResult.playerId)}
            {currentResult.isSpectator && (
              <span className="text-xs bg-gray-200 text-gray-700 px-2 py-1 rounded">
                Spectator
              </span>
            )}
          </div>
          <div className="text-sm text-gray-600">
            Player {currentPlayerIndex + 1} of {formData.results.length}
          </div>
        </div>

        <button
          type="button"
          onClick={nextPlayer}
          disabled={currentPlayerIndex === formData.results.length - 1}
          className="p-2 text-gray-600 hover:text-gray-900 disabled:opacity-30 disabled:cursor-not-allowed"
        >
          <svg
            className="w-6 h-6"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M9 5l7 7-7 7"
            />
          </svg>
        </button>
      </div>

      {/* Player Tabs (Mobile: Scroll, Desktop: Grid) */}
      <div className="flex gap-2 overflow-x-auto pb-2">
        {formData.results.map((result, index) => {
          const playerProfit = calculateProfit(
            result.buyInCents,
            result.cashOutCents,
          )
          return (
            <button
              key={result.playerId}
              type="button"
              onClick={() => goToPlayer(index)}
              className={`px-4 py-2 rounded-lg text-sm whitespace-nowrap transition-colors ${
                index === currentPlayerIndex
                  ? 'bg-blue-600 text-white'
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              }`}
            >
              {getPlayerName(result.playerId)}
              {result.isSpectator ? (
                <span
                  className={`ml-2 ${
                    index === currentPlayerIndex
                      ? 'text-blue-100'
                      : 'text-gray-500'
                  }`}
                >
                  (Spectator)
                </span>
              ) : (
                <span
                  className={`ml-2 ${
                    index === currentPlayerIndex
                      ? 'text-blue-100'
                      : playerProfit > 0
                        ? 'text-green-600'
                        : playerProfit < 0
                          ? 'text-red-600'
                          : 'text-gray-600'
                  }`}
                >
                  {playerProfit > 0 ? '+' : playerProfit < 0 ? '-' : ''}
                  {formatCents(Math.abs(playerProfit))}
                </span>
              )}
            </button>
          )
        })}
      </div>

      {/* Result Entry Form */}
      <div className="bg-white border-2 border-gray-200 rounded-lg p-6 space-y-6">
        {currentResult.isSpectator ? (
          <div className="bg-gray-50 border border-gray-300 rounded-lg p-6 text-center">
            <div className="text-gray-700 mb-4">
              <span className="text-4xl">👀</span>
            </div>
            <h3 className="text-lg font-semibold text-gray-900 mb-2">
              Spectator (Absent Player)
            </h3>
            <p className="text-gray-600 mb-4">
              This player was absent from the session but will have view access.
            </p>
            <div className="bg-white rounded border border-gray-200 p-4 inline-block">
              <div className="text-sm text-gray-600 mb-1">Buy-in / Cash-out</div>
              <div className="text-2xl font-semibold text-gray-900">0 PLN / 0 PLN</div>
            </div>
          </div>
        ) : (
          <>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Buy-In (PLN) *
              </label>
              <input
                type="number"
                step="0.01"
                min="0"
                value={formatCurrency(currentResult.buyInCents)}
                onChange={(e) =>
                  updateResult(currentResult.playerId, {
                    buyInCents: parseCurrency(e.target.value),
                  })
                }
                className={`w-full px-4 py-3 text-2xl border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
                  errors[`buyIn-${currentPlayerIndex}`]
                    ? 'border-red-500'
                    : 'border-gray-300'
                }`}
                placeholder="100.00"
              />
              {errors[`buyIn-${currentPlayerIndex}`] && (
                <p className="text-red-500 text-sm mt-1">
                  {errors[`buyIn-${currentPlayerIndex}`]}
                </p>
              )}
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Cash-Out (PLN) *
              </label>
              <input
                type="number"
                step="0.01"
                min="0"
                value={formatCurrency(currentResult.cashOutCents)}
                onChange={(e) =>
                  updateResult(currentResult.playerId, {
                    cashOutCents: parseCurrency(e.target.value),
                  })
                }
                className={`w-full px-4 py-3 text-2xl border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
                  errors[`cashOut-${currentPlayerIndex}`]
                    ? 'border-red-500'
                    : 'border-gray-300'
                }`}
                placeholder="150.00"
              />
              {errors[`cashOut-${currentPlayerIndex}`] && (
                <p className="text-red-500 text-sm mt-1">
                  {errors[`cashOut-${currentPlayerIndex}`]}
                </p>
              )}
            </div>

            {/* Profit/Loss Display */}
            <div
              className={`p-4 rounded-lg ${
                profit > 0
                  ? 'bg-green-50 border border-green-200'
                  : profit < 0
                    ? 'bg-red-50 border border-red-200'
                    : 'bg-gray-50 border border-gray-200'
              }`}
            >
              <div className="text-sm text-gray-700 mb-1">Profit/Loss</div>
              <div
                className={`text-3xl font-bold ${
                  profit > 0
                    ? 'text-green-600'
                    : profit < 0
                      ? 'text-red-600'
                      : 'text-gray-600'
                }`}
              >
                {profit > 0 ? '+' : profit < 0 ? '-' : ''}
                {formatCents(Math.abs(profit))}
              </div>
            </div>

            {/* Notes */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Notes (Optional)
              </label>
              <textarea
                value={currentResult.notes}
                onChange={(e) =>
                  updateResult(currentResult.playerId, { notes: e.target.value })
                }
                rows={2}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="Any notes about this player's session..."
              />
            </div>
          </>
        )}
      </div>

      {/* Summary Panel (Sticky) */}
      <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
        <h3 className="font-semibold text-blue-900 mb-3">Session Summary</h3>
        <div className="grid grid-cols-3 gap-4 text-sm">
          <div>
            <div className="text-blue-700">Total Buy-In</div>
            <div className="font-semibold text-blue-900">
              {formatCents(totalBuyIn)}
            </div>
          </div>
          <div>
            <div className="text-blue-700">Total Cash-Out</div>
            <div className="font-semibold text-blue-900">
              {formatCents(totalCashOut)}
            </div>
          </div>
          <div>
            <div className="text-blue-700">Discrepancy</div>
            <div
              className={`font-semibold ${
                discrepancy !== 0 ? 'text-orange-600' : 'text-green-600'
              }`}
            >
              {discrepancy > 0 ? '+' : discrepancy < 0 ? '-' : ''}
              {formatCents(Math.abs(discrepancy))}
            </div>
          </div>
        </div>
        {discrepancy !== 0 && (
          <div className="mt-3 text-sm text-orange-700 bg-orange-50 border border-orange-200 rounded p-2">
            ⚠️ Buy-ins and cash-outs don't balance. This is okay but may
            indicate a data entry error.
          </div>
        )}
      </div>

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
          Next: Review & Submit
        </button>
      </div>
    </div>
  )
}
