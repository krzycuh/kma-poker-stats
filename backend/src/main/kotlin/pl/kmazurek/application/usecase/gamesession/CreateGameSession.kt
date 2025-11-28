package pl.kmazurek.application.usecase.gamesession

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pl.kmazurek.application.usecase.player.PlayerNotFoundException
import pl.kmazurek.domain.model.gamesession.GameSession
import pl.kmazurek.domain.model.gamesession.GameType
import pl.kmazurek.domain.model.gamesession.Location
import pl.kmazurek.domain.model.gamesession.SessionResult
import pl.kmazurek.domain.model.player.Player
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

        // Validate minimum 2 active (non-spectator) players
        val activePlayers = command.results.filter { !it.isSpectator }
        require(activePlayers.size >= 2) { "Session must have at least 2 active (non-spectator) players" }

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
                    isSpectator = resultCommand.isSpectator,
                )
            }

        // Calculate placements based on profit (descending order)
        // Players with same profit get same placement (DENSE_RANK behavior)
        val resultsWithPlacements = calculatePlacements(results)

        val savedResults = sessionResultRepository.saveAll(resultsWithPlacements)

        return GameSessionWithResults(savedSession, savedResults)
    }

    /**
     * Calculate placements for session results based on profit
     * Uses DENSE_RANK logic: players with same profit get same placement
     * Spectators are excluded from placement calculation
     */
    private fun calculatePlacements(results: List<SessionResult>): List<SessionResult> {
        // Separate spectators from active players
        val (spectators, activePlayers) = results.partition { it.isSpectator }

        // Sort active players by profit descending, then by player ID for stability
        val sortedActivePlayers =
            activePlayers.sortedWith(
                compareByDescending<SessionResult> { it.profit().amountInCents }
                    .thenBy { it.playerId.toString() },
            )

        var currentPlacement = 1
        var previousProfit: Long? = null

        val activePlayersWithPlacements =
            sortedActivePlayers.map { result ->
                val profit = result.profit().amountInCents

                // If profit is different from previous, increment placement
                if (previousProfit != null && profit != previousProfit) {
                    currentPlacement++
                }

                previousProfit = profit

                // Copy result with placement set
                result.copy(placement = currentPlacement)
            }

        // Return active players with placements + spectators with null placement
        return activePlayersWithPlacements + spectators
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
    val isSpectator: Boolean = false,
)

data class GameSessionWithResults(
    val session: GameSession,
    val results: List<SessionResult>,
    val playersById: Map<PlayerId, Player> = emptyMap(),
)
