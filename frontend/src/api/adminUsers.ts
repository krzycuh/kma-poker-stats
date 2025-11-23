import apiClient from './client'
import type { UserRole } from '../types/auth'

export interface UserSummary {
  id: string
  email: string
  name: string
  role: UserRole
  avatarUrl?: string | null
  createdAt: string
}

export interface PagedResponse<T> {
  items: T[]
  page: number
  pageSize: number
  totalItems: number
  totalPages: number
}

export const adminUsersApi = {
  listUnlinked: async (params: {
    searchTerm?: string
    page?: number
    pageSize?: number
  }): Promise<PagedResponse<UserSummary>> => {
    const response = await apiClient.get<PagedResponse<UserSummary>>('/admin/users/unlinked', {
      params,
    })
    return response.data
  },

  getUnlinkedCount: async (): Promise<number> => {
    const response = await apiClient.get<{ count: number }>('/admin/users/unlinked/count')
    return response.data.count
  },
}
