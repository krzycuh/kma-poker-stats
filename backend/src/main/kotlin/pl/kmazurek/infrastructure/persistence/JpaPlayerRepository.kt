package pl.kmazurek.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import pl.kmazurek.infrastructure.persistence.entity.PlayerJpaEntity
import java.util.UUID

/**
 * Spring Data JPA repository interface for Player
 */
@Repository
interface JpaPlayerRepository : JpaRepository<PlayerJpaEntity, UUID> {
    fun findByUserId(userId: UUID): PlayerJpaEntity?

    fun findAllByIsActive(isActive: Boolean): List<PlayerJpaEntity>

    fun findByNameContainingIgnoreCase(name: String): List<PlayerJpaEntity>

    fun findByNameContainingIgnoreCaseAndIsActive(
        name: String,
        isActive: Boolean,
    ): List<PlayerJpaEntity>

    fun existsByName(name: String): Boolean
}
