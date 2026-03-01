export interface GameSession {
  id: string
  startTime: string
  endTime: string | null
  location: string
  gameType: string
  minBuyInCents: number
  notes: string | null
  createdByUserId: string | null
  isDeleted: boolean
  createdAt: string
  updatedAt: string
}

export interface SessionResult {
  id: string
  sessionId: string
  playerId: string
  buyInCents: number
  cashOutCents: number
  profitCents: number
  placement?: number | null
  notes: string | null
  isSpectator: boolean
  createdAt: string
  updatedAt: string
  playerName?: string | null
  playerAvatarUrl?: string | null
  linkedUserId?: string | null
}

export interface SessionExpense {
  id: string
  sessionId: string
  payerPlayerId: string
  payerPlayerName?: string | null
  description: string
  amountCents: number
  createdAt: string
  updatedAt: string
}

export interface PlayerBalance {
  playerId: string
  playerName?: string | null
  paidCents: number
  owedCents: number
  balanceCents: number
}

export interface ExpenseSplit {
  totalExpensesCents: number
  perPlayerShareCents: number
  playerCount: number
  playerBalances: PlayerBalance[]
}

export interface GameSessionWithResults {
  session: GameSession
  results: SessionResult[]
  expenses?: SessionExpense[]
  expenseSplit?: ExpenseSplit | null
}

export interface CreateSessionExpenseRequest {
  payerPlayerId: string
  description: string
  amountCents: number
}

export interface CreateGameSessionRequest {
  startTime: string
  endTime?: string | null
  location: string
  gameType: string
  minBuyInCents: number
  notes?: string | null
  results: CreateSessionResultRequest[]
  expenses?: CreateSessionExpenseRequest[]
}

export interface CreateSessionResultRequest {
  playerId: string
  buyInCents: number
  cashOutCents: number
  notes?: string | null
  isSpectator?: boolean
}

export interface UpdateGameSessionRequest {
  startTime: string
  endTime?: string | null
  location: string
  gameType: string
  minBuyInCents: number
  notes?: string | null
}

export interface UpdateSessionResultRequest {
  buyInCents: number
  cashOutCents: number
  notes?: string | null
  isSpectator?: boolean
}

export enum GameType {
  TEXAS_HOLDEM = 'TEXAS_HOLDEM',
  OMAHA = 'OMAHA',
  OMAHA_HI_LO = 'OMAHA_HI_LO',
  SEVEN_CARD_STUD = 'SEVEN_CARD_STUD',
  FIVE_CARD_DRAW = 'FIVE_CARD_DRAW',
  MIXED_GAMES = 'MIXED_GAMES',
  OTHER = 'OTHER',
}
