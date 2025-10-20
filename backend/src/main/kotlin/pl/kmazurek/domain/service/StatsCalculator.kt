package pl.kmazurek.domain.service

import pl.kmazurek.domain.model.gamesession.GameSession
import pl.kmazurek.domain.model.gamesession.SessionResult
import pl.kmazurek.domain.model.player.PlayerId
import pl.kmazurek.domain.model.shared.Money

/**
 * Domain Service for calculating player statistics
 * Pure business logic, no framework dependencies
 * Following DDD principle: complex logic that doesn't fit in a single entity
 */
class StatsCalculator {
    /**
     * Calculate comprehensive statistics for a player
     */
    fun calculatePlayerStats(
        playerId: PlayerId,
        results: List<SessionResult>,
    ): PlayerStats {
        if (results.isEmpty()) {
            return PlayerStats.empty(playerId)
        }

        val totalSessions = results.size
        val totalBuyIn = results.fold(Money.ZERO) { acc, r -> acc + r.buyIn }
        val totalCashOut = results.fold(Money.ZERO) { acc, r -> acc + r.cashOut }
        val netProfit = totalCashOut - totalBuyIn

        val roi =
            if (totalBuyIn.amountInCents > 0) {
                ((netProfit.amountInCents.toDouble() / totalBuyIn.amountInCents) * 100.0)
            } else {
                0.0
            }

        val winningResults = results.filter { it.isWinning() }
        val losingResults = results.filter { it.isLosing() }
        val winRate =
            if (totalSessions > 0) {
                (winningResults.size.toDouble() / totalSessions * 100.0)
            } else {
                0.0
            }

        val biggestWin =
            results.maxOfOrNull { it.profit().amountInCents }
                ?.let { Money.ofCents(it) } ?: Money.ZERO
        val biggestLoss =
            results.minOfOrNull { it.profit().amountInCents }
                ?.let { Money.ofCents(it) } ?: Money.ZERO
        val avgSessionProfit =
            if (totalSessions > 0) {
                Money.ofCents(netProfit.amountInCents / totalSessions)
            } else {
                Money.ZERO
            }

        val currentStreak = calculateStreak(results.sortedBy { it.createdAt })

        return PlayerStats(
            playerId = playerId,
            totalSessions = totalSessions,
            totalBuyIn = totalBuyIn,
            totalCashOut = totalCashOut,
            netProfit = netProfit,
            roi = roi,
            winRate = winRate,
            winningSessionsCount = winningResults.size,
            losingSessionsCount = losingResults.size,
            biggestWin = biggestWin,
            biggestLoss = biggestLoss,
            averageSessionProfit = avgSessionProfit,
            currentStreak = currentStreak,
        )
    }

    /**
     * Calculate current winning/losing streak
     * Positive = winning streak, Negative = losing streak, Zero = no streak
     */
    private fun calculateStreak(sortedResults: List<SessionResult>): Int {
        if (sortedResults.isEmpty()) return 0

        var streak = 0
        var lastWasWin: Boolean? = null

        for (result in sortedResults.reversed()) {
            val isWin = result.isWinning()

            when {
                lastWasWin == null -> {
                    // First result
                    streak = if (isWin) 1 else -1
                    lastWasWin = isWin
                }

                lastWasWin == isWin -> {
                    // Continue streak
                    streak = if (isWin) streak + 1 else streak - 1
                }

                else -> {
                    // Streak broken
                    break
                }
            }
        }

        return streak
    }

    /**
     * Calculate system-wide statistics
     */
    fun calculateSystemStats(
        sessions: List<GameSession>,
        results: List<SessionResult>,
        activePlayers: Int,
    ): SystemStats {
        val totalSessions = sessions.count { !it.isDeleted }
        val totalActiveSessions = sessions.count { it.isActive() }

        val totalMoneyInPlay =
            results.fold(Money.ZERO) { acc, r -> acc + r.buyIn }

        return SystemStats(
            totalSessions = totalSessions,
            activeSessions = totalActiveSessions,
            totalPlayers = activePlayers,
            totalMoneyInPlay = totalMoneyInPlay,
        )
    }
}

/**
 * Value Object: Player Statistics
 * Immutable snapshot of a player's performance
 */
data class PlayerStats(
    val playerId: PlayerId,
    val totalSessions: Int,
    val totalBuyIn: Money,
    val totalCashOut: Money,
    val netProfit: Money,
    val roi: Double,
    val winRate: Double,
    val winningSessionsCount: Int,
    val losingSessionsCount: Int,
    val biggestWin: Money,
    val biggestLoss: Money,
    val averageSessionProfit: Money,
    val currentStreak: Int,
) {
    companion object {
        fun empty(playerId: PlayerId) =
            PlayerStats(
                playerId = playerId,
                totalSessions = 0,
                totalBuyIn = Money.ZERO,
                totalCashOut = Money.ZERO,
                netProfit = Money.ZERO,
                roi = 0.0,
                winRate = 0.0,
                winningSessionsCount = 0,
                losingSessionsCount = 0,
                biggestWin = Money.ZERO,
                biggestLoss = Money.ZERO,
                averageSessionProfit = Money.ZERO,
                currentStreak = 0,
            )
    }
}

/**
 * Value Object: System-wide Statistics
 */
data class SystemStats(
    val totalSessions: Int,
    val activeSessions: Int,
    val totalPlayers: Int,
    val totalMoneyInPlay: Money,
)
