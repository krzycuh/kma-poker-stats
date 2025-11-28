package pl.kmazurek.infrastructure.persistence.mapper

import pl.kmazurek.domain.model.gamesession.GameSessionId
import pl.kmazurek.domain.model.gamesession.SessionResult
import pl.kmazurek.domain.model.gamesession.SessionResultId
import pl.kmazurek.domain.model.player.PlayerId
import pl.kmazurek.domain.model.shared.Money
import pl.kmazurek.infrastructure.persistence.entity.SessionResultJpaEntity

/**
 * Mapper between Domain SessionResult and JPA SessionResultJpaEntity
 * Anti-corruption layer - keeps domain and infrastructure separated
 */
object SessionResultMapper {
    fun toDomain(jpa: SessionResultJpaEntity): SessionResult {
        return SessionResult(
            id = SessionResultId(jpa.id),
            sessionId = GameSessionId(jpa.sessionId),
            playerId = PlayerId(jpa.playerId),
            buyIn = Money.ofCents(jpa.buyInCents),
            cashOut = Money.ofCents(jpa.cashOutCents),
            placement = jpa.placement,
            notes = jpa.notes,
            createdAt = jpa.createdAt,
            updatedAt = jpa.updatedAt,
        )
    }

    fun toJpa(domain: SessionResult): SessionResultJpaEntity {
        return SessionResultJpaEntity(
            id = domain.id.value,
            sessionId = domain.sessionId.value,
            playerId = domain.playerId.value,
            buyInCents = domain.buyIn.amountInCents,
            cashOutCents = domain.cashOut.amountInCents,
            placement = domain.placement,
            notes = domain.notes,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt,
        )
    }
}
