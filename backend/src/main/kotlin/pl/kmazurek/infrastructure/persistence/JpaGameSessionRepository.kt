package pl.kmazurek.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import pl.kmazurek.infrastructure.persistence.entity.GameSessionJpaEntity
import pl.kmazurek.infrastructure.persistence.entity.GameTypeJpa
import java.time.LocalDateTime
import java.util.UUID

/**
 * Spring Data JPA repository interface for GameSession
 */
@Repository
interface JpaGameSessionRepository : JpaRepository<GameSessionJpaEntity, UUID> {
    fun findAllByIsDeleted(isDeleted: Boolean): List<GameSessionJpaEntity>

    fun findByCreatedByUserIdAndIsDeleted(
        userId: UUID,
        isDeleted: Boolean,
    ): List<GameSessionJpaEntity>

    fun findByCreatedByUserId(userId: UUID): List<GameSessionJpaEntity>

    fun findByLocationAndIsDeleted(
        location: String,
        isDeleted: Boolean,
    ): List<GameSessionJpaEntity>

    fun findByLocation(location: String): List<GameSessionJpaEntity>

    fun findByGameTypeAndIsDeleted(
        gameType: GameTypeJpa,
        isDeleted: Boolean,
    ): List<GameSessionJpaEntity>

    fun findByGameType(gameType: GameTypeJpa): List<GameSessionJpaEntity>

    @Query("SELECT s FROM GameSessionJpaEntity s WHERE s.startTime BETWEEN :startDate AND :endDate AND s.isDeleted = :isDeleted")
    fun findByDateRange(
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime,
        @Param("isDeleted") isDeleted: Boolean,
    ): List<GameSessionJpaEntity>

    @Query("SELECT s FROM GameSessionJpaEntity s WHERE s.startTime BETWEEN :startDate AND :endDate")
    fun findByDateRangeAll(
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime,
    ): List<GameSessionJpaEntity>

    @Query(
        """
        SELECT DISTINCT s FROM GameSessionJpaEntity s
        JOIN SessionResultJpaEntity sr ON sr.sessionId = s.id
        WHERE sr.playerId = :playerId AND s.isDeleted = :isDeleted
        """,
    )
    fun findByParticipantPlayerIdAndIsDeleted(
        @Param("playerId") playerId: UUID,
        @Param("isDeleted") isDeleted: Boolean,
    ): List<GameSessionJpaEntity>

    @Query(
        """
        SELECT DISTINCT s FROM GameSessionJpaEntity s
        JOIN SessionResultJpaEntity sr ON sr.sessionId = s.id
        WHERE sr.playerId = :playerId
        """,
    )
    fun findByParticipantPlayerId(
        @Param("playerId") playerId: UUID,
    ): List<GameSessionJpaEntity>
}
