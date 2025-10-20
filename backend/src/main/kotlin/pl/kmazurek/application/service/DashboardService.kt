package pl.kmazurek.application.service

import org.springframework.stereotype.Service
import pl.kmazurek.application.dto.AdminDashboardDto
import pl.kmazurek.application.dto.CasualPlayerDashboardDto
import pl.kmazurek.application.dto.LeaderboardPositionDto
import pl.kmazurek.application.dto.PlayerStatsDto
import pl.kmazurek.application.dto.RecentSessionDto
import pl.kmazurek.application.dto.SystemStatsDto
import pl.kmazurek.domain.model.player.PlayerId
import pl.kmazurek.domain.model.user.UserId
import pl.kmazurek.domain.repository.GameSessionRepository
import pl.kmazurek.domain.repository.PlayerRepository
import pl.kmazurek.domain.repository.SessionResultRepository
import pl.kmazurek.domain.service.StatsCalculator

/**
 * Application Service for dashboard data
 * Orchestrates domain services and repositories
 */
@Service
class DashboardService(
    private val playerRepository: PlayerRepository,
    private val sessionRepository: GameSessionRepository,
    private val resultRepository: SessionResultRepository,
    private val statsCalculator: StatsCalculator,
) {
    /**
     * Get dashboard data for casual player
     */
    fun getCasualPlayerDashboard(userId: UserId): CasualPlayerDashboardDto {
        // Find player linked to this user
        val player =
            playerRepository.findByUserId(userId)
                ?: throw IllegalStateException("No player linked to user")

        // Get player's results
        val results = resultRepository.findByPlayerId(player.id)

        // Calculate stats
        val stats = statsCalculator.calculatePlayerStats(player.id, results)
        val playerStatsDto = PlayerStatsDto.fromDomain(stats)

        // Get recent sessions
        val recentSessions = getRecentSessions(player.id, limit = 5)

        // Get leaderboard position (simplified for now)
        val leaderboardPosition = calculateLeaderboardPosition(player.id, stats.netProfit.amountInCents)

        return CasualPlayerDashboardDto(
            personalStats = playerStatsDto,
            recentSessions = recentSessions,
            leaderboardPosition = leaderboardPosition,
        )
    }

    /**
     * Get dashboard data for admin
     */
    fun getAdminDashboard(userId: UserId): AdminDashboardDto {
        // Try to find player linked to this admin
        val player = playerRepository.findByUserId(userId)
        val personalStats =
            player?.let {
                val results = resultRepository.findByPlayerId(it.id)
                val stats = statsCalculator.calculatePlayerStats(it.id, results)
                PlayerStatsDto.fromDomain(stats)
            }

        // Get system-wide stats
        val allSessions = sessionRepository.findAll()
        val allResults =
            allSessions.flatMap { session ->
                resultRepository.findBySessionId(session.id)
            }
        val activePlayers = playerRepository.count().toInt()

        val systemStats =
            statsCalculator.calculateSystemStats(
                sessions = allSessions,
                results = allResults,
                activePlayers = activePlayers,
            )
        val systemStatsDto = SystemStatsDto.fromDomain(systemStats)

        // Get recent sessions across all players
        val recentSessions =
            getRecentSessions(playerId = null, limit = 10)

        return AdminDashboardDto(
            personalStats = personalStats,
            systemStats = systemStatsDto,
            recentSessions = recentSessions,
        )
    }

    /**
     * Get recent sessions for a player (or all if playerId is null)
     */
    private fun getRecentSessions(
        playerId: PlayerId?,
        limit: Int,
    ): List<RecentSessionDto> {
        val allSessions =
            sessionRepository.findAll()
                .filter { !it.isDeleted }
                .sortedByDescending { it.startTime }
                .take(limit * 2) // Get more than needed to ensure we have enough after filtering

        return allSessions.mapNotNull { session ->
            val sessionResults = resultRepository.findBySessionId(session.id)

            // If playerId is provided, only include sessions where player participated
            if (playerId != null) {
                val playerResult = sessionResults.find { it.playerId == playerId }
                playerResult?.let {
                    RecentSessionDto(
                        sessionId = session.id.toString(),
                        startTime = session.startTime,
                        location = session.location.value,
                        gameType = session.gameType.name,
                        playerCount = sessionResults.size,
                        personalProfitCents = it.profit().amountInCents,
                        isWinning = it.isWinning(),
                    )
                }
            } else {
                // Admin view: show all sessions
                RecentSessionDto(
                    sessionId = session.id.toString(),
                    startTime = session.startTime,
                    location = session.location.value,
                    gameType = session.gameType.name,
                    playerCount = sessionResults.size,
                    personalProfitCents = null,
                    isWinning = null,
                )
            }
        }.take(limit)
    }

    /**
     * Calculate leaderboard position (simplified version)
     * TODO: This should be enhanced in Phase 5 with proper leaderboard service
     */
    private fun calculateLeaderboardPosition(
        playerId: PlayerId,
        netProfit: Long,
    ): LeaderboardPositionDto? {
        val allPlayers = playerRepository.findAll()
        if (allPlayers.size <= 1) return null

        // Calculate profit for all players
        val playerProfits =
            allPlayers.map { player ->
                val results = resultRepository.findByPlayerId(player.id)
                val stats = statsCalculator.calculatePlayerStats(player.id, results)
                player.id to stats.netProfit.amountInCents
            }.sortedByDescending { it.second }

        // Find position
        val position = playerProfits.indexOfFirst { it.first == playerId } + 1

        return if (position > 0) {
            LeaderboardPositionDto(
                position = position,
                totalPlayers = playerProfits.size,
                metric = "Net Profit",
                value = formatCents(netProfit),
            )
        } else {
            null
        }
    }

    private fun formatCents(cents: Long): String {
        val dollars = cents / 100
        val remainingCents = kotlin.math.abs(cents % 100)
        return if (cents >= 0) {
            "+$$dollars.${remainingCents.toString().padStart(2, '0')}"
        } else {
            "-$${kotlin.math.abs(dollars)}.${remainingCents.toString().padStart(2, '0')}"
        }
    }
}
