package pl.kmazurek.domain.repository

import pl.kmazurek.domain.model.player.Player
import pl.kmazurek.domain.model.player.PlayerId
import pl.kmazurek.domain.model.user.UserId

/**
 * Repository interface for Player aggregate
 * Defines contract for player data access
 * Implementation will be in infrastructure layer
 */
interface PlayerRepository {
    fun findById(id: PlayerId): Player?

    fun findByUserId(userId: UserId): Player?

    fun findAll(includeInactive: Boolean = false): List<Player>

    fun findByNameContaining(
        name: String,
        includeInactive: Boolean = false,
    ): List<Player>

    fun existsByName(name: String): Boolean

    fun save(player: Player): Player

    fun deleteById(id: PlayerId)

    fun count(): Long
}
