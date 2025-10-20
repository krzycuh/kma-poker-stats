package com.pokerstats.application.usecase.user

import com.pokerstats.domain.model.user.User
import com.pokerstats.domain.model.user.UserId
import com.pokerstats.domain.repository.UserRepository
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
