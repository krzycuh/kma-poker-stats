import { describe, it, expect } from 'vitest';
import { 
  formatCurrency, 
  formatDate, 
  formatDateTime, 
  formatRelativeTime, 
  calculateProfit 
} from '../format';

describe('formatCurrency', () => {
  it('should format positive amounts correctly', () => {
    expect(formatCurrency(1000)).toBe('10.00 PLN');
    expect(formatCurrency(12345)).toBe('123.45 PLN');
    expect(formatCurrency(100)).toBe('1.00 PLN');
  });

  it('should format negative amounts correctly', () => {
    expect(formatCurrency(-1000)).toBe('-10.00 PLN');
    expect(formatCurrency(-12345)).toBe('-123.45 PLN');
  });

  it('should format zero correctly', () => {
    expect(formatCurrency(0)).toBe('0.00 PLN');
  });

  it('should handle large amounts', () => {
    expect(formatCurrency(1000000)).toBe('10,000.00 PLN');
    expect(formatCurrency(123456789)).toBe('1,234,567.89 PLN');
  });
});

describe('calculateProfit', () => {
  it('should calculate positive profit correctly', () => {
    expect(calculateProfit(10000, 15000)).toBe(5000); // +50.00 PLN
    expect(calculateProfit(10000, 20000)).toBe(10000); // +100.00 PLN
  });

  it('should calculate negative profit (loss) correctly', () => {
    expect(calculateProfit(10000, 5000)).toBe(-5000); // -50.00 PLN
    expect(calculateProfit(20000, 0)).toBe(-20000); // -200.00 PLN
  });

  it('should return zero for break-even', () => {
    expect(calculateProfit(10000, 10000)).toBe(0);
  });

  it('should handle edge cases', () => {
    expect(calculateProfit(0, 10000)).toBe(10000);
    expect(calculateProfit(10000, 0)).toBe(-10000);
    expect(calculateProfit(0, 0)).toBe(0);
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
    expect(formatted).toMatch(/3:30 PM|15:30/);
  });
});

describe('formatRelativeTime', () => {
  it('should format recent times correctly', () => {
    const now = new Date();
    const oneHourAgo = new Date(now.getTime() - 60 * 60 * 1000);
    const formatted = formatRelativeTime(oneHourAgo.toISOString());
    expect(formatted).toMatch(/hour|hours/i);
  });

  it('should format past days correctly', () => {
    const now = new Date();
    const twoDaysAgo = new Date(now.getTime() - 2 * 24 * 60 * 60 * 1000);
    const formatted = formatRelativeTime(twoDaysAgo.toISOString());
    expect(formatted).toMatch(/day|days/i);
  });
});
