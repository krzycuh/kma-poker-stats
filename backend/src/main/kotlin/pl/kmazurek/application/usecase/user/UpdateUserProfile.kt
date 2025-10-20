package pl.kmazurek.application.usecase.user

import pl.kmazurek.domain.model.user.User
import pl.kmazurek.domain.model.user.UserId
import pl.kmazurek.domain.repository.UserRepository
import org.springframework.stereotype.Service

/**
 * Use Case: Update user profile
 */
@Service
class UpdateUserProfile(
    private val userRepository: UserRepository,
) {
    fun execute(
        userId: UserId,
        name: String,
        avatarUrl: String?,
    ): User {
        val user =
            userRepository.findById(userId)
                ?: throw UserNotFoundException("User not found")

        val updatedUser = user.updateProfile(name, avatarUrl)
        return userRepository.save(updatedUser)
    }
}
