import apiClient from './client';

export interface PlayerStatsDto {
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
}

export interface SystemStatsDto {
  totalSessions: number;
  activeSessions: number;
  totalPlayers: number;
  totalMoneyInPlayCents: number;
}

export interface RecentSessionDto {
  sessionId: string;
  startTime: string;
  location: string;
  gameType: string;
  playerCount: number;
  personalProfitCents: number | null;
  isWinning: boolean | null;
}

export interface LeaderboardPositionDto {
  position: number;
  totalPlayers: number;
  metric: string;
  value: string;
}

export interface CasualPlayerDashboardDto {
  personalStats: PlayerStatsDto;
  recentSessions: RecentSessionDto[];
  leaderboardPosition: LeaderboardPositionDto | null;
}

export interface AdminDashboardDto {
  personalStats: PlayerStatsDto | null;
  systemStats: SystemStatsDto;
  recentSessions: RecentSessionDto[];
}

export const dashboardApi = {
  getCasualPlayerDashboard: async (): Promise<CasualPlayerDashboardDto> => {
    const response = await apiClient.get<CasualPlayerDashboardDto>('/dashboard/player');
    return response.data;
  },

  getAdminDashboard: async (): Promise<AdminDashboardDto> => {
    const response = await apiClient.get<AdminDashboardDto>('/dashboard/admin');
    return response.data;
  },
};
