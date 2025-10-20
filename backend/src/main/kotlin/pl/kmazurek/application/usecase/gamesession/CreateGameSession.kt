package pl.kmazurek.application.usecase.gamesession

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pl.kmazurek.domain.model.gamesession.GameSession
import pl.kmazurek.domain.model.gamesession.GameType
import pl.kmazurek.domain.model.gamesession.Location
import pl.kmazurek.domain.model.gamesession.SessionResult
import pl.kmazurek.domain.model.player.PlayerId
import pl.kmazurek.domain.model.shared.Money
import pl.kmazurek.domain.model.user.UserId
import pl.kmazurek.domain.repository.GameSessionRepository
import pl.kmazurek.domain.repository.PlayerRepository
import pl.kmazurek.domain.repository.SessionResultRepository
import java.time.LocalDateTime

/**
 * Use Case: Create a new game session with results
 */
@Service
class CreateGameSession(
    private val gameSessionRepository: GameSessionRepository,
    private val sessionResultRepository: SessionResultRepository,
    private val playerRepository: PlayerRepository,
) {
    @Transactional
    fun execute(command: CreateGameSessionCommand): GameSessionWithResults {
        // Validate that all players exist
        command.results.forEach { resultCommand ->
            val playerId = PlayerId.fromString(resultCommand.playerId)
            playerRepository.findById(playerId)
                ?: throw PlayerNotFoundException("Player not found: ${resultCommand.playerId}")
        }

        // Validate minimum 2 players
        require(command.results.size >= 2) { "Session must have at least 2 players" }

        // Create session
        val session =
            GameSession.create(
                startTime = command.startTime,
                endTime = command.endTime,
                location = Location(command.location),
                gameType = GameType.fromString(command.gameType),
                minBuyIn = Money.ofCents(command.minBuyInCents),
                notes = command.notes,
                createdByUserId = command.createdByUserId?.let { UserId.fromString(it) },
            )

        val savedSession = gameSessionRepository.save(session)

        // Create results
        val results =
            command.results.map { resultCommand ->
                SessionResult.create(
                    sessionId = savedSession.id,
                    playerId = PlayerId.fromString(resultCommand.playerId),
                    buyIn = Money.ofCents(resultCommand.buyInCents),
                    cashOut = Money.ofCents(resultCommand.cashOutCents),
                    notes = resultCommand.notes,
                )
            }

        val savedResults = sessionResultRepository.saveAll(results)

        return GameSessionWithResults(savedSession, savedResults)
    }
}

data class CreateGameSessionCommand(
    val startTime: LocalDateTime,
    val endTime: LocalDateTime? = null,
    val location: String,
    val gameType: String,
    val minBuyInCents: Long,
    val notes: String? = null,
    val createdByUserId: String? = null,
    val results: List<CreateSessionResultCommand>,
)

data class CreateSessionResultCommand(
    val playerId: String,
    val buyInCents: Long,
    val cashOutCents: Long,
    val notes: String? = null,
)

data class GameSessionWithResults(
    val session: GameSession,
    val results: List<SessionResult>,
)

class PlayerNotFoundException(message: String) : RuntimeException(message)
