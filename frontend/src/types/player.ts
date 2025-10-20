export interface Player {
  id: string
  name: string
  avatarUrl: string | null
  userId: string | null
  isActive: boolean
  createdAt: string
  updatedAt: string
}

export interface CreatePlayerRequest {
  name: string
  avatarUrl?: string | null
  userId?: string | null
}

export interface UpdatePlayerRequest {
  name: string
  avatarUrl?: string | null
}
