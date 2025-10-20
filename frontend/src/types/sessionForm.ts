import { GameType } from './gameSession'

export interface SessionFormData {
  startTime: string
  endTime: string
  location: string
  gameType: GameType
  minBuyInCents: number
  sessionNotes: string
  selectedPlayerIds: string[]
  results: {
    playerId: string
    buyInCents: number
    cashOutCents: number
    notes: string
  }[]
}
