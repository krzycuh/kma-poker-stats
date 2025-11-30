package pl.kmazurek.application.service

import org.springframework.stereotype.Service
import pl.kmazurek.application.dto.SharedPlayerSummaryDto
import pl.kmazurek.application.usecase.player.PlayerAccessDeniedException
import pl.kmazurek.domain.model.gamesession.GameSessionId
import pl.kmazurek.domain.model.player.Player
import pl.kmazurek.domain.model.player.PlayerId
import pl.kmazurek.domain.model.user.UserId
import pl.kmazurek.domain.repository.GameSessionRepository
import pl.kmazurek.domain.repository.PlayerRepository
import pl.kmazurek.domain.repository.SessionResultRepository

@Service
class PlayerNetworkService(
    private val playerRepository: PlayerRepository,
    private val sessionRepository: GameSessionRepository,
    private val sessionResultRepository: SessionResultRepository,
) {
    companion object {
        private const val DEFAULT_LIMIT = 25
        private const val MAX_LIMIT = 100
    }

    fun searchVisiblePlayers(
        userId: UserId,
        searchTerm: String?,
        limit: Int?,
    ): List<SharedPlayerSummaryDto> {
        val viewerPlayer = findPlayerForUser(userId)
        val viewerResults = sessionResultRepository.findByPlayerId(viewerPlayer.id)
        if (viewerResults.isEmpty()) {
            return emptyList()
        }

        // Filter to only include non-deleted sessions
        val activeSessionIds =
            viewerResults.map { it.sessionId }.toSet()
                .filter { sessionId ->
                    sessionRepository.findById(sessionId)?.isDeleted == false
                }.toSet()
        if (activeSessionIds.isEmpty()) {
            return emptyList()
        }

        val sharedResults =
            sessionResultRepository.findBySessionIds(activeSessionIds)
                .filter { it.playerId != viewerPlayer.id }
        if (sharedResults.isEmpty()) {
            return emptyList()
        }

        val groupedByPlayer = sharedResults.groupBy { it.playerId }
        val connectedPlayerIds = groupedByPlayer.keys

        val playersById =
            playerRepository.findByIds(connectedPlayerIds).asSequence()
                .filter { it.isActive }
                .associateBy { it.id }

        val normalizedSearch = searchTerm?.trim()?.lowercase()

        val summaries =
            groupedByPlayer.mapNotNull { (playerId, results) ->
                val player = playersById[playerId] ?: return@mapNotNull null

                if (normalizedSearch != null && !player.matchesSearch(normalizedSearch)) {
                    return@mapNotNull null
                }

                val sessionCount = results.map { it.sessionId }.toSet().size
                val lastSharedAt = results.maxOfOrNull { it.createdAt }

                SharedPlayerSummaryDto(
                    playerId = player.id.toString(),
                    name = player.name.value,
                    avatarUrl = player.avatarUrl,
                    sharedSessionsCount = sessionCount,
                    lastSharedSessionAt = lastSharedAt,
                )
            }
                .sortedWith(
                    compareByDescending<SharedPlayerSummaryDto> { it.sharedSessionsCount }
                        .thenBy { it.name.lowercase() },
                )

        val effectiveLimit =
            limit?.takeIf { it > 0 }
                ?.coerceAtMost(MAX_LIMIT)
                ?: DEFAULT_LIMIT

        return summaries.take(effectiveLimit)
    }

    fun sharedSessionIdsBetween(
        userId: UserId,
        targetPlayerId: PlayerId,
    ): Pair<Player, Set<GameSessionId>> {
        val viewerPlayer = findPlayerForUser(userId)
        val viewerSessions =
            sessionResultRepository.findByPlayerId(viewerPlayer.id)
                .map { it.sessionId }
                .filter { sessionId ->
                    sessionRepository.findById(sessionId)?.isDeleted == false
                }.toSet()
        if (viewerSessions.isEmpty()) {
            throw PlayerAccessDeniedException("You do not share any sessions with this player")
        }

        val targetResults = sessionResultRepository.findByPlayerId(targetPlayerId)
        val sharedSessions =
            targetResults
                .map { it.sessionId }
                .filter { it in viewerSessions }
                .filter { sessionId ->
                    sessionRepository.findById(sessionId)?.isDeleted == false
                }.toSet()

        if (sharedSessions.isEmpty()) {
            throw PlayerAccessDeniedException("You do not share any sessions with this player")
        }

        val targetPlayer =
            playerRepository.findById(targetPlayerId)
                ?: throw PlayerAccessDeniedException("Player is unavailable")

        if (!targetPlayer.isActive) {
            throw PlayerAccessDeniedException("Player is unavailable")
        }

        return targetPlayer to sharedSessions
    }

    private fun findPlayerForUser(userId: UserId): Player {
        return playerRepository.findByUserId(userId)
            ?: throw PlayerAccessDeniedException("User is not linked to any player")
    }

    private fun Player.matchesSearch(normalizedSearch: String): Boolean {
        return this.name.value.lowercase().contains(normalizedSearch)
    }
}
