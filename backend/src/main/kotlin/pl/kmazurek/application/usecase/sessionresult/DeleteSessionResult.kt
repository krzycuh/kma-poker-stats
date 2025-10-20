package pl.kmazurek.application.usecase.sessionresult

import org.springframework.stereotype.Service
import pl.kmazurek.domain.model.gamesession.SessionResultId
import pl.kmazurek.domain.repository.SessionResultRepository

/**
 * Use Case: Delete a session result
 */
@Service
class DeleteSessionResult(
    private val sessionResultRepository: SessionResultRepository,
) {
    fun execute(resultId: SessionResultId) {
        val result =
            sessionResultRepository.findById(resultId)
                ?: throw SessionResultNotFoundException("Session result not found")

        sessionResultRepository.deleteById(resultId)
    }
}
