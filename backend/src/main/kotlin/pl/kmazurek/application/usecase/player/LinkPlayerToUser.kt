package pl.kmazurek.application.usecase.player

import org.springframework.stereotype.Service
import pl.kmazurek.domain.model.player.PlayerId
import pl.kmazurek.domain.model.user.UserId
import pl.kmazurek.domain.repository.PlayerRepository
import pl.kmazurek.domain.repository.UserRepository

/**
 * Use Case: Link an existing player to an existing user account.
 */
@Service
class LinkPlayerToUser(
    private val playerRepository: PlayerRepository,
    private val userRepository: UserRepository,
) {
    fun execute(
        playerId: PlayerId,
        userId: UserId,
    ): pl.kmazurek.domain.model.player.Player {
        val player =
            playerRepository.findById(playerId)
                ?: throw PlayerNotFoundException("Player not found")

        if (!player.isActive) {
            throw PlayerInactiveException("Cannot link inactive player")
        }
        if (player.userId != null) {
            throw PlayerAlreadyLinkedException("Player is already linked to a user")
        }

        val user =
            userRepository.findById(userId)
                ?: throw LinkedUserNotFoundException("User not found")

        val existingLinkedPlayer = playerRepository.findByUserId(user.id)
        if (existingLinkedPlayer != null && existingLinkedPlayer.id != playerId) {
            throw UserAlreadyLinkedException("User is already linked to player '${existingLinkedPlayer.name.value}'")
        }

        val updatedPlayer = player.linkToUser(user.id)
        return playerRepository.save(updatedPlayer)
    }
}

/**
 * Use Case: Remove link between a player and a user account.
 */
@Service
class UnlinkPlayerFromUser(
    private val playerRepository: PlayerRepository,
) {
    fun execute(playerId: PlayerId): pl.kmazurek.domain.model.player.Player {
        val player =
            playerRepository.findById(playerId)
                ?: throw PlayerNotFoundException("Player not found")

        if (player.userId == null) {
            throw PlayerNotLinkedException("Player is not linked to any user")
        }

        val updatedPlayer = player.unlinkFromUser()
        return playerRepository.save(updatedPlayer)
    }
}

class PlayerInactiveException(message: String) : RuntimeException(message)

class PlayerNotLinkedException(message: String) : RuntimeException(message)

class PlayerAlreadyLinkedException(message: String) : RuntimeException(message)

class LinkedUserNotFoundException(message: String) : RuntimeException(message)

class UserAlreadyLinkedException(message: String) : RuntimeException(message)
