package pl.kmazurek.infrastructure.persistence

import org.springframework.stereotype.Component
import pl.kmazurek.domain.model.gamesession.GameSession
import pl.kmazurek.domain.model.gamesession.GameSessionId
import pl.kmazurek.domain.model.gamesession.GameType
import pl.kmazurek.domain.model.user.UserId
import pl.kmazurek.domain.repository.GameSessionRepository
import pl.kmazurek.infrastructure.persistence.entity.GameTypeJpa
import pl.kmazurek.infrastructure.persistence.mapper.GameSessionMapper
import java.time.LocalDateTime

/**
 * Implementation of GameSessionRepository (domain interface)
 * Adapter that connects domain to JPA infrastructure
 */
@Component
class GameSessionRepositoryImpl(
    private val jpaRepository: JpaGameSessionRepository,
) : GameSessionRepository {
    override fun findById(id: GameSessionId): GameSession? {
        return jpaRepository.findById(id.value)
            .map { GameSessionMapper.toDomain(it) }
            .orElse(null)
    }

    override fun findAll(includeDeleted: Boolean): List<GameSession> {
        return if (includeDeleted) {
            jpaRepository.findAll()
        } else {
            jpaRepository.findAllByIsDeleted(false)
        }.map { GameSessionMapper.toDomain(it) }
    }

    override fun findByCreatedByUserId(
        userId: UserId,
        includeDeleted: Boolean,
    ): List<GameSession> {
        return if (includeDeleted) {
            jpaRepository.findByCreatedByUserId(userId.value)
        } else {
            jpaRepository.findByCreatedByUserIdAndIsDeleted(userId.value, false)
        }.map { GameSessionMapper.toDomain(it) }
    }

    override fun findByLocation(
        location: String,
        includeDeleted: Boolean,
    ): List<GameSession> {
        return if (includeDeleted) {
            jpaRepository.findByLocation(location)
        } else {
            jpaRepository.findByLocationAndIsDeleted(location, false)
        }.map { GameSessionMapper.toDomain(it) }
    }

    override fun findByGameType(
        gameType: GameType,
        includeDeleted: Boolean,
    ): List<GameSession> {
        val jpaGameType = toJpaGameType(gameType)
        return if (includeDeleted) {
            jpaRepository.findByGameType(jpaGameType)
        } else {
            jpaRepository.findByGameTypeAndIsDeleted(jpaGameType, false)
        }.map { GameSessionMapper.toDomain(it) }
    }

    override fun findByDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        includeDeleted: Boolean,
    ): List<GameSession> {
        return if (includeDeleted) {
            jpaRepository.findByDateRangeAll(startDate, endDate)
        } else {
            jpaRepository.findByDateRange(startDate, endDate, false)
        }.map { GameSessionMapper.toDomain(it) }
    }

    override fun save(session: GameSession): GameSession {
        val jpaEntity = GameSessionMapper.toJpa(session)
        val saved = jpaRepository.save(jpaEntity)
        return GameSessionMapper.toDomain(saved)
    }

    override fun deleteById(id: GameSessionId) {
        jpaRepository.deleteById(id.value)
    }

    override fun count(): Long {
        return jpaRepository.count()
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
