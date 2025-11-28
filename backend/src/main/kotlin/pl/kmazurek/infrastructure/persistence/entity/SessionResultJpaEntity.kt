package pl.kmazurek.infrastructure.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

/**
 * JPA Entity for SessionResult table
 * This is in the infrastructure layer - separate from domain model
 */
@Entity
@Table(name = "session_results")
class SessionResultJpaEntity(
    @Id
    @Column(columnDefinition = "UUID")
    var id: UUID = UUID.randomUUID(),
    @Column(name = "session_id", nullable = false, columnDefinition = "UUID")
    var sessionId: UUID,
    @Column(name = "player_id", nullable = false, columnDefinition = "UUID")
    var playerId: UUID,
    @Column(name = "buy_in_cents", nullable = false)
    var buyInCents: Long,
    @Column(name = "cash_out_cents", nullable = false)
    var cashOutCents: Long,
    @Column(name = "placement")
    var placement: Int? = null,
    @Column(columnDefinition = "TEXT")
    var notes: String? = null,
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),
)
