package pl.kmazurek.application.service

import org.springframework.stereotype.Service
import pl.kmazurek.application.dto.LeaderboardDto
import pl.kmazurek.application.dto.LeaderboardEntryDto
import pl.kmazurek.application.dto.LeaderboardMetric
import pl.kmazurek.domain.model.player.PlayerId
import pl.kmazurek.domain.model.user.UserId
import pl.kmazurek.domain.repository.PlayerRepository
import pl.kmazurek.domain.repository.SessionResultRepository
import pl.kmazurek.domain.service.PlayerStats
import pl.kmazurek.domain.service.StatsCalculator

/**
 * Application Service for leaderboards
 * Phase 5: Statistics & Analytics
 */
@Service
class LeaderboardService(
    private val playerRepository: PlayerRepository,
    private val resultRepository: SessionResultRepository,
    private val statsCalculator: StatsCalculator,
) {
    /**
     * Get leaderboard for a specific metric
     */
    fun getLeaderboard(
        metric: LeaderboardMetric,
        userId: UserId? = null,
        limit: Int = 50,
    ): LeaderboardDto {
        // Get all players and calculate their stats
        val allPlayers = playerRepository.findAll()

        val playerStatsMap =
            allPlayers.associate { player ->
                val results = resultRepository.findByPlayerId(player.id)
                val stats = statsCalculator.calculatePlayerStats(player.id, results)
                player to stats
            }

        // Filter out players with no sessions (unless metric is TOTAL_SESSIONS)
        val filteredPlayers =
            if (metric == LeaderboardMetric.TOTAL_SESSIONS) {
                playerStatsMap
            } else {
                playerStatsMap.filter { (_, stats) -> stats.totalSessions > 0 }
            }

        // Sort players by the selected metric
        val sortedPlayers =
            when (metric) {
                LeaderboardMetric.NET_PROFIT ->
                    filteredPlayers.toList()
                        .sortedByDescending { (_, stats) -> stats.netProfit.amountInCents }
                LeaderboardMetric.ROI ->
                    filteredPlayers.toList()
                        .sortedByDescending { (_, stats) -> stats.roi }
                LeaderboardMetric.WIN_RATE ->
                    filteredPlayers.toList()
                        .sortedByDescending { (_, stats) -> stats.winRate }
                LeaderboardMetric.CURRENT_STREAK ->
                    filteredPlayers.toList()
                        .sortedByDescending { (_, stats) -> stats.currentStreak }
                LeaderboardMetric.TOTAL_SESSIONS ->
                    filteredPlayers.toList()
                        .sortedByDescending { (_, stats) -> stats.totalSessions }
                LeaderboardMetric.AVERAGE_PROFIT ->
                    filteredPlayers.toList()
                        .sortedByDescending { (_, stats) -> stats.averageSessionProfit.amountInCents }
            }

        // Find current user's player
        val currentUserPlayer =
            userId?.let { uid ->
                playerRepository.findByUserId(uid)
            }

        // Create leaderboard entries
        val entries =
            sortedPlayers.take(limit).mapIndexed { index, (player, stats) ->
                createLeaderboardEntry(
                    rank = index + 1,
                    playerId = player.id,
                    playerName = player.name.value,
                    stats = stats,
                    metric = metric,
                    isCurrentUser = player.id == currentUserPlayer?.id,
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
                    val currentUserStats = playerStatsMap[currentUserPlayer]
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
     * Format cents to dollar string
     */
    private fun formatCents(cents: Long): String {
        val dollars = cents / 100
        val remainingCents = kotlin.math.abs(cents % 100)
        return if (cents >= 0) {
            "$$dollars.${remainingCents.toString().padStart(2, '0')}"
        } else {
            "-$${kotlin.math.abs(dollars)}.${remainingCents.toString().padStart(2, '0')}"
        }
    }
}
