package pl.kmazurek.domain.model.user

import java.time.LocalDateTime

/**
 * User Aggregate Root
 * Represents a user in the Identity & Access bounded context
 * Domain entity - no framework dependencies
 */
data class User(
    val id: UserId,
    val email: Email,
    val passwordHash: HashedPassword,
    val name: String,
    val role: UserRole,
    val avatarUrl: String? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    init {
        require(name.isNotBlank()) { "User name cannot be blank" }
        require(name.length <= 255) { "User name cannot exceed 255 characters" }
    }

    /**
     * Update user profile information
     */
    fun updateProfile(
        name: String,
        avatarUrl: String?,
    ): User {
        require(name.isNotBlank()) { "User name cannot be blank" }
        require(name.length <= 255) { "User name cannot exceed 255 characters" }

        return copy(
            name = name,
            avatarUrl = avatarUrl,
            updatedAt = LocalDateTime.now(),
        )
    }

    /**
     * Update user password
     */
    fun updatePassword(newPasswordHash: HashedPassword): User {
        return copy(
            passwordHash = newPasswordHash,
            updatedAt = LocalDateTime.now(),
        )
    }

    /**
     * Check if user has admin privileges
     */
    fun isAdmin() = role.hasAdminPrivileges()

    companion object {
        /**
         * Factory method to create a new user
         */
        fun create(
            email: Email,
            passwordHash: HashedPassword,
            name: String,
            role: UserRole = UserRole.CASUAL_PLAYER,
            avatarUrl: String? = null,
        ): User {
            val now = LocalDateTime.now()
            return User(
                id = UserId.generate(),
                email = email,
                passwordHash = passwordHash,
                name = name,
                role = role,
                avatarUrl = avatarUrl,
                createdAt = now,
                updatedAt = now,
            )
        }
    }
}
