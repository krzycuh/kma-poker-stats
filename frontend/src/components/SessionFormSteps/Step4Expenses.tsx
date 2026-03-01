import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { playerApi } from '../../api/players'
import type { SessionFormData, ExpenseFormEntry } from '../../types/sessionForm'
import { formatCents } from '../../utils/format'

interface Step4Props {
  formData: SessionFormData
  updateFormData: (updates: Partial<SessionFormData>) => void
  onNext: () => void
  onPrev: () => void
}

export function Step4Expenses({
  formData,
  updateFormData,
  onNext,
  onPrev,
}: Step4Props) {
  const [errors, setErrors] = useState<Record<string, string>>({})

  const { data: allPlayers = [] } = useQuery({
    queryKey: ['players'],
    queryFn: () => playerApi.list({ includeInactive: false }),
  })

  const sessionPlayers = allPlayers.filter((p) =>
    formData.selectedPlayerIds.includes(p.id),
  )

  const getPlayerName = (playerId: string): string => {
    const player = allPlayers.find((p) => p.id === playerId)
    return player?.name || 'Unknown Player'
  }

  const formatCurrency = (cents: number): string => {
    return (cents / 100).toFixed(2)
  }

  const parseCurrency = (value: string): number => {
    const num = parseFloat(value)
    return isNaN(num) ? 0 : Math.round(num * 100)
  }

  const addExpense = () => {
    const newExpense: ExpenseFormEntry = {
      payerPlayerId: sessionPlayers[0]?.id || '',
      description: '',
      amountCents: 0,
    }
    updateFormData({ expenses: [...formData.expenses, newExpense] })
  }

  const removeExpense = (index: number) => {
    const newExpenses = formData.expenses.filter((_, i) => i !== index)
    updateFormData({ expenses: newExpenses })
    // Clear errors for removed item
    const newErrors = { ...errors }
    delete newErrors[`desc-${index}`]
    delete newErrors[`amount-${index}`]
    delete newErrors[`payer-${index}`]
    setErrors(newErrors)
  }

  const updateExpense = (
    index: number,
    updates: Partial<ExpenseFormEntry>,
  ) => {
    const newExpenses = formData.expenses.map((e, i) =>
      i === index ? { ...e, ...updates } : e,
    )
    updateFormData({ expenses: newExpenses })
  }

  const totalExpensesCents = formData.expenses.reduce(
    (sum, e) => sum + e.amountCents,
    0,
  )

  const activePlayerCount = formData.results.filter(
    (r) => !r.isSpectator,
  ).length

  const perPlayerShareCents =
    activePlayerCount > 0
      ? Math.floor(totalExpensesCents / activePlayerCount)
      : 0

  const validate = (): boolean => {
    const newErrors: Record<string, string> = {}

    formData.expenses.forEach((expense, index) => {
      if (!expense.description.trim()) {
        newErrors[`desc-${index}`] = 'Description is required'
      }
      if (expense.amountCents <= 0) {
        newErrors[`amount-${index}`] = 'Amount must be greater than 0'
      }
      if (!expense.payerPlayerId) {
        newErrors[`payer-${index}`] = 'Select who paid'
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

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-xl font-semibold text-gray-900 mb-4">
          Shared Expenses
        </h2>
        <p className="text-gray-600 mb-6">
          Add any shared costs (food, drinks, etc.) to split among players.
          This step is optional.
        </p>
      </div>

      {/* Expense List */}
      <div className="space-y-4">
        {formData.expenses.map((expense, index) => (
          <div
            key={index}
            className="bg-white border-2 border-gray-200 rounded-lg p-4 space-y-3"
          >
            <div className="flex items-center justify-between">
              <span className="text-sm font-medium text-gray-500">
                Expense #{index + 1}
              </span>
              <button
                type="button"
                onClick={() => removeExpense(index)}
                className="text-red-500 hover:text-red-700 text-sm"
              >
                Remove
              </button>
            </div>

            {/* Description */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Description *
              </label>
              <input
                type="text"
                value={expense.description}
                onChange={(e) =>
                  updateExpense(index, { description: e.target.value })
                }
                className={`w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
                  errors[`desc-${index}`]
                    ? 'border-red-500'
                    : 'border-gray-300'
                }`}
                placeholder="e.g., Pizza, Beer, Snacks..."
              />
              {errors[`desc-${index}`] && (
                <p className="text-red-500 text-sm mt-1">
                  {errors[`desc-${index}`]}
                </p>
              )}
            </div>

            <div className="grid grid-cols-2 gap-3">
              {/* Amount */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Amount (PLN) *
                </label>
                <input
                  type="number"
                  step="0.01"
                  min="0"
                  value={formatCurrency(expense.amountCents)}
                  onChange={(e) =>
                    updateExpense(index, {
                      amountCents: parseCurrency(e.target.value),
                    })
                  }
                  className={`w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
                    errors[`amount-${index}`]
                      ? 'border-red-500'
                      : 'border-gray-300'
                  }`}
                  placeholder="50.00"
                />
                {errors[`amount-${index}`] && (
                  <p className="text-red-500 text-sm mt-1">
                    {errors[`amount-${index}`]}
                  </p>
                )}
              </div>

              {/* Payer */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Paid by *
                </label>
                <select
                  value={expense.payerPlayerId}
                  onChange={(e) =>
                    updateExpense(index, { payerPlayerId: e.target.value })
                  }
                  className={`w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
                    errors[`payer-${index}`]
                      ? 'border-red-500'
                      : 'border-gray-300'
                  }`}
                >
                  <option value="">Select player</option>
                  {sessionPlayers.map((player) => (
                    <option key={player.id} value={player.id}>
                      {player.name}
                    </option>
                  ))}
                </select>
                {errors[`payer-${index}`] && (
                  <p className="text-red-500 text-sm mt-1">
                    {errors[`payer-${index}`]}
                  </p>
                )}
              </div>
            </div>
          </div>
        ))}

        {/* Add Expense Button */}
        <button
          type="button"
          onClick={addExpense}
          className="w-full py-3 border-2 border-dashed border-gray-300 rounded-lg text-gray-600 hover:border-blue-400 hover:text-blue-600 transition-colors flex items-center justify-center gap-2"
        >
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
              d="M12 4v16m8-8H4"
            />
          </svg>
          Add Expense
        </button>
      </div>

      {/* Summary Panel */}
      {formData.expenses.length > 0 && (
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
          <h3 className="font-semibold text-blue-900 mb-3">
            Cost Split Summary
          </h3>
          <div className="grid grid-cols-3 gap-4 text-sm">
            <div>
              <div className="text-blue-700">Total Expenses</div>
              <div className="font-semibold text-blue-900">
                {formatCents(totalExpensesCents)}
              </div>
            </div>
            <div>
              <div className="text-blue-700">Active Players</div>
              <div className="font-semibold text-blue-900">
                {activePlayerCount}
              </div>
            </div>
            <div>
              <div className="text-blue-700">Per Player</div>
              <div className="font-semibold text-blue-900">
                ~{formatCents(perPlayerShareCents)}
              </div>
            </div>
          </div>

          {/* Per-player breakdown */}
          <div className="mt-4 space-y-2">
            <div className="text-sm font-medium text-blue-800">
              Player Balances:
            </div>
            {formData.results
              .filter((r) => !r.isSpectator)
              .map((result, idx) => {
                const paid = formData.expenses
                  .filter((e) => e.payerPlayerId === result.playerId)
                  .reduce((sum, e) => sum + e.amountCents, 0)
                const owed =
                  perPlayerShareCents +
                  (idx <
                  (activePlayerCount > 0
                    ? totalExpensesCents % activePlayerCount
                    : 0)
                    ? 1
                    : 0)
                const balance = paid - owed

                return (
                  <div
                    key={result.playerId}
                    className="flex items-center justify-between text-sm bg-white rounded px-3 py-2"
                  >
                    <span className="text-gray-700">
                      {getPlayerName(result.playerId)}
                    </span>
                    <div className="flex items-center gap-4">
                      <span className="text-gray-500">
                        paid {formatCents(paid)}
                      </span>
                      <span
                        className={`font-semibold ${
                          balance > 0
                            ? 'text-green-600'
                            : balance < 0
                              ? 'text-red-600'
                              : 'text-gray-600'
                        }`}
                      >
                        {balance > 0
                          ? `+${formatCents(balance)}`
                          : balance < 0
                            ? `-${formatCents(Math.abs(balance))}`
                            : 'settled'}
                      </span>
                    </div>
                  </div>
                )
              })}
          </div>
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
          Next: Review & Submit
        </button>
      </div>
    </div>
  )
}
