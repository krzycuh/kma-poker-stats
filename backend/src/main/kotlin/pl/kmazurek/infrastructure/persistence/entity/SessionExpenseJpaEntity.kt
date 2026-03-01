package pl.kmazurek.infrastructure.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

/**
 * JPA Entity for SessionExpense table
 * This is in the infrastructure layer - separate from domain model
 */
@Entity
@Table(name = "session_expenses")
class SessionExpenseJpaEntity(
    @Id
    @Column(columnDefinition = "UUID")
    var id: UUID = UUID.randomUUID(),
    @Column(name = "session_id", nullable = false, columnDefinition = "UUID")
    var sessionId: UUID,
    @Column(name = "payer_player_id", nullable = false, columnDefinition = "UUID")
    var payerPlayerId: UUID,
    @Column(nullable = false, length = 255)
    var description: String,
    @Column(name = "amount_cents", nullable = false)
    var amountCents: Long,
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),
)
