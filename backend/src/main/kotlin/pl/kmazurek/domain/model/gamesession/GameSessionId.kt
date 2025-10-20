package pl.kmazurek.domain.model.gamesession

import java.util.UUID

/**
 * Value Object representing a unique GameSession identifier
 */
@JvmInline
value class GameSessionId(val value: UUID) {
    companion object {
        fun generate(): GameSessionId = GameSessionId(UUID.randomUUID())

        fun fromString(value: String): GameSessionId = GameSessionId(UUID.fromString(value))
    }

    override fun toString(): String = value.toString()
}
