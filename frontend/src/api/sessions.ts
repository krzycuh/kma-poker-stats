import apiClient from './client'
import type {
  GameSession,
  GameSessionWithResults,
  CreateGameSessionRequest,
  CreateSessionExpenseRequest,
  SessionExpense,
  UpdateGameSessionRequest,
} from '../types/gameSession'

export const sessionApi = {
  list: async (params?: {
    location?: string
    gameType?: string
    startDate?: string
    endDate?: string
    includeDeleted?: boolean
  }): Promise<GameSession[]> => {
    const response = await apiClient.get<GameSession[]>('/sessions', { params })
    return response.data
  },

  getAll: async (): Promise<GameSession[]> => {
    const response = await apiClient.get<GameSession[]>('/sessions')
    return response.data
  },

  get: async (id: string): Promise<GameSessionWithResults> => {
    const response =
      await apiClient.get<GameSessionWithResults>(`/sessions/${id}`)
    return response.data
  },

  getById: async (id: string): Promise<GameSessionWithResults> => {
    const response =
      await apiClient.get<GameSessionWithResults>(`/sessions/${id}`)
    return response.data
  },

  create: async (
    data: CreateGameSessionRequest,
  ): Promise<GameSessionWithResults> => {
    const response = await apiClient.post<GameSessionWithResults>(
      '/sessions',
      data,
    )
    return response.data
  },

  update: async (
    id: string,
    data: UpdateGameSessionRequest,
  ): Promise<GameSession> => {
    const response = await apiClient.put<GameSession>(`/sessions/${id}`, data)
    return response.data
  },

  delete: async (id: string): Promise<void> => {
    await apiClient.delete(`/sessions/${id}`)
  },

  addExpense: async (
    sessionId: string,
    data: CreateSessionExpenseRequest,
  ): Promise<SessionExpense> => {
    const response = await apiClient.post<SessionExpense>(
      `/sessions/${sessionId}/expenses`,
      data,
    )
    return response.data
  },

  updateExpense: async (
    sessionId: string,
    expenseId: string,
    data: CreateSessionExpenseRequest,
  ): Promise<SessionExpense> => {
    const response = await apiClient.put<SessionExpense>(
      `/sessions/${sessionId}/expenses/${expenseId}`,
      data,
    )
    return response.data
  },

  deleteExpense: async (
    sessionId: string,
    expenseId: string,
  ): Promise<void> => {
    await apiClient.delete(`/sessions/${sessionId}/expenses/${expenseId}`)
  },
}
