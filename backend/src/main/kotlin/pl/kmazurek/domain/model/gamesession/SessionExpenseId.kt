package pl.kmazurek.domain.model.gamesession

import java.util.UUID

/**
 * Value Object representing a unique SessionExpense identifier
 */
@JvmInline
value class SessionExpenseId(val value: UUID) {
    companion object {
        fun generate(): SessionExpenseId = SessionExpenseId(UUID.randomUUID())

        fun fromString(value: String): SessionExpenseId = SessionExpenseId(UUID.fromString(value))
    }

    override fun toString(): String = value.toString()
}
