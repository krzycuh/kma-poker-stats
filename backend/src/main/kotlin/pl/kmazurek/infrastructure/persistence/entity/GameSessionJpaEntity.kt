package pl.kmazurek.infrastructure.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

/**
 * JPA Entity for GameSession table
 * This is in the infrastructure layer - separate from domain model
 */
@Entity
@Table(name = "game_sessions")
class GameSessionJpaEntity(
    @Id
    @Column(columnDefinition = "UUID")
    var id: UUID = UUID.randomUUID(),
    @Column(name = "start_time", nullable = false)
    var startTime: LocalDateTime,
    @Column(name = "end_time")
    var endTime: LocalDateTime? = null,
    @Column(nullable = false, length = 255)
    var location: String,
    @Column(name = "game_type", nullable = false, length = 100)
    @Enumerated(EnumType.STRING)
    var gameType: GameTypeJpa,
    @Column(name = "min_buy_in_cents", nullable = false)
    var minBuyInCents: Long,
    @Column(columnDefinition = "TEXT")
    var notes: String? = null,
    @Column(name = "created_by_user_id", columnDefinition = "UUID")
    var createdByUserId: UUID? = null,
    @Column(name = "is_deleted", nullable = false)
    var isDeleted: Boolean = false,
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),
)

enum class GameTypeJpa {
    TEXAS_HOLDEM,
    OMAHA,
    OMAHA_HI_LO,
    SEVEN_CARD_STUD,
    FIVE_CARD_DRAW,
    MIXED_GAMES,
    OTHER,
}
