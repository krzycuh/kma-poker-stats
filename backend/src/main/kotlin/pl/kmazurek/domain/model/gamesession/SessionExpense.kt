package pl.kmazurek.domain.model.gamesession

import pl.kmazurek.domain.model.player.PlayerId
import pl.kmazurek.domain.model.shared.Money
import java.time.LocalDateTime

/**
 * SessionExpense Entity
 * Represents a single expense item in a game session (e.g. pizza, beer)
 * Part of the GameSession aggregate
 */
data class SessionExpense(
    val id: SessionExpenseId,
    val sessionId: GameSessionId,
    val payerPlayerId: PlayerId,
    val description: String,
    val amount: Money,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    init {
        require(description.isNotBlank()) { "Expense description cannot be blank" }
        require(description.length <= 255) { "Expense description cannot exceed 255 characters" }
        require(amount.isPositive()) { "Expense amount must be positive, got ${amount.amountInCents}" }
    }

    fun update(
        description: String,
        amount: Money,
        payerPlayerId: PlayerId,
    ): SessionExpense {
        return copy(
            description = description,
            amount = amount,
            payerPlayerId = payerPlayerId,
            updatedAt = LocalDateTime.now(),
        )
    }

    companion object {
        fun create(
            sessionId: GameSessionId,
            payerPlayerId: PlayerId,
            description: String,
            amount: Money,
        ): SessionExpense {
            val now = LocalDateTime.now()
            return SessionExpense(
                id = SessionExpenseId.generate(),
                sessionId = sessionId,
                payerPlayerId = payerPlayerId,
                description = description,
                amount = amount,
                createdAt = now,
                updatedAt = now,
            )
        }
    }
}
