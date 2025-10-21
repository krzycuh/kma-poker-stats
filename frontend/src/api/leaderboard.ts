import { apiClient } from './client';

/**
 * Leaderboard API client (Phase 5)
 */

export enum LeaderboardMetric {
  NET_PROFIT = 'NET_PROFIT',
  ROI = 'ROI',
  WIN_RATE = 'WIN_RATE',
  CURRENT_STREAK = 'CURRENT_STREAK',
  TOTAL_SESSIONS = 'TOTAL_SESSIONS',
  AVERAGE_PROFIT = 'AVERAGE_PROFIT',
}

export interface LeaderboardEntry {
  rank: number;
  playerId: string;
  playerName: string;
  value: number;
  valueFormatted: string;
  sessionsPlayed: number;
  isCurrentUser: boolean;
}

export interface Leaderboard {
  metric: LeaderboardMetric;
  entries: LeaderboardEntry[];
  currentUserEntry: LeaderboardEntry | null;
  totalEntries: number;
}

export const leaderboardApi = {
  /**
   * Get leaderboard for a specific metric
   */
  async getLeaderboard(
    metric: LeaderboardMetric = LeaderboardMetric.NET_PROFIT,
    limit: number = 50
  ): Promise<Leaderboard> {
    const response = await apiClient.get('/api/leaderboards', {
      params: { metric, limit },
    });
    return response.data;
  },
};
