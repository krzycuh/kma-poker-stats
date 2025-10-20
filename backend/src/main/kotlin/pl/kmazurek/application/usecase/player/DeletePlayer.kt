package pl.kmazurek.application.usecase.player

import org.springframework.stereotype.Service
import pl.kmazurek.domain.model.player.Player
import pl.kmazurek.domain.model.player.PlayerId
import pl.kmazurek.domain.repository.PlayerRepository

/**
 * Use Case: Delete (deactivate) a player
 */
@Service
class DeletePlayer(
    private val playerRepository: PlayerRepository,
) {
    fun execute(playerId: PlayerId): Player {
        val player =
            playerRepository.findById(playerId)
                ?: throw PlayerNotFoundException("Player not found")

        val deactivatedPlayer = player.deactivate()
        return playerRepository.save(deactivatedPlayer)
    }
}
