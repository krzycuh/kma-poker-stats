package pl.kmazurek.application.usecase.player

import org.springframework.stereotype.Service
import pl.kmazurek.domain.model.player.Player
import pl.kmazurek.domain.model.player.PlayerName
import pl.kmazurek.domain.model.user.UserId
import pl.kmazurek.domain.repository.PlayerRepository
import pl.kmazurek.domain.repository.UserRepository

/**
 * Use Case: Create a new player
 */
@Service
class CreatePlayer(
    private val playerRepository: PlayerRepository,
    private val userRepository: UserRepository,
) {
    fun execute(command: CreatePlayerCommand): Player {
        // Check if name already exists
        if (playerRepository.existsByName(command.name)) {
            throw PlayerAlreadyExistsException("Player with name '${command.name}' already exists")
        }

        val linkedUserId =
            command.userId?.let {
                val userId = UserId.fromString(it)
                val user = userRepository.findById(userId) ?: throw LinkedUserNotFoundException("User not found")
                val existingLinkedPlayer = playerRepository.findByUserId(user.id)
                if (existingLinkedPlayer != null) {
                    throw UserAlreadyLinkedException("User is already linked to player '${existingLinkedPlayer.name.value}'")
                }
                userId
            }

        val player =
            Player.create(
                name = PlayerName(command.name),
                avatarUrl = command.avatarUrl,
                userId = linkedUserId,
            )

        return playerRepository.save(player)
    }
}

data class CreatePlayerCommand(
    val name: String,
    val avatarUrl: String? = null,
    val userId: String? = null,
)
