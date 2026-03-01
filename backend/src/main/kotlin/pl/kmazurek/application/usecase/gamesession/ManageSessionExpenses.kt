package pl.kmazurek.application.usecase.gamesession

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pl.kmazurek.domain.model.gamesession.GameSessionId
import pl.kmazurek.domain.model.gamesession.SessionExpense
import pl.kmazurek.domain.model.gamesession.SessionExpenseId
import pl.kmazurek.domain.model.player.PlayerId
import pl.kmazurek.domain.model.shared.Money
import pl.kmazurek.domain.repository.GameSessionRepository
import pl.kmazurek.domain.repository.SessionExpenseRepository

/**
 * Use Case: Manage expenses for an existing game session
 */
@Service
class ManageSessionExpenses(
    private val sessionExpenseRepository: SessionExpenseRepository,
    private val gameSessionRepository: GameSessionRepository,
) {
    @Transactional
    fun addExpense(
        sessionId: GameSessionId,
        command: AddExpenseCommand,
    ): SessionExpense {
        gameSessionRepository.findById(sessionId)
            ?: throw GameSessionNotFoundException("Game session not found")

        val expense = SessionExpense.create(
            sessionId = sessionId,
            payerPlayerId = PlayerId.fromString(command.payerPlayerId),
            description = command.description,
            amount = Money.ofCents(command.amountCents),
        )

        return sessionExpenseRepository.save(expense)
    }

    @Transactional
    fun updateExpense(
        expenseId: SessionExpenseId,
        command: UpdateExpenseCommand,
    ): SessionExpense {
        val expense = sessionExpenseRepository.findById(expenseId)
            ?: throw SessionExpenseNotFoundException("Session expense not found")

        val updated = expense.update(
            description = command.description,
            amount = Money.ofCents(command.amountCents),
            payerPlayerId = PlayerId.fromString(command.payerPlayerId),
        )

        return sessionExpenseRepository.save(updated)
    }

    @Transactional
    fun deleteExpense(expenseId: SessionExpenseId) {
        sessionExpenseRepository.findById(expenseId)
            ?: throw SessionExpenseNotFoundException("Session expense not found")

        sessionExpenseRepository.deleteById(expenseId)
    }
}

data class AddExpenseCommand(
    val payerPlayerId: String,
    val description: String,
    val amountCents: Long,
)

data class UpdateExpenseCommand(
    val payerPlayerId: String,
    val description: String,
    val amountCents: Long,
)

class SessionExpenseNotFoundException(message: String) : RuntimeException(message)
