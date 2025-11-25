/**
 * Utility functions for formatting data
 */

/**
 * Format cents to PLN string
 */
const PLN_NUMBER_FORMAT = new Intl.NumberFormat('en-US', {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
});

export function formatCents(cents: number): string {
  const sign = cents < 0 ? '-' : '';
  const amount = PLN_NUMBER_FORMAT.format(Math.abs(cents) / 100);
  return `${sign}PLN ${amount}`;
}

/**
 * Format cents to PLN string with +/- sign
 */
export function formatProfitCents(cents: number): string {
  const sign = cents >= 0 ? '+' : '-';
  const amount = PLN_NUMBER_FORMAT.format(Math.abs(cents) / 100);
  return `${sign}PLN ${amount}`;
}

/**
 * Format percentage
 */
export function formatPercentage(value: number): string {
  return `${value.toFixed(1)}%`;
}

/**
 * Format ROI
 */
export function formatROI(roi: number): string {
  const sign = roi >= 0 ? '+' : '';
  return `${sign}${roi.toFixed(1)}%`;
}

/**
 * Format date to readable string
 */
export function formatDate(dateString: string): string {
  const date = new Date(dateString);
  return date.toLocaleDateString('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  });
}

/**
 * Format date and time to readable string
 */
export function formatDateTime(dateString: string): string {
  return formatDate(dateString);
}

/**
 * Format game type to display name
 */
export function formatGameType(gameType: string): string {
  const gameTypeMap: Record<string, string> = {
    TEXAS_HOLDEM: "Texas Hold'em",
    OMAHA: 'Omaha',
    OMAHA_HI_LO: 'Omaha Hi-Lo',
    SEVEN_CARD_STUD: '7 Card Stud',
    FIVE_CARD_DRAW: '5 Card Draw',
    MIXED_GAMES: 'Mixed Games',
    OTHER: 'Other',
  };
  return gameTypeMap[gameType] || gameType;
}

/**
 * Format streak to readable string
 */
export function formatStreak(streak: number): string {
  if (streak === 0) return 'No Streak';
  const absStreak = Math.abs(streak);
  const type = streak > 0 ? 'Win' : 'Loss';
  return `${absStreak} ${type} Streak`;
}
