package pl.kmazurek.application.usecase.gamesession

import org.springframework.stereotype.Service
import pl.kmazurek.domain.model.gamesession.GameSession
import pl.kmazurek.domain.model.gamesession.GameSessionId
import pl.kmazurek.domain.repository.GameSessionRepository

/**
 * Use Case: Delete (soft delete) a game session
 */
@Service
class DeleteGameSession(
    private val gameSessionRepository: GameSessionRepository,
) {
    fun execute(sessionId: GameSessionId): GameSession {
        val session =
            gameSessionRepository.findById(sessionId)
                ?: throw GameSessionNotFoundException("Game session not found")

        val deletedSession = session.delete()
        return gameSessionRepository.save(deletedSession)
    }
}
