package pl.kmazurek.application.usecase.player

import org.springframework.stereotype.Service
import pl.kmazurek.domain.model.player.Player
import pl.kmazurek.domain.model.player.PlayerId
import pl.kmazurek.domain.repository.PlayerRepository

/**
 * Use Case: Get a player by ID
 */
@Service
class GetPlayer(
    private val playerRepository: PlayerRepository,
) {
    fun execute(playerId: PlayerId): Player {
        return playerRepository.findById(playerId)
            ?: throw PlayerNotFoundException("Player not found")
    }
}
