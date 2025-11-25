import apiClient from './client'
import type { CompleteStats } from './stats'

export interface SharedPlayerSummary {
  playerId: string
  name: string
  avatarUrl: string | null
  sharedSessionsCount: number
  lastSharedSessionAt: string | null
}

export interface SharedPlayerStatsResponse {
  playerId: string
  playerName: string
  avatarUrl: string | null
  sharedSessionsCount: number
  stats: CompleteStats
}

export const playerNetworkApi = {
  async searchPlayers(params?: { searchTerm?: string; limit?: number }): Promise<SharedPlayerSummary[]> {
    const response = await apiClient.get<SharedPlayerSummary[]>('/player-network/players', {
      params,
    })
    return response.data
  },

  async getSharedStats(
    playerId: string,
    startDate?: string,
    endDate?: string,
  ): Promise<SharedPlayerStatsResponse> {
    const query = new URLSearchParams()
    if (startDate) query.append('startDate', startDate)
    if (endDate) query.append('endDate', endDate)
    const queryString = query.toString()

    const response = await apiClient.get<SharedPlayerStatsResponse>(
      `/stats/players/${playerId}${queryString ? `?${queryString}` : ''}`,
    )
    return response.data
  },
}
