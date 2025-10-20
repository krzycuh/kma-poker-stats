package pl.kmazurek.application.usecase.player

import org.springframework.stereotype.Service
import pl.kmazurek.domain.model.player.Player
import pl.kmazurek.domain.model.player.PlayerName
import pl.kmazurek.domain.model.user.UserId
import pl.kmazurek.domain.repository.PlayerRepository

/**
 * Use Case: Create a new player
 */
@Service
class CreatePlayer(
    private val playerRepository: PlayerRepository,
) {
    fun execute(command: CreatePlayerCommand): Player {
        // Check if name already exists
        if (playerRepository.existsByName(command.name)) {
            throw PlayerAlreadyExistsException("Player with name '${command.name}' already exists")
        }

        val player =
            Player.create(
                name = PlayerName(command.name),
                avatarUrl = command.avatarUrl,
                userId = command.userId?.let { UserId.fromString(it) },
            )

        return playerRepository.save(player)
    }
}

data class CreatePlayerCommand(
    val name: String,
    val avatarUrl: String? = null,
    val userId: String? = null,
)

class PlayerAlreadyExistsException(message: String) : RuntimeException(message)
