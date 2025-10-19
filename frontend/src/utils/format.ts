import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'

dayjs.extend(relativeTime)

/**
 * Format currency amount
 */
export const formatCurrency = (amount: number, currency = 'PLN'): string => {
  return new Intl.NumberFormat('pl-PL', {
    style: 'currency',
    currency,
  }).format(amount / 100) // Convert from cents
}

/**
 * Format date
 */
export const formatDate = (date: string | Date): string => {
  return dayjs(date).format('DD MMM YYYY')
}

/**
 * Format date and time
 */
export const formatDateTime = (date: string | Date): string => {
  return dayjs(date).format('DD MMM YYYY, HH:mm')
}

/**
 * Format relative time (e.g., "2 hours ago")
 */
export const formatRelativeTime = (date: string | Date): string => {
  return dayjs(date).fromNow()
}

/**
 * Format percentage
 */
export const formatPercentage = (value: number, decimals = 1): string => {
  return `${value.toFixed(decimals)}%`
}

/**
 * Format profit/loss with color class
 */
export const getProfitClass = (profit: number): string => {
  if (profit > 0) return 'profit-text'
  if (profit < 0) return 'loss-text'
  return 'breakeven-text'
}

/**
 * Format profit with sign
 */
export const formatProfit = (profit: number, currency = 'PLN'): string => {
  const formatted = formatCurrency(Math.abs(profit), currency)
  return profit >= 0 ? `+${formatted}` : `-${formatted}`
}
