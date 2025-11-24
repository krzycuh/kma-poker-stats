package pl.kmazurek.application.dto

import pl.kmazurek.domain.model.user.User
import pl.kmazurek.domain.model.user.UserRole
import java.time.LocalDateTime

/**
 * DTOs used for admin-only user management endpoints.
 */
data class UserSummaryDto(
    val id: String,
    val email: String,
    val name: String,
    val role: UserRole,
    val avatarUrl: String?,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun fromDomain(user: User): UserSummaryDto =
            UserSummaryDto(
                id = user.id.toString(),
                email = user.email.value,
                name = user.name,
                role = user.role,
                avatarUrl = user.avatarUrl,
                createdAt = user.createdAt,
            )
    }
}
