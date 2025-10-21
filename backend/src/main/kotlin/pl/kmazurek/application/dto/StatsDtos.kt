package pl.kmazurek.application.dto

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * DTOs for statistics endpoints (Phase 5)
 *
 * Time-series data point for profit over time
 */
data class ProfitDataPointDto(
    val date: LocalDate,
    val profitCents: Long,
    val cumulativeProfitCents: Long,
)

/**
 * Historical stats data
 */
data class HistoricalStatsDto(
    val profitOverTime: List<ProfitDataPointDto>,
)

/**
 * Performance by location
 */
data class LocationPerformanceDto(
    val location: String,
    val sessionsPlayed: Int,
    val totalProfitCents: Long,
    val avgProfitCents: Long,
    val winRate: Double,
)

/**
 * Performance by day of week (0 = Sunday, 6 = Saturday)
 */
data class DayOfWeekPerformanceDto(
    val dayOfWeek: Int,
    val dayName: String,
    val sessionsPlayed: Int,
    val totalProfitCents: Long,
    val avgProfitCents: Long,
)

/**
 * Best/worst session info
 */
data class NotableSessionDto(
    val sessionId: String,
    val date: LocalDateTime,
    val location: String,
    val gameType: String,
    val profitCents: Long,
)

/**
 * Complete stats response
 */
data class CompleteStatsDto(
    val overview: PlayerStatsDto,
    val profitOverTime: List<ProfitDataPointDto>,
    val locationPerformance: List<LocationPerformanceDto>,
    val dayOfWeekPerformance: List<DayOfWeekPerformanceDto>,
    val bestSessions: List<NotableSessionDto>,
    val worstSessions: List<NotableSessionDto>,
)

/**
 * Stats request with optional date range filter
 */
data class StatsFilterDto(
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
)
