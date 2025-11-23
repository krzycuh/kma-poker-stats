package pl.kmazurek.domain.repository

import pl.kmazurek.domain.model.user.Email
import pl.kmazurek.domain.model.user.User
import pl.kmazurek.domain.model.user.UserId

/**
 * Repository interface (port) for User aggregate
 * Part of domain layer - defines contract but no implementation
 */
interface UserRepository {
    /**
     * Find user by unique identifier
     */
    fun findById(id: UserId): User?

    /**
     * Find user by email address
     */
    fun findByEmail(email: Email): User?

    /**
     * Check if email already exists
     */
    fun existsByEmail(email: Email): Boolean

    /**
     * Save user (create or update)
     */
    fun save(user: User): User

    /**
     * Delete user by ID
     */
    fun deleteById(id: UserId)

    /**
     * Find users that are not linked to any player.
     * Returns a pair of (users, totalCount) for pagination support.
     */
    fun findUnlinkedUsers(
        searchTerm: String?,
        page: Int,
        pageSize: Int,
    ): Pair<List<User>, Long>

    /**
     * Count users that are not linked to any player.
     */
    fun countUnlinkedUsers(): Long
}
