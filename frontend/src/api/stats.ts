import { apiClient } from './client';

/**
 * Stats API client (Phase 5)
 */

export interface ProfitDataPoint {
  date: string;
  profitCents: number;
  cumulativeProfitCents: number;
}

export interface LocationPerformance {
  location: string;
  sessionsPlayed: number;
  totalProfitCents: number;
  avgProfitCents: number;
  winRate: number;
}

export interface DayOfWeekPerformance {
  dayOfWeek: number;
  dayName: string;
  sessionsPlayed: number;
  totalProfitCents: number;
  avgProfitCents: number;
}

export interface NotableSession {
  sessionId: string;
  date: string;
  location: string;
  gameType: string;
  profitCents: number;
}

export interface PlayerStatsOverview {
  playerId: string;
  totalSessions: number;
  totalBuyInCents: number;
  totalCashOutCents: number;
  netProfitCents: number;
  roi: number;
  winRate: number;
  winningSessionsCount: number;
  losingSessionsCount: number;
  biggestWinCents: number;
  biggestLossCents: number;
  averageSessionProfitCents: number;
  currentStreak: number;
  firstPlaceCount: number;
  secondPlaceCount: number;
  thirdPlaceCount: number;
}

export interface CompleteStats {
  overview: PlayerStatsOverview;
  profitOverTime: ProfitDataPoint[];
  locationPerformance: LocationPerformance[];
  dayOfWeekPerformance: DayOfWeekPerformance[];
  bestSessions: NotableSession[];
  worstSessions: NotableSession[];
}

export interface SharedPlayerSummary {
  playerId: string;
  name: string;
  avatarUrl: string | null;
  sharedSessionsCount: number;
  lastSharedSessionAt: string | null;
}

export interface SharedPlayerStatsResponse {
  playerId: string;
  playerName: string;
  avatarUrl: string | null;
  sharedSessionsCount: number;
  stats: CompleteStats;
}

export const statsApi = {
  /**
   * Get complete statistics with optional date range filter
   */
  async getPersonalStats(
    startDate?: string,
    endDate?: string
  ): Promise<CompleteStats> {
    const params = new URLSearchParams();
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);
    const queryString = params.toString();
    const url = `/stats/personal${queryString ? `?${queryString}` : ''}`;

    const response = await apiClient.get(url);
    return response.data;
  },

  /**
   * Search for players in the network (shared sessions)
   */
  async searchPlayers(params?: { searchTerm?: string; limit?: number }): Promise<SharedPlayerSummary[]> {
    const response = await apiClient.get<SharedPlayerSummary[]>('/player-network/players', {
      params,
    });
    return response.data;
  },

  /**
   * Get stats for another player (filtered to shared sessions)
   */
  async getPlayerStats(
    playerId: string,
    startDate?: string,
    endDate?: string,
  ): Promise<SharedPlayerStatsResponse> {
    const query = new URLSearchParams();
    if (startDate) query.append('startDate', startDate);
    if (endDate) query.append('endDate', endDate);
    const queryString = query.toString();

    const response = await apiClient.get<SharedPlayerStatsResponse>(
      `/stats/players/${playerId}${queryString ? `?${queryString}` : ''}`,
    );
    return response.data;
  },
};
