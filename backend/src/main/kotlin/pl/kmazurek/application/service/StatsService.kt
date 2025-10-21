package pl.kmazurek.application.service

import org.springframework.stereotype.Service
import pl.kmazurek.application.dto.CompleteStatsDto
import pl.kmazurek.application.dto.DayOfWeekPerformanceDto
import pl.kmazurek.application.dto.LocationPerformanceDto
import pl.kmazurek.application.dto.NotableSessionDto
import pl.kmazurek.application.dto.PlayerStatsDto
import pl.kmazurek.application.dto.ProfitDataPointDto
import pl.kmazurek.domain.model.gamesession.SessionResult
import pl.kmazurek.domain.model.user.UserId
import pl.kmazurek.domain.repository.GameSessionRepository
import pl.kmazurek.domain.repository.PlayerRepository
import pl.kmazurek.domain.repository.SessionResultRepository
import pl.kmazurek.domain.service.StatsCalculator
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Application Service for advanced statistics
 * Phase 5: Statistics & Analytics
 */
@Service
class StatsService(
    private val playerRepository: PlayerRepository,
    private val sessionRepository: GameSessionRepository,
    private val resultRepository: SessionResultRepository,
    private val statsCalculator: StatsCalculator,
) {
    /**
     * Get complete statistics for a player with optional date filtering
     */
    fun getCompleteStats(
        userId: UserId,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
    ): CompleteStatsDto {
        val player =
            playerRepository.findByUserId(userId)
                ?: throw IllegalStateException("No player linked to user")

        // Get all results for player
        val allResults = resultRepository.findByPlayerId(player.id)

        // Apply date filtering if provided
        val filteredResults = filterResultsByDateRange(allResults, startDate, endDate)

        // Calculate overview stats
        val stats = statsCalculator.calculatePlayerStats(player.id, filteredResults)
        val overview = PlayerStatsDto.fromDomain(stats)

        // Get all sessions for additional analysis
        val sessions =
            filteredResults.map { result ->
                sessionRepository.findById(result.sessionId)
                    ?: throw IllegalStateException("Session not found: ${result.sessionId}")
            }

        // Calculate time-series data
        val profitOverTime = calculateProfitOverTime(filteredResults, sessions)

        // Calculate location performance
        val locationPerformance = calculateLocationPerformance(filteredResults, sessions)

        // Calculate day of week performance
        val dayOfWeekPerformance = calculateDayOfWeekPerformance(filteredResults, sessions)

        // Get best and worst sessions
        val sortedResults = filteredResults.sortedByDescending { it.profit().amountInCents }
        val bestSessions =
            sortedResults.take(5).map { result ->
                val session = sessionRepository.findById(result.sessionId)!!
                NotableSessionDto(
                    sessionId = session.id.toString(),
                    date = session.startTime,
                    location = session.location.value,
                    gameType = session.gameType.name,
                    profitCents = result.profit().amountInCents,
                )
            }

        val worstSessions =
            sortedResults.reversed().take(5).map { result ->
                val session = sessionRepository.findById(result.sessionId)!!
                NotableSessionDto(
                    sessionId = session.id.toString(),
                    date = session.startTime,
                    location = session.location.value,
                    gameType = session.gameType.name,
                    profitCents = result.profit().amountInCents,
                )
            }

        return CompleteStatsDto(
            overview = overview,
            profitOverTime = profitOverTime,
            locationPerformance = locationPerformance,
            dayOfWeekPerformance = dayOfWeekPerformance,
            bestSessions = bestSessions,
            worstSessions = worstSessions,
        )
    }

    /**
     * Filter results by date range
     */
    private fun filterResultsByDateRange(
        results: List<SessionResult>,
        startDate: LocalDate?,
        endDate: LocalDate?,
    ): List<SessionResult> {
        if (startDate == null && endDate == null) return results

        return results.filter { result ->
            val sessionDate = result.createdAt.toLocalDate()
            val afterStart = startDate == null || !sessionDate.isBefore(startDate)
            val beforeEnd = endDate == null || !sessionDate.isAfter(endDate)
            afterStart && beforeEnd
        }
    }

    /**
     * Calculate profit over time (time-series data)
     */
    private fun calculateProfitOverTime(
        results: List<SessionResult>,
        sessions: List<pl.kmazurek.domain.model.gamesession.GameSession>,
    ): List<ProfitDataPointDto> {
        if (results.isEmpty()) return emptyList()

        // Create map of sessionId to session for quick lookup
        val sessionMap = sessions.associateBy { it.id }

        // Sort results by session start time
        val sortedResults =
            results.sortedBy { result ->
                sessionMap[result.sessionId]?.startTime
            }

        var cumulativeProfit = 0L
        val dataPoints = mutableListOf<ProfitDataPointDto>()

        sortedResults.forEach { result ->
            val session = sessionMap[result.sessionId]
            if (session != null) {
                val profit = result.profit().amountInCents
                cumulativeProfit += profit

                dataPoints.add(
                    ProfitDataPointDto(
                        date = session.startTime.toLocalDate(),
                        profitCents = profit,
                        cumulativeProfitCents = cumulativeProfit,
                    ),
                )
            }
        }

        return dataPoints
    }

    /**
     * Calculate performance by location
     */
    private fun calculateLocationPerformance(
        results: List<SessionResult>,
        sessions: List<pl.kmazurek.domain.model.gamesession.GameSession>,
    ): List<LocationPerformanceDto> {
        if (results.isEmpty()) return emptyList()

        val sessionMap = sessions.associateBy { it.id }

        // Group results by location
        val resultsByLocation =
            results.groupBy { result ->
                sessionMap[result.sessionId]?.location?.value ?: "Unknown"
            }

        return resultsByLocation.map { (location, locationResults) ->
            val totalProfit = locationResults.sumOf { it.profit().amountInCents }
            val avgProfit = if (locationResults.isNotEmpty()) totalProfit / locationResults.size else 0
            val winningCount = locationResults.count { it.isWinning() }
            val winRate =
                if (locationResults.isNotEmpty()) {
                    (winningCount.toDouble() / locationResults.size) * 100.0
                } else {
                    0.0
                }

            LocationPerformanceDto(
                location = location,
                sessionsPlayed = locationResults.size,
                totalProfitCents = totalProfit,
                avgProfitCents = avgProfit,
                winRate = winRate,
            )
        }.sortedByDescending { it.totalProfitCents }
    }

    /**
     * Calculate performance by day of week
     */
    private fun calculateDayOfWeekPerformance(
        results: List<SessionResult>,
        sessions: List<pl.kmazurek.domain.model.gamesession.GameSession>,
    ): List<DayOfWeekPerformanceDto> {
        if (results.isEmpty()) return emptyList()

        val sessionMap = sessions.associateBy { it.id }

        // Group results by day of week
        val resultsByDay =
            results.groupBy { result ->
                sessionMap[result.sessionId]?.startTime?.dayOfWeek
            }

        return DayOfWeek.values().map { dayOfWeek ->
            val dayResults = resultsByDay[dayOfWeek] ?: emptyList()
            val totalProfit = dayResults.sumOf { it.profit().amountInCents }
            val avgProfit = if (dayResults.isNotEmpty()) totalProfit / dayResults.size else 0

            DayOfWeekPerformanceDto(
                dayOfWeek = dayOfWeek.value % 7, // Convert to 0=Sunday, 6=Saturday
                dayName = dayOfWeek.name,
                sessionsPlayed = dayResults.size,
                totalProfitCents = totalProfit,
                avgProfitCents = avgProfit,
            )
        }
    }
}
