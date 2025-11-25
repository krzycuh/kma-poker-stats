import type { ReactNode } from 'react'
import { useNavigate } from 'react-router-dom'

type PageHeaderProps = {
  title: string
  description?: string
  backTo?: string | number
  backLabel?: string
  actions?: ReactNode
  className?: string
}

export function PageHeader({
  title,
  description,
  backTo = '/',
  backLabel = 'Back to Dashboard',
  actions,
  className = '',
}: PageHeaderProps) {
  const navigate = useNavigate()

  const handleBack = () => {
    if (typeof backTo === 'number') {
      navigate(backTo)
      return
    }

    navigate(backTo)
  }

  return (
    <div className={`mb-8 ${className}`}>
      <button
        type="button"
        onClick={handleBack}
        className="text-blue-600 hover:text-blue-700 flex items-center gap-2 mb-4"
      >
        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M10 19l-7-7m0 0l7-7m-7 7h18"
          />
        </svg>
        {backLabel}
      </button>
      <h1 className="text-3xl font-bold text-gray-900">{title}</h1>
      {description && <p className="text-gray-600 mt-2">{description}</p>}
      {actions && <div className="mt-6">{actions}</div>}
    </div>
  )
}
