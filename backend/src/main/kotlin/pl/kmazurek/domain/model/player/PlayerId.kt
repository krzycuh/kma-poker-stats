package pl.kmazurek.domain.model.player

import java.util.UUID

/**
 * Value Object representing a unique Player identifier
 */
@JvmInline
value class PlayerId(val value: UUID) {
    companion object {
        fun generate(): PlayerId = PlayerId(UUID.randomUUID())

        fun fromString(value: String): PlayerId = PlayerId(UUID.fromString(value))
    }

    override fun toString(): String = value.toString()
}
