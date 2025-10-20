package com.pokerstats.application.usecase.auth

import com.pokerstats.domain.model.user.Email
import com.pokerstats.domain.model.user.User
import com.pokerstats.domain.model.user.UserRole
import com.pokerstats.domain.repository.UserRepository
import com.pokerstats.infrastructure.security.PasswordEncoderService
import org.springframework.stereotype.Service

/**
 * Use Case: Register a new user
 * Following DDD - orchestrates domain operations
 */
@Service
class RegisterUser(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoderService,
) {
    fun execute(
        email: String,
        password: String,
        name: String,
    ): User {
        val userEmail = Email(email)

        // Check if email already exists
        if (userRepository.existsByEmail(userEmail)) {
            throw EmailAlreadyExistsException("Email $email is already registered")
        }

        // Hash password and create user
        val hashedPassword = passwordEncoder.hash(password)
        val user =
            User.create(
                email = userEmail,
                passwordHash = hashedPassword,
                name = name,
                role = UserRole.CASUAL_PLAYER,
            )

        // Save and return
        return userRepository.save(user)
    }
}

class EmailAlreadyExistsException(message: String) : RuntimeException(message)
