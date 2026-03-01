package pl.kmazurek.infrastructure.persistence

import org.springframework.stereotype.Component
import pl.kmazurek.domain.model.gamesession.GameSessionId
import pl.kmazurek.domain.model.gamesession.SessionExpense
import pl.kmazurek.domain.model.gamesession.SessionExpenseId
import pl.kmazurek.domain.repository.SessionExpenseRepository
import pl.kmazurek.infrastructure.persistence.mapper.SessionExpenseMapper

/**
 * Implementation of SessionExpenseRepository (domain interface)
 * Adapter that connects domain to JPA infrastructure
 */
@Component
class SessionExpenseRepositoryImpl(
    private val jpaRepository: JpaSessionExpenseRepository,
) : SessionExpenseRepository {
    override fun findById(id: SessionExpenseId): SessionExpense? {
        return jpaRepository.findById(id.value)
            .map { SessionExpenseMapper.toDomain(it) }
            .orElse(null)
    }

    override fun findBySessionId(sessionId: GameSessionId): List<SessionExpense> {
        return jpaRepository.findBySessionId(sessionId.value)
            .map { SessionExpenseMapper.toDomain(it) }
    }

    override fun save(expense: SessionExpense): SessionExpense {
        val jpaEntity = SessionExpenseMapper.toJpa(expense)
        val saved = jpaRepository.save(jpaEntity)
        return SessionExpenseMapper.toDomain(saved)
    }

    override fun saveAll(expenses: List<SessionExpense>): List<SessionExpense> {
        val jpaEntities = expenses.map { SessionExpenseMapper.toJpa(it) }
        val saved = jpaRepository.saveAll(jpaEntities)
        return saved.map { SessionExpenseMapper.toDomain(it) }
    }

    override fun deleteById(id: SessionExpenseId) {
        jpaRepository.deleteById(id.value)
    }

    override fun deleteBySessionId(sessionId: GameSessionId) {
        jpaRepository.deleteBySessionId(sessionId.value)
    }
}
