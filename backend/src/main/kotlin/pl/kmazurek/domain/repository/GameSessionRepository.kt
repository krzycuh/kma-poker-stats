package pl.kmazurek.domain.repository

import pl.kmazurek.domain.model.gamesession.GameSession
import pl.kmazurek.domain.model.gamesession.GameSessionId
import pl.kmazurek.domain.model.gamesession.GameType
import pl.kmazurek.domain.model.player.PlayerId
import pl.kmazurek.domain.model.user.UserId
import java.time.LocalDateTime

/**
 * Repository interface for GameSession aggregate
 * Defines contract for game session data access
 * Implementation will be in infrastructure layer
 */
interface GameSessionRepository {
    fun findById(id: GameSessionId): GameSession?

    fun findAll(includeDeleted: Boolean = false): List<GameSession>

    fun findByCreatedByUserId(
        userId: UserId,
        includeDeleted: Boolean = false,
    ): List<GameSession>

    fun findByLocation(
        location: String,
        includeDeleted: Boolean = false,
    ): List<GameSession>

    fun findByGameType(
        gameType: GameType,
        includeDeleted: Boolean = false,
    ): List<GameSession>

    fun findByDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        includeDeleted: Boolean = false,
    ): List<GameSession>

    fun findByParticipantPlayerId(
        playerId: PlayerId,
        includeDeleted: Boolean = false,
    ): List<GameSession>

    fun save(session: GameSession): GameSession

    fun deleteById(id: GameSessionId)

    fun count(): Long
}
