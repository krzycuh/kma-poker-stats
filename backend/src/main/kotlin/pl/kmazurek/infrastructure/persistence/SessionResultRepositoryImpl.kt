package pl.kmazurek.infrastructure.persistence

import org.springframework.stereotype.Component
import pl.kmazurek.domain.model.gamesession.GameSessionId
import pl.kmazurek.domain.model.gamesession.SessionResult
import pl.kmazurek.domain.model.gamesession.SessionResultId
import pl.kmazurek.domain.model.player.PlayerId
import pl.kmazurek.domain.repository.SessionResultRepository
import pl.kmazurek.infrastructure.persistence.mapper.SessionResultMapper

/**
 * Implementation of SessionResultRepository (domain interface)
 * Adapter that connects domain to JPA infrastructure
 */
@Component
class SessionResultRepositoryImpl(
    private val jpaRepository: JpaSessionResultRepository,
) : SessionResultRepository {
    override fun findById(id: SessionResultId): SessionResult? {
        return jpaRepository.findById(id.value)
            .map { SessionResultMapper.toDomain(it) }
            .orElse(null)
    }

    override fun findBySessionId(sessionId: GameSessionId): List<SessionResult> {
        return jpaRepository.findBySessionId(sessionId.value)
            .map { SessionResultMapper.toDomain(it) }
    }

    override fun findBySessionIds(sessionIds: Collection<GameSessionId>): List<SessionResult> {
        if (sessionIds.isEmpty()) {
            return emptyList()
        }
        val uuidIds = sessionIds.map { it.value }
        return jpaRepository.findBySessionIdIn(uuidIds)
            .map { SessionResultMapper.toDomain(it) }
    }

    override fun findByPlayerId(playerId: PlayerId): List<SessionResult> {
        return jpaRepository.findByPlayerId(playerId.value)
            .map { SessionResultMapper.toDomain(it) }
    }

    override fun existsBySessionIdAndPlayerId(
        sessionId: GameSessionId,
        playerId: PlayerId,
    ): Boolean {
        return jpaRepository.existsBySessionIdAndPlayerId(sessionId.value, playerId.value)
    }

    override fun save(result: SessionResult): SessionResult {
        val jpaEntity = SessionResultMapper.toJpa(result)
        val saved = jpaRepository.save(jpaEntity)
        return SessionResultMapper.toDomain(saved)
    }

    override fun saveAll(results: List<SessionResult>): List<SessionResult> {
        val jpaEntities = results.map { SessionResultMapper.toJpa(it) }
        val saved = jpaRepository.saveAll(jpaEntities)
        return saved.map { SessionResultMapper.toDomain(it) }
    }

    override fun deleteById(id: SessionResultId) {
        jpaRepository.deleteById(id.value)
    }

    override fun deleteBySessionId(sessionId: GameSessionId) {
        jpaRepository.deleteBySessionId(sessionId.value)
    }

    override fun count(): Long {
        return jpaRepository.count()
    }
}
