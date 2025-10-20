package pl.kmazurek.domain.model.gamesession

import pl.kmazurek.domain.model.shared.Money
import pl.kmazurek.domain.model.user.UserId
import java.time.LocalDateTime

/**
 * GameSession Aggregate Root
 * Represents a poker game session with multiple players
 * Domain entity - no framework dependencies
 */
data class GameSession(
    val id: GameSessionId,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime? = null,
    val location: Location,
    val gameType: GameType,
    val minBuyIn: Money,
    val notes: String? = null,
    val createdByUserId: UserId? = null,
    val isDeleted: Boolean = false,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    init {
        require(minBuyIn.amountInCents >= 0) { "Minimum buy-in must be non-negative" }
        if (endTime != null) {
            require(endTime.isAfter(startTime)) { "End time must be after start time" }
        }
    }

    /**
     * Update session information
     */
    fun update(
        startTime: LocalDateTime,
        endTime: LocalDateTime? = null,
        location: Location,
        gameType: GameType,
        minBuyIn: Money,
        notes: String? = null,
    ): GameSession {
        if (endTime != null) {
            require(endTime.isAfter(startTime)) { "End time must be after start time" }
        }

        return copy(
            startTime = startTime,
            endTime = endTime,
            location = location,
            gameType = gameType,
            minBuyIn = minBuyIn,
            notes = notes,
            updatedAt = LocalDateTime.now(),
        )
    }

    /**
     * End the session
     */
    fun end(endTime: LocalDateTime): GameSession {
        require(endTime.isAfter(startTime)) { "End time must be after start time" }
        require(this.endTime == null) { "Session has already ended" }

        return copy(
            endTime = endTime,
            updatedAt = LocalDateTime.now(),
        )
    }

    /**
     * Soft delete the session
     */
    fun delete(): GameSession {
        require(!isDeleted) { "Session is already deleted" }
        return copy(
            isDeleted = true,
            updatedAt = LocalDateTime.now(),
        )
    }

    /**
     * Restore a deleted session
     */
    fun restore(): GameSession {
        require(isDeleted) { "Session is not deleted" }
        return copy(
            isDeleted = false,
            updatedAt = LocalDateTime.now(),
        )
    }

    /**
     * Check if session is active (not ended)
     */
    fun isActive() = endTime == null && !isDeleted

    /**
     * Check if session has ended
     */
    fun hasEnded() = endTime != null

    /**
     * Calculate session duration (if ended)
     */
    fun duration(): Long? {
        return endTime?.let {
            java.time.Duration.between(startTime, it).toMinutes()
        }
    }

    companion object {
        /**
         * Factory method to create a new game session
         */
        fun create(
            startTime: LocalDateTime,
            location: Location,
            gameType: GameType,
            minBuyIn: Money,
            notes: String? = null,
            createdByUserId: UserId? = null,
            endTime: LocalDateTime? = null,
        ): GameSession {
            val now = LocalDateTime.now()
            return GameSession(
                id = GameSessionId.generate(),
                startTime = startTime,
                endTime = endTime,
                location = location,
                gameType = gameType,
                minBuyIn = minBuyIn,
                notes = notes,
                createdByUserId = createdByUserId,
                isDeleted = false,
                createdAt = now,
                updatedAt = now,
            )
        }
    }
}
