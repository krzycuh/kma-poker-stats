package pl.kmazurek.domain.model.gamesession

import pl.kmazurek.domain.model.player.PlayerId
import pl.kmazurek.domain.model.shared.Money
import java.time.LocalDateTime

/**
 * SessionResult Entity
 * Represents a single player's result in a game session
 * Part of the GameSession aggregate
 */
data class SessionResult(
    val id: SessionResultId,
    val sessionId: GameSessionId,
    val playerId: PlayerId,
    val buyIn: Money,
    val cashOut: Money,
    val notes: String? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    init {
        require(buyIn.amountInCents >= 0) { "Buy-in must be non-negative" }
        require(cashOut.amountInCents >= 0) { "Cash-out must be non-negative" }
    }

    /**
     * Calculate profit/loss for this result
     */
    fun profit(): Money = cashOut - buyIn

    /**
     * Check if this result is a winning result
     */
    fun isWinning() = profit().isPositive()

    /**
     * Check if this result is a break-even result
     */
    fun isBreakEven() = profit().isZero()

    /**
     * Check if this result is a losing result
     */
    fun isLosing() = profit().isNegative()

    /**
     * Update the result amounts
     */
    fun update(
        buyIn: Money,
        cashOut: Money,
        notes: String?,
    ): SessionResult {
        return copy(
            buyIn = buyIn,
            cashOut = cashOut,
            notes = notes,
            updatedAt = LocalDateTime.now(),
        )
    }

    companion object {
        /**
         * Factory method to create a new session result
         */
        fun create(
            sessionId: GameSessionId,
            playerId: PlayerId,
            buyIn: Money,
            cashOut: Money,
            notes: String? = null,
        ): SessionResult {
            val now = LocalDateTime.now()
            return SessionResult(
                id = SessionResultId.generate(),
                sessionId = sessionId,
                playerId = playerId,
                buyIn = buyIn,
                cashOut = cashOut,
                notes = notes,
                createdAt = now,
                updatedAt = now,
            )
        }
    }
}
