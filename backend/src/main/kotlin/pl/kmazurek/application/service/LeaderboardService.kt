package pl.kmazurek.application.service

import org.springframework.stereotype.Service
import pl.kmazurek.application.dto.LeaderboardDto
import pl.kmazurek.application.dto.LeaderboardEntryDto
import pl.kmazurek.application.dto.LeaderboardMetric
import pl.kmazurek.application.usecase.player.PlayerAccessDeniedException
import pl.kmazurek.application.usecase.user.UserNotFoundException
import pl.kmazurek.domain.model.gamesession.GameSessionId
import pl.kmazurek.domain.model.player.Player
import pl.kmazurek.domain.model.player.PlayerId
import pl.kmazurek.domain.model.user.UserId
import pl.kmazurek.domain.repository.GameSessionRepository
import pl.kmazurek.domain.repository.PlayerRepository
import pl.kmazurek.domain.repository.SessionResultRepository
import pl.kmazurek.domain.repository.UserRepository
import pl.kmazurek.domain.service.PlayerStats
import pl.kmazurek.domain.service.StatsCalculator
import java.util.Locale

/**
 * Application Service for leaderboards
 * Phase 5: Statistics & Analytics
 */
@Service
class LeaderboardService(
    private val playerRepository: PlayerRepository,
    private val sessionRepository: GameSessionRepository,
    private val resultRepository: SessionResultRepository,
    private val statsCalculator: StatsCalculator,
    private val userRepository: UserRepository,
) {
    /**
     * Get leaderboard for a specific metric
     */
    fun getLeaderboard(
        metric: LeaderboardMetric,
        userId: UserId? = null,
        limit: Int = 50,
    ): LeaderboardDto {
        val viewerAccess = resolveViewerAccess(userId)

        val playerStats =
            when (viewerAccess.scope) {
                LeaderboardScope.GLOBAL -> buildGlobalPlayerStats()
                LeaderboardScope.PLAYER_NETWORK -> buildNetworkPlayerStats(viewerAccess)
            }

        if (playerStats.isEmpty()) {
            return emptyLeaderboard(metric)
        }

        val eligiblePlayers =
            if (metric == LeaderboardMetric.TOTAL_SESSIONS) {
                playerStats
            } else {
                playerStats.filter { it.stats.totalSessions > 0 }
            }

        if (eligiblePlayers.isEmpty()) {
            return emptyLeaderboard(metric)
        }

        // Sort players by the selected metric
        val sortedPlayers = sortPlayersByMetric(eligiblePlayers, metric)

        // Find current user's player
        val currentUserPlayer =
            viewerAccess.currentPlayer
                ?: userId?.let { uid ->
                    playerRepository.findByUserId(uid)
                }

        // Create leaderboard entries
        val entries =
            sortedPlayers.take(limit).mapIndexed { index, playerWithStats ->
                createLeaderboardEntry(
                    rank = index + 1,
                    playerId = playerWithStats.player.id,
                    playerName = playerWithStats.player.name.value,
                    stats = playerWithStats.stats,
                    metric = metric,
                    isCurrentUser = playerWithStats.player.id == currentUserPlayer?.id,
                )
            }

        // Find current user entry if not in top N
        val currentUserEntry =
            if (currentUserPlayer != null) {
                val currentUserRank =
                    sortedPlayers.indexOfFirst { (player, _) ->
                        player.id == currentUserPlayer.id
                    } + 1

                if (currentUserRank > 0 && currentUserRank > limit) {
                    val currentUserStats = playerStats.find { it.player.id == currentUserPlayer.id }?.stats
                    if (currentUserStats != null) {
                        createLeaderboardEntry(
                            rank = currentUserRank,
                            playerId = currentUserPlayer.id,
                            playerName = currentUserPlayer.name.value,
                            stats = currentUserStats,
                            metric = metric,
                            isCurrentUser = true,
                        )
                    } else {
                        null
                    }
                } else {
                    entries.find { it.isCurrentUser }
                }
            } else {
                null
            }

        return LeaderboardDto(
            metric = metric,
            entries = entries,
            currentUserEntry = currentUserEntry,
            totalEntries = sortedPlayers.size,
        )
    }

    private fun buildGlobalPlayerStats(): List<PlayerWithStats> {
        val allPlayers = playerRepository.findAll()
        if (allPlayers.isEmpty()) {
            return emptyList()
        }

        return allPlayers.map { player ->
            val allResults = resultRepository.findByPlayerId(player.id)
            // Filter out spectator results and results from deleted sessions
            val activeResults =
                allResults.filter { result ->
                    !result.isSpectator &&
                        (sessionRepository.findById(result.sessionId)?.isDeleted == false)
                }
            val stats = statsCalculator.calculatePlayerStats(player.id, activeResults)
            PlayerWithStats(player, stats)
        }
    }

    /**
     * Restrict leaderboard to players that share sessions with the viewer.
     */
    private fun buildNetworkPlayerStats(access: ViewerAccess): List<PlayerWithStats> {
        if (access.sessionIds.isEmpty()) {
            return emptyList()
        }

        val sharedResults = resultRepository.findBySessionIds(access.sessionIds)
        if (sharedResults.isEmpty()) {
            return emptyList()
        }

        val resultsByPlayer = sharedResults.groupBy { it.playerId }
        if (resultsByPlayer.isEmpty()) {
            return emptyList()
        }

        val connectedPlayers =
            playerRepository.findByIds(resultsByPlayer.keys)
                .filter { it.isActive }

        return connectedPlayers.mapNotNull { player ->
            val allPlayerResults = resultsByPlayer[player.id] ?: return@mapNotNull null
            // Filter out spectator results and results from deleted sessions
            val activeResults =
                allPlayerResults.filter { result ->
                    !result.isSpectator &&
                        (sessionRepository.findById(result.sessionId)?.isDeleted == false)
                }
            val stats = statsCalculator.calculatePlayerStats(player.id, activeResults)
            PlayerWithStats(player, stats)
        }
    }

    private fun sortPlayersByMetric(
        players: List<PlayerWithStats>,
        metric: LeaderboardMetric,
    ): List<PlayerWithStats> {
        return when (metric) {
            LeaderboardMetric.NET_PROFIT ->
                players.sortedByDescending { it.stats.netProfit.amountInCents }
            LeaderboardMetric.ROI ->
                players.sortedByDescending { it.stats.roi }
            LeaderboardMetric.WIN_RATE ->
                players.sortedByDescending { it.stats.winRate }
            LeaderboardMetric.CURRENT_STREAK ->
                players.sortedByDescending { it.stats.currentStreak }
            LeaderboardMetric.TOTAL_SESSIONS ->
                players.sortedByDescending { it.stats.totalSessions }
            LeaderboardMetric.AVERAGE_PROFIT ->
                players.sortedByDescending { it.stats.averageSessionProfit.amountInCents }
        }
    }

    private fun resolveViewerAccess(userId: UserId?): ViewerAccess {
        if (userId == null) {
            return ViewerAccess(LeaderboardScope.GLOBAL, currentPlayer = null, sessionIds = emptySet())
        }

        val user =
            userRepository.findById(userId)
                ?: throw UserNotFoundException("User not found")

        val linkedPlayer = playerRepository.findByUserId(userId)

        if (user.role.hasAdminPrivileges()) {
            return ViewerAccess(LeaderboardScope.GLOBAL, currentPlayer = linkedPlayer, sessionIds = emptySet())
        }

        val player =
            linkedPlayer ?: throw PlayerAccessDeniedException("User is not linked to any player")

        val viewerResults = resultRepository.findByPlayerId(player.id)
        val sharedSessionIds = viewerResults.map { it.sessionId }.toSet()

        return ViewerAccess(LeaderboardScope.PLAYER_NETWORK, currentPlayer = player, sessionIds = sharedSessionIds)
    }

    private fun emptyLeaderboard(metric: LeaderboardMetric) =
        LeaderboardDto(
            metric = metric,
            entries = emptyList(),
            currentUserEntry = null,
            totalEntries = 0,
        )

    /**
     * Create a leaderboard entry from player stats
     */
    private fun createLeaderboardEntry(
        rank: Int,
        playerId: PlayerId,
        playerName: String,
        stats: PlayerStats,
        metric: LeaderboardMetric,
        isCurrentUser: Boolean,
    ): LeaderboardEntryDto {
        val (value, formatted) =
            when (metric) {
                LeaderboardMetric.NET_PROFIT -> {
                    val cents = stats.netProfit.amountInCents
                    cents.toDouble() to formatCents(cents)
                }
                LeaderboardMetric.ROI -> {
                    stats.roi to "${String.format("%.1f", stats.roi)}%"
                }
                LeaderboardMetric.WIN_RATE -> {
                    stats.winRate to "${String.format("%.1f", stats.winRate)}%"
                }
                LeaderboardMetric.CURRENT_STREAK -> {
                    stats.currentStreak.toDouble() to stats.currentStreak.toString()
                }
                LeaderboardMetric.TOTAL_SESSIONS -> {
                    stats.totalSessions.toDouble() to stats.totalSessions.toString()
                }
                LeaderboardMetric.AVERAGE_PROFIT -> {
                    val cents = stats.averageSessionProfit.amountInCents
                    cents.toDouble() to formatCents(cents)
                }
            }

        return LeaderboardEntryDto(
            rank = rank,
            playerId = playerId.toString(),
            playerName = playerName,
            value = value,
            valueFormatted = formatted,
            sessionsPlayed = stats.totalSessions,
            isCurrentUser = isCurrentUser,
        )
    }

    /**
     * Format cents to PLN string
     */
    private fun formatCents(cents: Long): String {
        val absoluteValue = kotlin.math.abs(cents) / 100.0
        val formattedAmount = String.format(Locale.US, "%,.2f", absoluteValue)
        return if (cents >= 0) {
            "PLN $formattedAmount"
        } else {
            "-PLN $formattedAmount"
        }
    }

    private data class PlayerWithStats(
        val player: Player,
        val stats: PlayerStats,
    )

    private data class ViewerAccess(
        val scope: LeaderboardScope,
        val currentPlayer: Player?,
        val sessionIds: Set<GameSessionId>,
    )

    private enum class LeaderboardScope {
        GLOBAL,
        PLAYER_NETWORK,
    }
}
