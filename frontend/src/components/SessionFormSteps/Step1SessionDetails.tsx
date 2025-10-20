import { useState } from 'react'
import { GameType } from '../../types/gameSession'
import type { SessionFormData } from '../../types/sessionForm'

interface Step1Props {
  formData: SessionFormData
  updateFormData: (updates: Partial<SessionFormData>) => void
  onNext: () => void
}

const commonLocations = [
  "John's House",
  "Mike's Place",
  'Downtown Poker Club',
  'Casino Night',
]

export function Step1SessionDetails({
  formData,
  updateFormData,
  onNext,
}: Step1Props) {
  const [showCustomLocation, setShowCustomLocation] = useState(
    !commonLocations.includes(formData.location) && formData.location !== '',
  )
  const [errors, setErrors] = useState<Record<string, string>>({})

  const validate = (): boolean => {
    const newErrors: Record<string, string> = {}

    if (!formData.location.trim()) {
      newErrors.location = 'Location is required'
    }

    if (!formData.startTime) {
      newErrors.startTime = 'Start time is required'
    }

    if (formData.endTime && formData.endTime < formData.startTime) {
      newErrors.endTime = 'End time must be after start time'
    }

    if (formData.minBuyInCents <= 0) {
      newErrors.minBuyInCents = 'Min buy-in must be greater than 0'
    }

    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const handleNext = () => {
    if (validate()) {
      onNext()
    }
  }

  const handleLocationSelect = (location: string) => {
    if (location === 'custom') {
      setShowCustomLocation(true)
      updateFormData({ location: '' })
    } else {
      setShowCustomLocation(false)
      updateFormData({ location })
    }
  }

  const formatCurrency = (cents: number): string => {
    return (cents / 100).toFixed(2)
  }

  const parseCurrency = (value: string): number => {
    const num = parseFloat(value)
    return isNaN(num) ? 0 : Math.round(num * 100)
  }

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-xl font-semibold text-gray-900 mb-4">
          Session Details
        </h2>
        <p className="text-gray-600 mb-6">
          Enter the basic information about this poker session
        </p>
      </div>

      {/* Date and Time */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Start Time *
          </label>
          <input
            type="datetime-local"
            value={formData.startTime}
            onChange={(e) => updateFormData({ startTime: e.target.value })}
            className={`w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
              errors.startTime ? 'border-red-500' : 'border-gray-300'
            }`}
          />
          {errors.startTime && (
            <p className="text-red-500 text-sm mt-1">{errors.startTime}</p>
          )}
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            End Time (Optional)
          </label>
          <input
            type="datetime-local"
            value={formData.endTime}
            onChange={(e) => updateFormData({ endTime: e.target.value })}
            className={`w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
              errors.endTime ? 'border-red-500' : 'border-gray-300'
            }`}
          />
          {errors.endTime && (
            <p className="text-red-500 text-sm mt-1">{errors.endTime}</p>
          )}
        </div>
      </div>

      {/* Location */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Location *
        </label>
        {!showCustomLocation ? (
          <div className="space-y-2">
            <select
              value={formData.location}
              onChange={(e) => handleLocationSelect(e.target.value)}
              className={`w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
                errors.location ? 'border-red-500' : 'border-gray-300'
              }`}
            >
              <option value="">Select a location...</option>
              {commonLocations.map((loc) => (
                <option key={loc} value={loc}>
                  {loc}
                </option>
              ))}
              <option value="custom">+ Add custom location</option>
            </select>
          </div>
        ) : (
          <div className="flex gap-2">
            <input
              type="text"
              value={formData.location}
              onChange={(e) => updateFormData({ location: e.target.value })}
              placeholder="Enter location name"
              className={`flex-1 px-3 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
                errors.location ? 'border-red-500' : 'border-gray-300'
              }`}
            />
            <button
              type="button"
              onClick={() => setShowCustomLocation(false)}
              className="px-3 py-2 text-gray-600 hover:text-gray-800"
            >
              Cancel
            </button>
          </div>
        )}
        {errors.location && (
          <p className="text-red-500 text-sm mt-1">{errors.location}</p>
        )}
      </div>

      {/* Game Type */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Game Type *
        </label>
        <select
          value={formData.gameType}
          onChange={(e) =>
            updateFormData({ gameType: e.target.value as GameType })
          }
          className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
        >
          <option value={GameType.TEXAS_HOLDEM}>Texas Hold'em</option>
          <option value={GameType.OMAHA}>Omaha</option>
          <option value={GameType.OMAHA_HI_LO}>Omaha Hi-Lo</option>
          <option value={GameType.SEVEN_CARD_STUD}>Seven Card Stud</option>
          <option value={GameType.FIVE_CARD_DRAW}>Five Card Draw</option>
          <option value={GameType.MIXED_GAMES}>Mixed Games</option>
          <option value={GameType.OTHER}>Other</option>
        </select>
      </div>

      {/* Min Buy-In */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Minimum Buy-In ($) *
        </label>
        <input
          type="number"
          step="0.01"
          min="0"
          value={formatCurrency(formData.minBuyInCents)}
          onChange={(e) =>
            updateFormData({ minBuyInCents: parseCurrency(e.target.value) })
          }
          className={`w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
            errors.minBuyInCents ? 'border-red-500' : 'border-gray-300'
          }`}
          placeholder="50.00"
        />
        {errors.minBuyInCents && (
          <p className="text-red-500 text-sm mt-1">{errors.minBuyInCents}</p>
        )}
      </div>

      {/* Session Notes */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Session Notes (Optional)
        </label>
        <textarea
          value={formData.sessionNotes}
          onChange={(e) => updateFormData({ sessionNotes: e.target.value })}
          rows={3}
          className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          placeholder="Any notes about this session..."
        />
      </div>

      {/* Navigation */}
      <div className="flex justify-end pt-4 border-t">
        <button
          type="button"
          onClick={handleNext}
          className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
        >
          Next: Select Players
        </button>
      </div>
    </div>
  )
}
