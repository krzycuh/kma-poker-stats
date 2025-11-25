package pl.kmazurek.infrastructure.persistence

import org.springframework.stereotype.Component
import pl.kmazurek.domain.model.player.Player
import pl.kmazurek.domain.model.player.PlayerId
import pl.kmazurek.domain.model.user.UserId
import pl.kmazurek.domain.repository.PlayerRepository
import pl.kmazurek.infrastructure.persistence.mapper.PlayerMapper

/**
 * Implementation of PlayerRepository (domain interface)
 * Adapter that connects domain to JPA infrastructure
 */
@Component
class PlayerRepositoryImpl(
    private val jpaRepository: JpaPlayerRepository,
) : PlayerRepository {
    override fun findById(id: PlayerId): Player? {
        return jpaRepository.findById(id.value)
            .map { PlayerMapper.toDomain(it) }
            .orElse(null)
    }

    override fun findByUserId(userId: UserId): Player? {
        return jpaRepository.findByUserId(userId.value)
            ?.let { PlayerMapper.toDomain(it) }
    }

    override fun findByIds(ids: Collection<PlayerId>): List<Player> {
        if (ids.isEmpty()) {
            return emptyList()
        }
        val uuidIds = ids.map { it.value }
        return jpaRepository.findAllById(uuidIds)
            .map { PlayerMapper.toDomain(it) }
    }

    override fun findAll(includeInactive: Boolean): List<Player> {
        return if (includeInactive) {
            jpaRepository.findAll()
        } else {
            jpaRepository.findAllByIsActive(true)
        }.map { PlayerMapper.toDomain(it) }
    }

    override fun findByNameContaining(
        name: String,
        includeInactive: Boolean,
    ): List<Player> {
        return if (includeInactive) {
            jpaRepository.findByNameContainingIgnoreCase(name)
        } else {
            jpaRepository.findByNameContainingIgnoreCaseAndIsActive(name, true)
        }.map { PlayerMapper.toDomain(it) }
    }

    override fun existsByName(name: String): Boolean {
        return jpaRepository.existsByName(name)
    }

    override fun save(player: Player): Player {
        val jpaEntity = PlayerMapper.toJpa(player)
        val saved = jpaRepository.save(jpaEntity)
        return PlayerMapper.toDomain(saved)
    }

    override fun deleteById(id: PlayerId) {
        jpaRepository.deleteById(id.value)
    }

    override fun count(): Long {
        return jpaRepository.count()
    }
}
