import { GameType } from './gameSession'

export interface SessionFormData {
  sessionDate: string
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
    isSpectator: boolean
  }[]
}
