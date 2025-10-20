package com.pokerstats.application.usecase.auth

import com.pokerstats.domain.model.user.Email
import com.pokerstats.domain.model.user.User
import com.pokerstats.domain.repository.UserRepository
import com.pokerstats.infrastructure.security.JwtService
import com.pokerstats.infrastructure.security.PasswordEncoderService
import org.springframework.stereotype.Service

/**
 * Use Case: Login user
 */
@Service
class LoginUser(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoderService,
    private val jwtService: JwtService,
) {
    data class LoginResult(
        val user: User,
        val accessToken: String,
        val refreshToken: String,
    )

    fun execute(
        email: String,
        password: String,
    ): LoginResult {
        val userEmail = Email(email)

        // Find user by email
        val user =
            userRepository.findByEmail(userEmail)
                ?: throw InvalidCredentialsException("Invalid email or password")

        // Verify password
        if (!passwordEncoder.matches(password, user.passwordHash)) {
            throw InvalidCredentialsException("Invalid email or password")
        }

        // Generate tokens
        val accessToken = jwtService.generateAccessToken(user.id, user.email, user.role)
        val refreshToken = jwtService.generateRefreshToken(user.id)

        return LoginResult(user, accessToken, refreshToken)
    }
}

class InvalidCredentialsException(message: String) : RuntimeException(message)
