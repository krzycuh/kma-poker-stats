package pl.kmazurek.application.usecase.gamesession

import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import pl.kmazurek.domain.model.gamesession.GameSessionId
import pl.kmazurek.domain.model.gamesession.SessionResult
import pl.kmazurek.domain.model.player.Player
import pl.kmazurek.domain.model.player.PlayerId
import pl.kmazurek.domain.model.user.UserId
import pl.kmazurek.domain.repository.GameSessionRepository
import pl.kmazurek.domain.repository.PlayerRepository
import pl.kmazurek.domain.repository.SessionResultRepository

/**
 * Use Case: Get a game session by ID with its results
 */
@Service
class GetGameSession(
    private val gameSessionRepository: GameSessionRepository,
    private val sessionResultRepository: SessionResultRepository,
    private val playerRepository: PlayerRepository,
) {
    fun execute(
        sessionId: GameSessionId,
        participantUserId: UserId? = null,
    ): GameSessionWithResults {
        val session =
            gameSessionRepository.findById(sessionId)
                ?: throw GameSessionNotFoundException("Game session not found")

        val results = sessionResultRepository.findBySessionId(sessionId)

        participantUserId?.let {
            enforceParticipantAccess(it, results)
        }

        val playersById = loadPlayersForResults(results)

        return GameSessionWithResults(session, results, playersById)
    }

    private fun enforceParticipantAccess(
        participantUserId: UserId,
        results: List<SessionResult>,
    ) {
        val player =
            playerRepository.findByUserId(participantUserId)
                ?: throw AccessDeniedException("User is not linked to a player profile")

        val participates = results.any { it.playerId == player.id }
        if (!participates) {
            throw AccessDeniedException("You do not have access to this session")
        }
    }

    private fun loadPlayersForResults(results: List<SessionResult>): Map<PlayerId, Player> {
        val playerIds = results.map { it.playerId }.distinct()
        if (playerIds.isEmpty()) {
            return emptyMap()
        }

        return playerIds.mapNotNull { playerId ->
            playerRepository.findById(playerId)?.let { playerId to it }
        }.toMap()
    }
}
