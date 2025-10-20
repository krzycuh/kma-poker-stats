package pl.kmazurek.domain.model.player

import pl.kmazurek.domain.model.user.UserId
import java.time.LocalDateTime

/**
 * Player Aggregate Root
 * Represents a poker player who may or may not be linked to a user account
 * Domain entity - no framework dependencies
 */
data class Player(
    val id: PlayerId,
    val name: PlayerName,
    val avatarUrl: String? = null,
    val userId: UserId? = null,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    /**
     * Update player information
     */
    fun update(
        name: PlayerName,
        avatarUrl: String?,
    ): Player {
        return copy(
            name = name,
            avatarUrl = avatarUrl,
            updatedAt = LocalDateTime.now(),
        )
    }

    /**
     * Link this player to a user account
     */
    fun linkToUser(userId: UserId): Player {
        require(this.userId == null) { "Player is already linked to a user" }
        return copy(
            userId = userId,
            updatedAt = LocalDateTime.now(),
        )
    }

    /**
     * Unlink this player from their user account
     */
    fun unlinkFromUser(): Player {
        require(this.userId != null) { "Player is not linked to a user" }
        return copy(
            userId = null,
            updatedAt = LocalDateTime.now(),
        )
    }

    /**
     * Deactivate this player (soft delete)
     */
    fun deactivate(): Player {
        require(isActive) { "Player is already deactivated" }
        return copy(
            isActive = false,
            updatedAt = LocalDateTime.now(),
        )
    }

    /**
     * Reactivate this player
     */
    fun reactivate(): Player {
        require(!isActive) { "Player is already active" }
        return copy(
            isActive = true,
            updatedAt = LocalDateTime.now(),
        )
    }

    /**
     * Check if player is linked to a user
     */
    fun isLinkedToUser() = userId != null

    companion object {
        /**
         * Factory method to create a new player
         */
        fun create(
            name: PlayerName,
            avatarUrl: String? = null,
            userId: UserId? = null,
        ): Player {
            val now = LocalDateTime.now()
            return Player(
                id = PlayerId.generate(),
                name = name,
                avatarUrl = avatarUrl,
                userId = userId,
                isActive = true,
                createdAt = now,
                updatedAt = now,
            )
        }
    }
}
