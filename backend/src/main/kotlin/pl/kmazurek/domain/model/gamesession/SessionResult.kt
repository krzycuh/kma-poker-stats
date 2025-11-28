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
    val placement: Int? = null,
    val notes: String? = null,
    val isSpectator: Boolean = false,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    init {
        // Buy-in and cash-out themselves must be non-negative
        // (but profit, which is cashOut - buyIn, can be negative)
        require(buyIn.amountInCents >= 0) { "Buy-in must be non-negative, got ${buyIn.amountInCents}" }
        require(cashOut.amountInCents >= 0) { "Cash-out must be non-negative, got ${cashOut.amountInCents}" }

        // Spectators must have 0 buy-in and 0 cash-out
        if (isSpectator) {
            require(buyIn.isZero() && cashOut.isZero()) {
                "Spectators must have 0 buy-in and 0 cash-out, got buyIn=${buyIn.amountInCents}, cashOut=${cashOut.amountInCents}"
            }
        }

        // Placement must be positive if provided, and spectators should not have placement
        placement?.let {
            require(it >= 1) { "Placement must be >= 1, got $it" }
            require(!isSpectator) { "Spectators should not have placement" }
        }
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
        placement: Int? = null,
        isSpectator: Boolean = false,
    ): SessionResult {
        return copy(
            buyIn = buyIn,
            cashOut = cashOut,
            notes = notes,
            placement = placement,
            isSpectator = isSpectator,
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
            isSpectator: Boolean = false,
        ): SessionResult {
            val now = LocalDateTime.now()
            return SessionResult(
                id = SessionResultId.generate(),
                sessionId = sessionId,
                playerId = playerId,
                buyIn = buyIn,
                cashOut = cashOut,
                notes = notes,
                isSpectator = isSpectator,
                createdAt = now,
                updatedAt = now,
            )
        }
    }
}
