import apiClient from './client'
import type {
  Player,
  CreatePlayerRequest,
  UpdatePlayerRequest,
} from '../types/player'

export const playerApi = {
  list: async (params?: {
    searchTerm?: string
    includeInactive?: boolean
  }): Promise<Player[]> => {
    const response = await apiClient.get<Player[]>('/players', { params })
    return response.data
  },

  get: async (id: string): Promise<Player> => {
    const response = await apiClient.get<Player>(`/players/${id}`)
    return response.data
  },

  create: async (data: CreatePlayerRequest): Promise<Player> => {
    const response = await apiClient.post<Player>('/players', data)
    return response.data
  },

  update: async (id: string, data: UpdatePlayerRequest): Promise<Player> => {
    const response = await apiClient.put<Player>(`/players/${id}`, data)
    return response.data
  },

  linkUser: async (playerId: string, userId: string): Promise<Player> => {
    const response = await apiClient.post<Player>(`/players/${playerId}/link`, {
      userId,
    })
    return response.data
  },

  unlinkUser: async (playerId: string): Promise<Player> => {
    const response = await apiClient.delete<Player>(`/players/${playerId}/link`)
    return response.data
  },

  delete: async (id: string): Promise<void> => {
    await apiClient.delete(`/players/${id}`)
  },
}
