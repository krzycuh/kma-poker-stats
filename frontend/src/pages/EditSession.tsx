import { useState, useEffect } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useMutation, useQuery } from '@tanstack/react-query'
import { MultiStepForm } from '../components/MultiStepForm'
import { Step1SessionDetails } from '../components/SessionFormSteps/Step1SessionDetails'
import { Step2PlayerSelection } from '../components/SessionFormSteps/Step2PlayerSelection'
import { Step3ResultsEntry } from '../components/SessionFormSteps/Step3ResultsEntry'
import { Step4ReviewSubmit } from '../components/SessionFormSteps/Step4ReviewSubmit'
import { sessionApi } from '../api/sessions'
import type { CreateGameSessionRequest } from '../types/gameSession'
import { GameType } from '../types/gameSession'
import { useAuth } from '../hooks/useAuth'
import { UserRole } from '../types/auth'
import type { SessionFormData } from '../types/sessionForm'

const steps = [
  { id: 1, title: 'Session Details', description: 'When & where' },
  { id: 2, title: 'Select Players', description: 'Who played' },
  { id: 3, title: 'Enter Results', description: 'Buy-ins & cash-outs' },
  { id: 4, title: 'Review & Submit', description: 'Confirm changes' },
]

export default function EditSession() {
  const navigate = useNavigate()
  const { id } = useParams<{ id: string }>()
  const { user } = useAuth()
  const [currentStep, setCurrentStep] = useState(0)
  const [formData, setFormData] = useState<SessionFormData>({
    sessionDate: '',
    location: '',
    gameType: GameType.TEXAS_HOLDEM,
    minBuyInCents: 5000,
    sessionNotes: '',
    selectedPlayerIds: [],
    results: [],
  })

  // Redirect non-admin users
  useEffect(() => {
    if (user && user.role !== UserRole.ADMIN) {
      navigate('/')
    }
  }, [user, navigate])

  // Load existing session
  const { data: session, isLoading } = useQuery({
    queryKey: ['session', id],
    queryFn: () => sessionApi.get(id!),
    enabled: !!id,
  })

  // Populate form when session loads
  useEffect(() => {
    if (session) {
      setFormData({
        sessionDate: session.session.startTime.slice(0, 10),
        location: session.session.location,
        gameType: session.session.gameType as GameType,
        minBuyInCents: session.session.minBuyInCents,
        sessionNotes: session.session.notes || '',
        selectedPlayerIds: session.results.map((r) => r.playerId),
        results: session.results.map((r) => ({
          playerId: r.playerId,
          buyInCents: r.buyInCents,
          cashOutCents: r.cashOutCents,
          notes: r.notes || '',
          isSpectator: r.isSpectator,
        })),
      })
    }
  }, [session])

  // Update session mutation - deletes old session and creates new one
  const updateSessionMutation = useMutation({
    mutationFn: async (data: CreateGameSessionRequest) => {
      // Delete the old session first
      await sessionApi.delete(id!)

      // Then create a new session with all the updated data
      return await sessionApi.create(data)
    },
    onSuccess: () => {
      setCurrentStep(4) // Success screen
    },
  })

  const updateFormData = (updates: Partial<SessionFormData>) => {
    setFormData({ ...formData, ...updates })
  }

  const nextStep = () => {
    if (currentStep < steps.length - 1) {
      setCurrentStep(currentStep + 1)
    }
  }

  const prevStep = () => {
    if (currentStep > 0) {
      setCurrentStep(currentStep - 1)
    }
  }

  const goToStep = (step: number) => {
    setCurrentStep(step)
  }

  const handleSubmit = () => {
    const request: CreateGameSessionRequest = {
      startTime: `${formData.sessionDate}T00:00`,
      location: formData.location,
      gameType: formData.gameType,
      minBuyInCents: formData.minBuyInCents,
      notes: formData.sessionNotes || null,
      results: formData.results.map((r) => ({
        playerId: r.playerId,
        buyInCents: r.buyInCents,
        cashOutCents: r.cashOutCents,
        notes: r.notes || null,
        isSpectator: r.isSpectator,
      })),
    }

    updateSessionMutation.mutate(request)
  }

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
          <p className="text-gray-600 mt-4">Loading session...</p>
        </div>
      </div>
    )
  }

  if (!session) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <p className="text-red-600">Session not found</p>
          <button
            onClick={() => navigate('/')}
            className="mt-4 px-4 py-2 bg-blue-600 text-white rounded-lg"
          >
            Back to Dashboard
          </button>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-50 pt-4 pb-8 px-4">
      <div className="max-w-4xl mx-auto mb-8">
        <button
          onClick={() => navigate('/')}
          className="text-blue-600 hover:text-blue-700 flex items-center gap-2 mb-2"
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
              d="M10 19l-7-7m0 0l7-7m-7 7h18"
            />
          </svg>
          Back to Dashboard
        </button>
        <h1 className="text-3xl font-bold text-gray-900">Edit Session</h1>
        <p className="text-gray-600 mt-2">
          Update the session details and player results
        </p>
      </div>

      <MultiStepForm steps={steps} currentStep={currentStep}>
        {currentStep === 0 && (
          <Step1SessionDetails
            formData={formData}
            updateFormData={updateFormData}
            onNext={nextStep}
          />
        )}
        {currentStep === 1 && (
          <Step2PlayerSelection
            formData={formData}
            updateFormData={updateFormData}
            onNext={nextStep}
            onPrev={prevStep}
          />
        )}
        {currentStep === 2 && (
          <Step3ResultsEntry
            formData={formData}
            updateFormData={updateFormData}
            onNext={nextStep}
            onPrev={prevStep}
          />
        )}
        {currentStep === 3 && (
          <Step4ReviewSubmit
            formData={formData}
            onSubmit={handleSubmit}
            onPrev={prevStep}
            goToStep={goToStep}
            isSubmitting={updateSessionMutation.isPending}
            error={updateSessionMutation.error}
          />
        )}
        {currentStep === 4 && <SuccessScreen />}
      </MultiStepForm>
    </div>
  )
}

function SuccessScreen() {
  const navigate = useNavigate()

  return (
    <div className="text-center py-12">
      <div className="mb-6">
        <div className="mx-auto w-16 h-16 bg-green-100 rounded-full flex items-center justify-center">
          <svg
            className="w-10 h-10 text-green-600"
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
        </div>
      </div>

      <h2 className="text-2xl font-bold text-gray-900 mb-2">
        Session Updated Successfully!
      </h2>
      <p className="text-gray-600 mb-8">
        The game session has been updated with all your changes including player
        results and spectator status.
      </p>

      <div className="flex flex-col sm:flex-row gap-4 justify-center">
        <button
          onClick={() => navigate('/')}
          className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
        >
          Back to Dashboard
        </button>
      </div>
    </div>
  )
}
