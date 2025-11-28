package pl.kmazurek.application.usecase.gamesession

import org.springframework.stereotype.Service
import pl.kmazurek.domain.model.gamesession.GameSession
import pl.kmazurek.domain.model.gamesession.GameType
import pl.kmazurek.domain.model.user.UserId
import pl.kmazurek.domain.repository.GameSessionRepository
import pl.kmazurek.domain.repository.PlayerRepository
import java.time.LocalDateTime

/**
 * Use Case: List game sessions with optional filtering
 */
@Service
class ListGameSessions(
    private val gameSessionRepository: GameSessionRepository,
    private val playerRepository: PlayerRepository,
) {
    fun execute(query: ListGameSessionsQuery): List<GameSession> {
        return when {
            query.userId != null -> {
                // For casual players, find sessions they participated in
                val userId = UserId.fromString(query.userId)
                val player = playerRepository.findByUserId(userId) ?: return emptyList()

                gameSessionRepository.findByParticipantPlayerId(
                    player.id,
                    query.includeDeleted,
                )
            }
            query.location != null -> {
                gameSessionRepository.findByLocation(query.location, query.includeDeleted)
            }
            query.gameType != null -> {
                gameSessionRepository.findByGameType(
                    GameType.fromString(query.gameType),
                    query.includeDeleted,
                )
            }
            query.startDate != null && query.endDate != null -> {
                gameSessionRepository.findByDateRange(
                    query.startDate,
                    query.endDate,
                    query.includeDeleted,
                )
            }
            else -> {
                gameSessionRepository.findAll(query.includeDeleted)
            }
        }
    }
}

data class ListGameSessionsQuery(
    val userId: String? = null,
    val location: String? = null,
    val gameType: String? = null,
    val startDate: LocalDateTime? = null,
    val endDate: LocalDateTime? = null,
    val includeDeleted: Boolean = false,
)
