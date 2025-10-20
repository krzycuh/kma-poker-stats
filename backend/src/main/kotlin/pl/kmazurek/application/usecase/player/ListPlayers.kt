package pl.kmazurek.application.usecase.player

import org.springframework.stereotype.Service
import pl.kmazurek.domain.model.player.Player
import pl.kmazurek.domain.repository.PlayerRepository

/**
 * Use Case: List all players with optional filtering
 */
@Service
class ListPlayers(
    private val playerRepository: PlayerRepository,
) {
    fun execute(query: ListPlayersQuery): List<Player> {
        return if (query.searchTerm.isNullOrBlank()) {
            playerRepository.findAll(query.includeInactive)
        } else {
            playerRepository.findByNameContaining(query.searchTerm, query.includeInactive)
        }
    }
}

data class ListPlayersQuery(
    val searchTerm: String? = null,
    val includeInactive: Boolean = false,
)
