package pl.kmazurek.infrastructure.persistence.mapper

import pl.kmazurek.domain.model.player.Player
import pl.kmazurek.domain.model.player.PlayerId
import pl.kmazurek.domain.model.player.PlayerName
import pl.kmazurek.domain.model.user.UserId
import pl.kmazurek.infrastructure.persistence.entity.PlayerJpaEntity

/**
 * Mapper between Domain Player and JPA PlayerJpaEntity
 * Anti-corruption layer - keeps domain and infrastructure separated
 */
object PlayerMapper {
    fun toDomain(jpa: PlayerJpaEntity): Player {
        return Player(
            id = PlayerId(jpa.id),
            name = PlayerName(jpa.name),
            avatarUrl = jpa.avatarUrl,
            userId = jpa.userId?.let { UserId(it) },
            isActive = jpa.isActive,
            createdAt = jpa.createdAt,
            updatedAt = jpa.updatedAt,
        )
    }

    fun toJpa(domain: Player): PlayerJpaEntity {
        return PlayerJpaEntity(
            id = domain.id.value,
            name = domain.name.value,
            avatarUrl = domain.avatarUrl,
            userId = domain.userId?.value,
            isActive = domain.isActive,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt,
        )
    }
}
