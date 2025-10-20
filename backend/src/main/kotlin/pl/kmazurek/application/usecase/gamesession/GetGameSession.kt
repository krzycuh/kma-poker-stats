package pl.kmazurek.application.usecase.gamesession

import org.springframework.stereotype.Service
import pl.kmazurek.domain.model.gamesession.GameSessionId
import pl.kmazurek.domain.repository.GameSessionRepository
import pl.kmazurek.domain.repository.SessionResultRepository

/**
 * Use Case: Get a game session by ID with its results
 */
@Service
class GetGameSession(
    private val gameSessionRepository: GameSessionRepository,
    private val sessionResultRepository: SessionResultRepository,
) {
    fun execute(sessionId: GameSessionId): GameSessionWithResults {
        val session =
            gameSessionRepository.findById(sessionId)
                ?: throw GameSessionNotFoundException("Game session not found")

        val results = sessionResultRepository.findBySessionId(sessionId)

        return GameSessionWithResults(session, results)
    }
}
