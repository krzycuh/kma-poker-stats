package pl.kmazurek.application.dto

import pl.kmazurek.domain.service.PlayerStats
import pl.kmazurek.domain.service.SystemStats
import java.time.LocalDateTime

/**
 * DTOs for dashboard data transfer
 */
data class CasualPlayerDashboardDto(
    val personalStats: PlayerStatsDto,
    val recentSessions: List<RecentSessionDto>,
    val leaderboardPosition: LeaderboardPositionDto?,
)

/**
 * Dashboard data for admins
 */
data class AdminDashboardDto(
    val personalStats: PlayerStatsDto?,
    val systemStats: SystemStatsDto,
    val recentSessions: List<RecentSessionDto>,
)

/**
 * Player statistics DTO
 */
data class PlayerStatsDto(
    val playerId: String,
    val totalSessions: Int,
    val totalBuyInCents: Long,
    val totalCashOutCents: Long,
    val netProfitCents: Long,
    val roi: Double,
    val winRate: Double,
    val winningSessionsCount: Int,
    val losingSessionsCount: Int,
    val biggestWinCents: Long,
    val biggestLossCents: Long,
    val averageSessionProfitCents: Long,
    val currentStreak: Int,
    val firstPlaceCount: Int,
    val secondPlaceCount: Int,
    val thirdPlaceCount: Int,
) {
    companion object {
        fun fromDomain(stats: PlayerStats): PlayerStatsDto =
            PlayerStatsDto(
                playerId = stats.playerId.toString(),
                totalSessions = stats.totalSessions,
                totalBuyInCents = stats.totalBuyIn.amountInCents,
                totalCashOutCents = stats.totalCashOut.amountInCents,
                netProfitCents = stats.netProfit.amountInCents,
                roi = stats.roi,
                winRate = stats.winRate,
                winningSessionsCount = stats.winningSessionsCount,
                losingSessionsCount = stats.losingSessionsCount,
                biggestWinCents = stats.biggestWin.amountInCents,
                biggestLossCents = stats.biggestLoss.amountInCents,
                averageSessionProfitCents = stats.averageSessionProfit.amountInCents,
                currentStreak = stats.currentStreak,
                firstPlaceCount = stats.firstPlaceCount,
                secondPlaceCount = stats.secondPlaceCount,
                thirdPlaceCount = stats.thirdPlaceCount,
            )
    }
}

/**
 * System-wide statistics DTO
 */
data class SystemStatsDto(
    val totalSessions: Int,
    val activeSessions: Int,
    val totalPlayers: Int,
    val totalMoneyInPlayCents: Long,
) {
    companion object {
        fun fromDomain(stats: SystemStats): SystemStatsDto =
            SystemStatsDto(
                totalSessions = stats.totalSessions,
                activeSessions = stats.activeSessions,
                totalPlayers = stats.totalPlayers,
                totalMoneyInPlayCents = stats.totalMoneyInPlay.amountInCents,
            )
    }
}

/**
 * Recent session summary for dashboard
 */
data class RecentSessionDto(
    val sessionId: String,
    val startTime: LocalDateTime,
    val location: String,
    val gameType: String,
    val playerCount: Int,
    val personalProfitCents: Long?,
    val isWinning: Boolean?,
)

/**
 * Leaderboard position info
 */
data class LeaderboardPositionDto(
    val position: Int,
    val totalPlayers: Int,
    val metric: String,
    val value: String,
)
