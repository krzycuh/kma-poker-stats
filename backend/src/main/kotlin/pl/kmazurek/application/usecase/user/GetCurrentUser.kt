package pl.kmazurek.application.usecase.user

import org.springframework.stereotype.Service
import pl.kmazurek.domain.model.user.User
import pl.kmazurek.domain.model.user.UserId
import pl.kmazurek.domain.repository.UserRepository

/**
 * Use Case: Get current authenticated user
 */
@Service
class GetCurrentUser(
    private val userRepository: UserRepository,
) {
    fun execute(userId: UserId): User {
        return userRepository.findById(userId)
            ?: throw UserNotFoundException("User not found")
    }
}

class UserNotFoundException(message: String) : RuntimeException(message)
