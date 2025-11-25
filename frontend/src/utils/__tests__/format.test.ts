import { describe, it, expect } from 'vitest';
import { 
  formatCents,
  formatProfitCents,
  formatPercentage,
  formatROI,
  formatDate, 
  formatDateTime,
  formatGameType,
  formatStreak
} from '../format';

describe('formatCents', () => {
  it('should format positive amounts correctly', () => {
    expect(formatCents(1000)).toBe('PLN 10.00');
    expect(formatCents(12345)).toBe('PLN 123.45');
    expect(formatCents(100)).toBe('PLN 1.00');
  });

  it('should format negative amounts correctly', () => {
    expect(formatCents(-1000)).toBe('-PLN 10.00');
    expect(formatCents(-12345)).toBe('-PLN 123.45');
  });

  it('should format zero correctly', () => {
    expect(formatCents(0)).toBe('PLN 0.00');
  });
});

describe('formatProfitCents', () => {
  it('should format positive profit with plus sign', () => {
    expect(formatProfitCents(5000)).toBe('+PLN 50.00');
    expect(formatProfitCents(10000)).toBe('+PLN 100.00');
  });

  it('should format negative profit with minus sign', () => {
    expect(formatProfitCents(-5000)).toBe('-PLN 50.00');
    expect(formatProfitCents(-20000)).toBe('-PLN 200.00');
  });

  it('should format zero with plus sign', () => {
    expect(formatProfitCents(0)).toBe('+PLN 0.00');
  });
});

describe('formatPercentage', () => {
  it('should format percentage correctly', () => {
    expect(formatPercentage(50.5)).toBe('50.5%');
    expect(formatPercentage(100)).toBe('100.0%');
    expect(formatPercentage(0)).toBe('0.0%');
  });
});

describe('formatROI', () => {
  it('should format positive ROI with plus sign', () => {
    expect(formatROI(25.5)).toBe('+25.5%');
    expect(formatROI(100)).toBe('+100.0%');
  });

  it('should format negative ROI with minus sign', () => {
    expect(formatROI(-25.5)).toBe('-25.5%');
  });

  it('should format zero ROI with plus sign', () => {
    expect(formatROI(0)).toBe('+0.0%');
  });
});

describe('formatDate', () => {
  it('should format dates correctly', () => {
    const date = '2025-10-21T15:30:00';
    const formatted = formatDate(date);
    expect(formatted).toMatch(/Oct 21, 2025/);
  });

  it('should handle different date formats', () => {
    const date = new Date('2025-12-25T00:00:00');
    const formatted = formatDate(date.toISOString());
    expect(formatted).toMatch(/Dec 25, 2025/);
  });
});

describe('formatDateTime', () => {
  it('should format datetime correctly', () => {
    const datetime = '2025-10-21T15:30:00';
    const formatted = formatDateTime(datetime);
    expect(formatted).toMatch(/Oct 21, 2025/);
  });
});

describe('formatGameType', () => {
  it('should format game types correctly', () => {
    expect(formatGameType('TEXAS_HOLDEM')).toBe("Texas Hold'em");
    expect(formatGameType('OMAHA')).toBe('Omaha');
    expect(formatGameType('OTHER')).toBe('Other');
  });

  it('should return unknown types as-is', () => {
    expect(formatGameType('UNKNOWN_TYPE')).toBe('UNKNOWN_TYPE');
  });
});

describe('formatStreak', () => {
  it('should format winning streaks', () => {
    expect(formatStreak(3)).toBe('3 Win Streak');
    expect(formatStreak(1)).toBe('1 Win Streak');
  });

  it('should format losing streaks', () => {
    expect(formatStreak(-3)).toBe('3 Loss Streak');
    expect(formatStreak(-1)).toBe('1 Loss Streak');
  });

  it('should format no streak', () => {
    expect(formatStreak(0)).toBe('No Streak');
  });
});
