package pl.kmazurek.application.usecase.sessionresult

import org.springframework.stereotype.Service
import pl.kmazurek.domain.model.gamesession.SessionResult
import pl.kmazurek.domain.model.gamesession.SessionResultId
import pl.kmazurek.domain.model.shared.Money
import pl.kmazurek.domain.repository.SessionResultRepository

/**
 * Use Case: Update a session result
 */
@Service
class UpdateSessionResult(
    private val sessionResultRepository: SessionResultRepository,
) {
    fun execute(
        resultId: SessionResultId,
        command: UpdateSessionResultCommand,
    ): SessionResult {
        val result =
            sessionResultRepository.findById(resultId)
                ?: throw SessionResultNotFoundException("Session result not found")

        val updatedResult =
            result.update(
                buyIn = Money.ofCents(command.buyInCents),
                cashOut = Money.ofCents(command.cashOutCents),
                notes = command.notes,
            )

        return sessionResultRepository.save(updatedResult)
    }
}

data class UpdateSessionResultCommand(
    val buyInCents: Long,
    val cashOutCents: Long,
    val notes: String? = null,
)

class SessionResultNotFoundException(message: String) : RuntimeException(message)
