package pl.kmazurek.application.usecase.sessionresult

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
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
    @Transactional
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

        // Get all results for this session to recalculate placements
        val allSessionResults = sessionResultRepository.findBySessionId(result.sessionId)

        // Replace the old result with the updated one
        val resultsToRank =
            allSessionResults.map { r ->
                if (r.id == updatedResult.id) updatedResult else r
            }

        // Recalculate placements for all results in the session
        val resultsWithPlacements = calculatePlacements(resultsToRank)

        // Save all results with updated placements
        sessionResultRepository.saveAll(resultsWithPlacements)

        // Return the specific result that was updated
        return resultsWithPlacements.first { it.id == resultId }
    }

    /**
     * Calculate placements for session results based on profit
     * Uses DENSE_RANK logic: players with same profit get same placement
     */
    private fun calculatePlacements(results: List<SessionResult>): List<SessionResult> {
        // Sort by profit descending, then by player ID for stability
        val sortedResults =
            results.sortedWith(
                compareByDescending<SessionResult> { it.profit().amountInCents }
                    .thenBy { it.playerId.toString() },
            )

        var currentPlacement = 1
        var previousProfit: Long? = null

        return sortedResults.map { result ->
            val profit = result.profit().amountInCents

            // If profit is different from previous, increment placement
            if (previousProfit != null && profit != previousProfit) {
                currentPlacement++
            }

            previousProfit = profit

            // Copy result with placement set
            result.copy(placement = currentPlacement)
        }
    }
}

data class UpdateSessionResultCommand(
    val buyInCents: Long,
    val cashOutCents: Long,
    val notes: String? = null,
)

class SessionResultNotFoundException(message: String) : RuntimeException(message)
