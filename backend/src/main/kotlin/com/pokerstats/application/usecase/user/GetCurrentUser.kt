package com.pokerstats.application.usecase.user

import com.pokerstats.domain.model.user.User
import com.pokerstats.domain.model.user.UserId
import com.pokerstats.domain.repository.UserRepository
import org.springframework.stereotype.Service

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
