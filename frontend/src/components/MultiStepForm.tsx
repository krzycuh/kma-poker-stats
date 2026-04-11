import React from 'react'

interface Step {
  id: number
  title: string
  description: string
}

interface MultiStepFormProps {
  steps: Step[]
  currentStep: number
  children: React.ReactNode
}

export const MultiStepForm: React.FC<MultiStepFormProps> = ({
  steps,
  currentStep,
  children,
}) => {
  return (
    <div className="max-w-4xl mx-auto">
      {/* Progress Indicator */}
      <div className="mb-4 sm:mb-6">
        <div className="flex items-center justify-between">
          {steps.map((step, index) => (
            <React.Fragment key={step.id}>
              <div className="flex flex-col items-center flex-1 min-w-0">
                {/* Step Circle */}
                <div
                  className={`w-7 h-7 sm:w-10 sm:h-10 rounded-full flex items-center justify-center text-xs sm:text-base font-semibold mb-1 sm:mb-2 transition-colors ${
                    index < currentStep
                      ? 'bg-green-500 text-white'
                      : index === currentStep
                        ? 'bg-blue-600 text-white'
                        : 'bg-gray-300 text-gray-600'
                  }`}
                >
                  {index < currentStep ? (
                    <svg
                      className="w-4 h-4 sm:w-6 sm:h-6"
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
                  ) : (
                    step.id
                  )}
                </div>
                {/* Step Title */}
                <div className="text-center">
                  <div
                    className={`text-[11px] sm:text-sm font-medium truncate ${
                      index === currentStep
                        ? 'text-blue-600'
                        : index < currentStep
                          ? 'text-green-600'
                          : 'text-gray-500'
                    }`}
                  >
                    {step.title}
                  </div>
                  <div className="text-xs text-gray-500 hidden sm:block">
                    {step.description}
                  </div>
                </div>
              </div>
              {/* Connector Line */}
              {index < steps.length - 1 && (
                <div className="flex-1 mx-1 sm:mx-2 mb-6 sm:mb-8 -mt-4 sm:-mt-0">
                  <div
                    className={`h-0.5 sm:h-1 rounded transition-colors ${
                      index < currentStep ? 'bg-green-500' : 'bg-gray-300'
                    }`}
                  />
                </div>
              )}
            </React.Fragment>
          ))}
        </div>
      </div>

      {/* Form Content */}
      <div className="bg-white rounded-lg shadow-md p-4 sm:p-6">{children}</div>
    </div>
  )
}
