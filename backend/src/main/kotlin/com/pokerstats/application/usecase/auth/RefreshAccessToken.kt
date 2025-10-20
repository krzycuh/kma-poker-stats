package com.pokerstats.application.usecase.auth

import com.pokerstats.domain.repository.UserRepository
import com.pokerstats.infrastructure.security.JwtService
import org.springframework.stereotype.Service

/**
 * Use Case: Refresh access token using refresh token
 */
@Service
class RefreshAccessToken(
    private val userRepository: UserRepository,
    private val jwtService: JwtService,
) {
    fun execute(refreshToken: String): String {
        // Validate refresh token
        if (!jwtService.isTokenValid(refreshToken) || !jwtService.isRefreshToken(refreshToken)) {
            throw InvalidRefreshTokenException("Invalid or expired refresh token")
        }

        // Extract user ID and fetch user
        val userId = jwtService.extractUserId(refreshToken)
        val user =
            userRepository.findById(userId)
                ?: throw InvalidRefreshTokenException("User not found")

        // Generate new access token
        return jwtService.generateAccessToken(user.id, user.email, user.role)
    }
}

class InvalidRefreshTokenException(message: String) : RuntimeException(message)
