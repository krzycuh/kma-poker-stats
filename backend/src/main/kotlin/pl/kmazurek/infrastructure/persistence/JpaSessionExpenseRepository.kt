package pl.kmazurek.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import pl.kmazurek.infrastructure.persistence.entity.SessionExpenseJpaEntity
import java.util.UUID

/**
 * Spring Data JPA repository interface for SessionExpense
 */
@Repository
interface JpaSessionExpenseRepository : JpaRepository<SessionExpenseJpaEntity, UUID> {
    fun findBySessionId(sessionId: UUID): List<SessionExpenseJpaEntity>

    fun deleteBySessionId(sessionId: UUID)
}
