package pl.kmazurek.application.usecase.auth

import org.springframework.stereotype.Service
import pl.kmazurek.domain.model.user.Email
import pl.kmazurek.domain.model.user.User
import pl.kmazurek.domain.repository.UserRepository
import pl.kmazurek.infrastructure.security.JwtService
import pl.kmazurek.infrastructure.security.PasswordEncoderService

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
