import { useQuery } from '@tanstack/react-query'
import { playerApi } from '../../api/players'
import type { SessionFormData } from '../../types/sessionForm'

interface Step4Props {
  formData: SessionFormData
  onSubmit: () => void
  onPrev: () => void
  goToStep: (step: number) => void
  isSubmitting: boolean
  error: Error | null
}

const GAME_TYPE_LABELS: Record<string, string> = {
  TEXAS_HOLDEM: "Texas Hold'em",
  OMAHA: 'Omaha',
  OMAHA_HI_LO: 'Omaha Hi-Lo',
  SEVEN_CARD_STUD: 'Seven Card Stud',
  FIVE_CARD_DRAW: 'Five Card Draw',
  MIXED_GAMES: 'Mixed Games',
  OTHER: 'Other',
}

export function Step4ReviewSubmit({
  formData,
  onSubmit,
  onPrev,
  goToStep,
  isSubmitting,
  error,
}: Step4Props) {
  const { data: allPlayers = [] } = useQuery({
    queryKey: ['players'],
    queryFn: () => playerApi.list({ includeInactive: false }),
  })

  const getPlayerName = (playerId: string): string => {
    const player = allPlayers.find((p) => p.id === playerId)
    return player?.name || 'Unknown Player'
  }

  const formatCurrency = (cents: number): string => {
    return (cents / 100).toFixed(2)
  }

  const formatDateTime = (dateTime: string): string => {
    if (!dateTime) return 'Not set'
    const date = new Date(dateTime)
    return date.toLocaleString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
      hour: 'numeric',
      minute: '2-digit',
      hour12: true,
    })
  }

  const calculateProfit = (
    buyInCents: number,
    cashOutCents: number,
  ): number => {
    return cashOutCents - buyInCents
  }

  const calculateTotals = () => {
    const totalBuyIn = formData.results.reduce(
      (sum, r) => sum + r.buyInCents,
      0,
    )
    const totalCashOut = formData.results.reduce(
      (sum, r) => sum + r.cashOutCents,
      0,
    )
    const discrepancy = totalCashOut - totalBuyIn
    return { totalBuyIn, totalCashOut, discrepancy }
  }

  const { totalBuyIn, totalCashOut, discrepancy } = calculateTotals()

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-xl font-semibold text-gray-900 mb-4">
          Review & Submit
        </h2>
        <p className="text-gray-600 mb-6">
          Please review all details before submitting
        </p>
      </div>

      {/* Error Display */}
      {error && (
        <div className="bg-red-50 border border-red-200 rounded-lg p-4">
          <p className="text-red-800">
            Error submitting session: {error.message}
          </p>
        </div>
      )}

      {/* Session Details */}
      <div className="bg-white border border-gray-200 rounded-lg p-6">
        <div className="flex justify-between items-start mb-4">
          <h3 className="text-lg font-semibold text-gray-900">
            Session Details
          </h3>
          <button
            type="button"
            onClick={() => goToStep(0)}
            className="text-blue-600 hover:text-blue-700 text-sm"
          >
            Edit
          </button>
        </div>

        <div className="grid grid-cols-2 gap-4 text-sm">
          <div>
            <div className="text-gray-600">Start Time</div>
            <div className="font-medium text-gray-900">
              {formatDateTime(formData.startTime)}
            </div>
          </div>
          <div>
            <div className="text-gray-600">End Time</div>
            <div className="font-medium text-gray-900">
              {formData.endTime
                ? formatDateTime(formData.endTime)
                : 'Not set'}
            </div>
          </div>
          <div>
            <div className="text-gray-600">Location</div>
            <div className="font-medium text-gray-900">{formData.location}</div>
          </div>
          <div>
            <div className="text-gray-600">Game Type</div>
            <div className="font-medium text-gray-900">
              {GAME_TYPE_LABELS[formData.gameType] || formData.gameType}
            </div>
          </div>
          <div>
            <div className="text-gray-600">Min Buy-In</div>
            <div className="font-medium text-gray-900">
              ${formatCurrency(formData.minBuyInCents)}
            </div>
          </div>
          {formData.sessionNotes && (
            <div className="col-span-2">
              <div className="text-gray-600">Session Notes</div>
              <div className="font-medium text-gray-900">
                {formData.sessionNotes}
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Player Results */}
      <div className="bg-white border border-gray-200 rounded-lg p-6">
        <div className="flex justify-between items-start mb-4">
          <h3 className="text-lg font-semibold text-gray-900">
            Player Results
          </h3>
          <button
            type="button"
            onClick={() => goToStep(2)}
            className="text-blue-600 hover:text-blue-700 text-sm"
          >
            Edit
          </button>
        </div>

        <div className="space-y-3">
          {formData.results
            .sort((a, b) => {
              const profitA = calculateProfit(a.buyInCents, a.cashOutCents)
              const profitB = calculateProfit(b.buyInCents, b.cashOutCents)
              return profitB - profitA
            })
            .map((result) => {
              const profit = calculateProfit(
                result.buyInCents,
                result.cashOutCents,
              )
              return (
                <div
                  key={result.playerId}
                  className="flex items-center justify-between p-3 bg-gray-50 rounded-lg"
                >
                  <div className="flex-1">
                    <div className="font-medium text-gray-900">
                      {getPlayerName(result.playerId)}
                    </div>
                    <div className="text-sm text-gray-600">
                      Buy-in: ${formatCurrency(result.buyInCents)} → Cash-out:
                      ${formatCurrency(result.cashOutCents)}
                    </div>
                    {result.notes && (
                      <div className="text-sm text-gray-500 mt-1 italic">
                        {result.notes}
                      </div>
                    )}
                  </div>
                  <div
                    className={`text-lg font-semibold ${
                      profit > 0
                        ? 'text-green-600'
                        : profit < 0
                          ? 'text-red-600'
                          : 'text-gray-600'
                    }`}
                  >
                    {profit > 0 ? '+' : ''}${formatCurrency(Math.abs(profit))}
                  </div>
                </div>
              )
            })}
        </div>

        {/* Totals */}
        <div className="mt-4 pt-4 border-t border-gray-200">
          <div className="grid grid-cols-3 gap-4 text-sm">
            <div>
              <div className="text-gray-600">Total Buy-In</div>
              <div className="font-semibold text-gray-900">
                ${formatCurrency(totalBuyIn)}
              </div>
            </div>
            <div>
              <div className="text-gray-600">Total Cash-Out</div>
              <div className="font-semibold text-gray-900">
                ${formatCurrency(totalCashOut)}
              </div>
            </div>
            <div>
              <div className="text-gray-600">Discrepancy</div>
              <div
                className={`font-semibold ${
                  discrepancy !== 0 ? 'text-orange-600' : 'text-green-600'
                }`}
              >
                {discrepancy > 0 ? '+' : ''}$
                {formatCurrency(Math.abs(discrepancy))}
              </div>
            </div>
          </div>
          {discrepancy !== 0 && (
            <div className="mt-3 text-sm text-orange-700 bg-orange-50 border border-orange-200 rounded p-2">
              ⚠️ Buy-ins and cash-outs don't balance by $
              {formatCurrency(Math.abs(discrepancy))}
            </div>
          )}
        </div>
      </div>

      {/* Navigation */}
      <div className="flex justify-between pt-4 border-t">
        <button
          type="button"
          onClick={onPrev}
          disabled={isSubmitting}
          className="px-6 py-2 text-gray-700 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors disabled:opacity-50"
        >
          Back
        </button>
        <button
          type="button"
          onClick={onSubmit}
          disabled={isSubmitting}
          className="px-6 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors disabled:opacity-50 flex items-center gap-2"
        >
          {isSubmitting ? (
            <>
              <div className="inline-block animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
              Submitting...
            </>
          ) : (
            <>
              <svg
                className="w-5 h-5"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M5 13l4 4L19 7"
                />
              </svg>
              Submit Session
            </>
          )}
        </button>
      </div>
    </div>
  )
}
