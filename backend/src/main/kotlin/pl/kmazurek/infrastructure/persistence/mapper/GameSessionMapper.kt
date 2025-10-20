package pl.kmazurek.infrastructure.persistence.mapper

import pl.kmazurek.domain.model.gamesession.GameSession
import pl.kmazurek.domain.model.gamesession.GameSessionId
import pl.kmazurek.domain.model.gamesession.GameType
import pl.kmazurek.domain.model.gamesession.Location
import pl.kmazurek.domain.model.shared.Money
import pl.kmazurek.domain.model.user.UserId
import pl.kmazurek.infrastructure.persistence.entity.GameSessionJpaEntity
import pl.kmazurek.infrastructure.persistence.entity.GameTypeJpa

/**
 * Mapper between Domain GameSession and JPA GameSessionJpaEntity
 * Anti-corruption layer - keeps domain and infrastructure separated
 */
object GameSessionMapper {
    fun toDomain(jpa: GameSessionJpaEntity): GameSession {
        return GameSession(
            id = GameSessionId(jpa.id),
            startTime = jpa.startTime,
            endTime = jpa.endTime,
            location = Location(jpa.location),
            gameType = toDomainGameType(jpa.gameType),
            minBuyIn = Money.ofCents(jpa.minBuyInCents),
            notes = jpa.notes,
            createdByUserId = jpa.createdByUserId?.let { UserId(it) },
            isDeleted = jpa.isDeleted,
            createdAt = jpa.createdAt,
            updatedAt = jpa.updatedAt,
        )
    }

    fun toJpa(domain: GameSession): GameSessionJpaEntity {
        return GameSessionJpaEntity(
            id = domain.id.value,
            startTime = domain.startTime,
            endTime = domain.endTime,
            location = domain.location.value,
            gameType = toJpaGameType(domain.gameType),
            minBuyInCents = domain.minBuyIn.amountInCents,
            notes = domain.notes,
            createdByUserId = domain.createdByUserId?.value,
            isDeleted = domain.isDeleted,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt,
        )
    }

    private fun toDomainGameType(jpaType: GameTypeJpa): GameType {
        return when (jpaType) {
            GameTypeJpa.TEXAS_HOLDEM -> GameType.TEXAS_HOLDEM
            GameTypeJpa.OMAHA -> GameType.OMAHA
            GameTypeJpa.OMAHA_HI_LO -> GameType.OMAHA_HI_LO
            GameTypeJpa.SEVEN_CARD_STUD -> GameType.SEVEN_CARD_STUD
            GameTypeJpa.FIVE_CARD_DRAW -> GameType.FIVE_CARD_DRAW
            GameTypeJpa.MIXED_GAMES -> GameType.MIXED_GAMES
            GameTypeJpa.OTHER -> GameType.OTHER
        }
    }

    private fun toJpaGameType(domainType: GameType): GameTypeJpa {
        return when (domainType) {
            GameType.TEXAS_HOLDEM -> GameTypeJpa.TEXAS_HOLDEM
            GameType.OMAHA -> GameTypeJpa.OMAHA
            GameType.OMAHA_HI_LO -> GameTypeJpa.OMAHA_HI_LO
            GameType.SEVEN_CARD_STUD -> GameTypeJpa.SEVEN_CARD_STUD
            GameType.FIVE_CARD_DRAW -> GameTypeJpa.FIVE_CARD_DRAW
            GameType.MIXED_GAMES -> GameTypeJpa.MIXED_GAMES
            GameType.OTHER -> GameTypeJpa.OTHER
        }
    }
}
