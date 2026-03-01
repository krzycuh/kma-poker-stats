package pl.kmazurek.infrastructure.persistence.mapper

import pl.kmazurek.domain.model.gamesession.GameSessionId
import pl.kmazurek.domain.model.gamesession.SessionExpense
import pl.kmazurek.domain.model.gamesession.SessionExpenseId
import pl.kmazurek.domain.model.player.PlayerId
import pl.kmazurek.domain.model.shared.Money
import pl.kmazurek.infrastructure.persistence.entity.SessionExpenseJpaEntity

/**
 * Mapper between Domain SessionExpense and JPA SessionExpenseJpaEntity
 * Anti-corruption layer - keeps domain and infrastructure separated
 */
object SessionExpenseMapper {
    fun toDomain(jpa: SessionExpenseJpaEntity): SessionExpense {
        return SessionExpense(
            id = SessionExpenseId(jpa.id),
            sessionId = GameSessionId(jpa.sessionId),
            payerPlayerId = PlayerId(jpa.payerPlayerId),
            description = jpa.description,
            amount = Money.ofCents(jpa.amountCents),
            createdAt = jpa.createdAt,
            updatedAt = jpa.updatedAt,
        )
    }

    fun toJpa(domain: SessionExpense): SessionExpenseJpaEntity {
        return SessionExpenseJpaEntity(
            id = domain.id.value,
            sessionId = domain.sessionId.value,
            payerPlayerId = domain.payerPlayerId.value,
            description = domain.description,
            amountCents = domain.amount.amountInCents,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt,
        )
    }
}
