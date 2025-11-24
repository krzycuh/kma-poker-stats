package pl.kmazurek.application.usecase.player

import org.springframework.stereotype.Service
import pl.kmazurek.domain.model.player.Player
import pl.kmazurek.domain.model.player.PlayerId
import pl.kmazurek.domain.model.player.PlayerName
import pl.kmazurek.domain.repository.PlayerRepository

/**
 * Use Case: Update an existing player
 */
@Service
class UpdatePlayer(
    private val playerRepository: PlayerRepository,
) {
    fun execute(
        playerId: PlayerId,
        command: UpdatePlayerCommand,
    ): Player {
        val player =
            playerRepository.findById(playerId)
                ?: throw PlayerNotFoundException("Player not found")

        val updatedPlayer =
            player.update(
                name = PlayerName(command.name),
                avatarUrl = command.avatarUrl,
            )

        return playerRepository.save(updatedPlayer)
    }
}

data class UpdatePlayerCommand(
    val name: String,
    val avatarUrl: String? = null,
)
