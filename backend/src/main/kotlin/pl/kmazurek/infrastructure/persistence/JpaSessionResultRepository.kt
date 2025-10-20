package pl.kmazurek.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import pl.kmazurek.infrastructure.persistence.entity.SessionResultJpaEntity
import java.util.UUID

/**
 * Spring Data JPA repository interface for SessionResult
 */
@Repository
interface JpaSessionResultRepository : JpaRepository<SessionResultJpaEntity, UUID> {
    fun findBySessionId(sessionId: UUID): List<SessionResultJpaEntity>

    fun findByPlayerId(playerId: UUID): List<SessionResultJpaEntity>

    fun existsBySessionIdAndPlayerId(
        sessionId: UUID,
        playerId: UUID,
    ): Boolean

    fun deleteBySessionId(sessionId: UUID)
}
