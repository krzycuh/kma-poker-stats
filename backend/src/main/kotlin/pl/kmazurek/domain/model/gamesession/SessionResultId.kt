package pl.kmazurek.domain.model.gamesession

import java.util.UUID

/**
 * Value Object representing a unique SessionResult identifier
 */
@JvmInline
value class SessionResultId(val value: UUID) {
    companion object {
        fun generate(): SessionResultId = SessionResultId(UUID.randomUUID())

        fun fromString(value: String): SessionResultId = SessionResultId(UUID.fromString(value))
    }

    override fun toString(): String = value.toString()
}
