package pl.kmazurek.application.usecase.auth

import org.springframework.stereotype.Service
import pl.kmazurek.domain.model.user.Email
import pl.kmazurek.domain.model.user.User
import pl.kmazurek.domain.model.user.UserRole
import pl.kmazurek.domain.repository.UserRepository
import pl.kmazurek.infrastructure.security.PasswordEncoderService

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
