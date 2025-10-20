package pl.kmazurek.application.usecase.gamesession

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pl.kmazurek.domain.model.gamesession.GameSession
import pl.kmazurek.domain.model.gamesession.GameSessionId
import pl.kmazurek.domain.model.gamesession.GameType
import pl.kmazurek.domain.model.gamesession.Location
import pl.kmazurek.domain.model.shared.Money
import pl.kmazurek.domain.repository.GameSessionRepository
import java.time.LocalDateTime

/**
 * Use Case: Update an existing game session
 */
@Service
class UpdateGameSession(
    private val gameSessionRepository: GameSessionRepository,
) {
    @Transactional
    fun execute(
        sessionId: GameSessionId,
        command: UpdateGameSessionCommand,
    ): GameSession {
        val session =
            gameSessionRepository.findById(sessionId)
                ?: throw GameSessionNotFoundException("Game session not found")

        val updatedSession =
            session.update(
                startTime = command.startTime,
                endTime = command.endTime,
                location = Location(command.location),
                gameType = GameType.fromString(command.gameType),
                minBuyIn = Money.ofCents(command.minBuyInCents),
                notes = command.notes,
            )

        return gameSessionRepository.save(updatedSession)
    }
}

data class UpdateGameSessionCommand(
    val startTime: LocalDateTime,
    val endTime: LocalDateTime? = null,
    val location: String,
    val gameType: String,
    val minBuyInCents: Long,
    val notes: String? = null,
)

class GameSessionNotFoundException(message: String) : RuntimeException(message)
