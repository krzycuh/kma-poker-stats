package pl.kmazurek.application.dto

/**
 * DTOs for leaderboard endpoints (Phase 5)
 *
 * Leaderboard entry
 */
data class LeaderboardEntryDto(
    val rank: Int,
    val playerId: String,
    val playerName: String,
    val value: Double,
    val valueFormatted: String,
    val sessionsPlayed: Int,
    val isCurrentUser: Boolean = false,
)

/**
 * Complete leaderboard response
 */
data class LeaderboardDto(
    val metric: LeaderboardMetric,
    val entries: List<LeaderboardEntryDto>,
    val currentUserEntry: LeaderboardEntryDto?,
    val totalEntries: Int,
)

/**
 * Leaderboard metric types
 */
enum class LeaderboardMetric {
    NET_PROFIT,
    ROI,
    WIN_RATE,
    CURRENT_STREAK,
    TOTAL_SESSIONS,
    AVERAGE_PROFIT,
}
