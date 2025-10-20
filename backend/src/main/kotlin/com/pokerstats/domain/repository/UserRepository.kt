package com.pokerstats.domain.repository

import com.pokerstats.domain.model.user.Email
import com.pokerstats.domain.model.user.User
import com.pokerstats.domain.model.user.UserId

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
}
