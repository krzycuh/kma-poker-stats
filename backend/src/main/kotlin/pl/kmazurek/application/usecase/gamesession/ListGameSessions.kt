package pl.kmazurek.application.usecase.gamesession

import org.springframework.stereotype.Service
import pl.kmazurek.domain.model.gamesession.GameSession
import pl.kmazurek.domain.model.gamesession.GameType
import pl.kmazurek.domain.model.user.UserId
import pl.kmazurek.domain.repository.GameSessionRepository
import java.time.LocalDateTime

/**
 * Use Case: List game sessions with optional filtering
 */
@Service
class ListGameSessions(
    private val gameSessionRepository: GameSessionRepository,
) {
    fun execute(query: ListGameSessionsQuery): List<GameSession> {
        return when {
            query.userId != null -> {
                gameSessionRepository.findByCreatedByUserId(
                    UserId.fromString(query.userId),
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
