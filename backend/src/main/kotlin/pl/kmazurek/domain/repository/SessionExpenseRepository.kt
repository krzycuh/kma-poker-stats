package pl.kmazurek.domain.repository

import pl.kmazurek.domain.model.gamesession.GameSessionId
import pl.kmazurek.domain.model.gamesession.SessionExpense
import pl.kmazurek.domain.model.gamesession.SessionExpenseId

/**
 * Repository interface for SessionExpense entity
 * Defines contract for session expense data access
 * Implementation will be in infrastructure layer
 */
interface SessionExpenseRepository {
    fun findById(id: SessionExpenseId): SessionExpense?

    fun findBySessionId(sessionId: GameSessionId): List<SessionExpense>

    fun save(expense: SessionExpense): SessionExpense

    fun saveAll(expenses: List<SessionExpense>): List<SessionExpense>

    fun deleteById(id: SessionExpenseId)

    fun deleteBySessionId(sessionId: GameSessionId)
}
