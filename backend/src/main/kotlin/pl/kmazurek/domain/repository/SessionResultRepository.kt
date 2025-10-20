package pl.kmazurek.domain.repository

import pl.kmazurek.domain.model.gamesession.GameSessionId
import pl.kmazurek.domain.model.gamesession.SessionResult
import pl.kmazurek.domain.model.gamesession.SessionResultId
import pl.kmazurek.domain.model.player.PlayerId

/**
 * Repository interface for SessionResult entity
 * Defines contract for session result data access
 * Implementation will be in infrastructure layer
 */
interface SessionResultRepository {
    fun findById(id: SessionResultId): SessionResult?

    fun findBySessionId(sessionId: GameSessionId): List<SessionResult>

    fun findByPlayerId(playerId: PlayerId): List<SessionResult>

    fun existsBySessionIdAndPlayerId(
        sessionId: GameSessionId,
        playerId: PlayerId,
    ): Boolean

    fun save(result: SessionResult): SessionResult

    fun saveAll(results: List<SessionResult>): List<SessionResult>

    fun deleteById(id: SessionResultId)

    fun deleteBySessionId(sessionId: GameSessionId)

    fun count(): Long
}
