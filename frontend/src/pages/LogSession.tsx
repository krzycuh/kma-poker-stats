import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useMutation } from '@tanstack/react-query'
import { MultiStepForm } from '../components/MultiStepForm'
import { PageHeader } from '../components/PageHeader'
import { Step1SessionDetails } from '../components/SessionFormSteps/Step1SessionDetails'
import { Step2PlayerSelection } from '../components/SessionFormSteps/Step2PlayerSelection'
import { Step3ResultsEntry } from '../components/SessionFormSteps/Step3ResultsEntry'
import { Step4ReviewSubmit } from '../components/SessionFormSteps/Step4ReviewSubmit'
import { sessionApi } from '../api/sessions'
import type {
  CreateGameSessionRequest,
} from '../types/gameSession'
import { GameType } from '../types/gameSession'
import { useAuth } from '../hooks/useAuth'
import { UserRole } from '../types/auth'
import type { SessionFormData } from '../types/sessionForm'

const DRAFT_KEY = 'session-draft'

const steps = [
  { id: 1, title: 'Session Details', description: 'When & where' },
  { id: 2, title: 'Select Players', description: 'Who played' },
  { id: 3, title: 'Enter Results', description: 'Buy-ins & cash-outs' },
  { id: 4, title: 'Review & Submit', description: 'Confirm details' },
]

export default function LogSession() {
  const navigate = useNavigate()
  const { user } = useAuth()
  const [currentStep, setCurrentStep] = useState(0)
  const [formData, setFormData] = useState<SessionFormData>({
    startTime: new Date().toISOString().slice(0, 16),
    endTime: '',
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

  // Load draft from localStorage
  useEffect(() => {
    const draft = localStorage.getItem(DRAFT_KEY)
    if (draft) {
      try {
        const parsed = JSON.parse(draft)
        setFormData(parsed)
      } catch (err) {
        console.error('Failed to load draft:', err)
      }
    }
  }, [])

  // Save draft to localStorage
  const saveDraft = (data: SessionFormData) => {
    localStorage.setItem(DRAFT_KEY, JSON.stringify(data))
  }

  // Clear draft
  const clearDraft = () => {
    localStorage.removeItem(DRAFT_KEY)
  }

  // Create session mutation
  const createSessionMutation = useMutation({
    mutationFn: (data: CreateGameSessionRequest) => sessionApi.create(data),
    onSuccess: () => {
      clearDraft()
      setCurrentStep(4) // Success screen
    },
  })

  const updateFormData = (updates: Partial<SessionFormData>) => {
    const newData = { ...formData, ...updates }
    setFormData(newData)
    saveDraft(newData)
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
      startTime: formData.startTime,
      endTime: formData.endTime || null,
      location: formData.location,
      gameType: formData.gameType,
      minBuyInCents: formData.minBuyInCents,
      notes: formData.sessionNotes || null,
      results: formData.results.map((r) => ({
        playerId: r.playerId,
        buyInCents: r.buyInCents,
        cashOutCents: r.cashOutCents,
        notes: r.notes || null,
      })),
    }

    createSessionMutation.mutate(request)
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8 px-4">
      <div className="max-w-4xl mx-auto">
        <PageHeader
          title="Log New Session"
          description="Record a poker game session with all player results"
        />
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
            isSubmitting={createSessionMutation.isPending}
            error={createSessionMutation.error}
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
        Session Logged Successfully!
      </h2>
      <p className="text-gray-600 mb-8">
        The game session has been recorded with all player results.
      </p>

      <div className="flex flex-col sm:flex-row gap-4 justify-center">
        <button
          onClick={() => navigate('/')}
          className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
        >
          Back to Dashboard
        </button>
        <button
          onClick={() => window.location.reload()}
          className="px-6 py-3 bg-white text-blue-600 border border-blue-600 rounded-lg hover:bg-blue-50 transition-colors"
        >
          Log Another Session
        </button>
      </div>
    </div>
  )
}
