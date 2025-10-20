// Common types for the application

export interface User {
  id: string
  email: string
  name: string
  role: 'CASUAL_PLAYER' | 'ADMIN'
  avatarUrl?: string
  createdAt: string
}

export interface Player {
  id: string
  name: string
  avatarUrl?: string
  userId?: string
  createdAt: string
}

export interface GameSession {
  id: string
  startTime: string
  location: string
  gameType: string
  minBuyIn: number
  results: SessionResult[]
  notes?: string
  createdAt: string
  updatedAt: string
}

export interface SessionResult {
  id: string
  sessionId: string
  playerId: string
  playerName: string
  buyIn: number
  cashOut: number
  profit: number
  notes?: string
}

export interface PlayerStats {
  playerId: string
  totalSessions: number
  netProfit: number
  roi: number
  winRate: number
  biggestWin: number
  biggestLoss: number
  averageProfit: number
  currentStreak: number
}

export interface LeaderboardEntry {
  rank: number
  playerId: string
  playerName: string
  avatarUrl?: string
  value: number
  change?: number
}

export interface ApiError {
  message: string
  status: number
  errors?: Record<string, string[]>
}
