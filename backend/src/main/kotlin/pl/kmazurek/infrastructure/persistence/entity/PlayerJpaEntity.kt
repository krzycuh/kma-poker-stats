package pl.kmazurek.infrastructure.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

/**
 * JPA Entity for Player table
 * This is in the infrastructure layer - separate from domain model
 */
@Entity
@Table(name = "players")
class PlayerJpaEntity(
    @Id
    @Column(columnDefinition = "UUID")
    var id: UUID = UUID.randomUUID(),
    @Column(nullable = false, length = 255)
    var name: String,
    @Column(name = "avatar_url", length = 500)
    var avatarUrl: String? = null,
    @Column(name = "user_id", columnDefinition = "UUID")
    var userId: UUID? = null,
    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),
)
