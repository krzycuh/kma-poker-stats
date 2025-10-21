package pl.kmazurek.infrastructure.api.rest.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import pl.kmazurek.application.dto.LeaderboardDto
import pl.kmazurek.application.dto.LeaderboardMetric
import pl.kmazurek.application.service.LeaderboardService
import pl.kmazurek.domain.model.user.UserId

/**
 * REST Controller for leaderboard endpoints
 * Phase 5: Statistics & Analytics
 */
@RestController
@RequestMapping("/api/leaderboards")
class LeaderboardController(
    private val leaderboardService: LeaderboardService,
) {
    /**
     * Get leaderboard for a specific metric
     * GET /api/leaderboards
     *
     * Query parameters:
     * - metric: Leaderboard metric (NET_PROFIT, ROI, WIN_RATE, CURRENT_STREAK, TOTAL_SESSIONS, AVERAGE_PROFIT)
     * - limit: Maximum number of entries to return (default: 50)
     */
    @GetMapping
    @PreAuthorize("hasRole('CASUAL_PLAYER') or hasRole('ADMIN')")
    fun getLeaderboard(
        @AuthenticationPrincipal userIdString: String,
        @RequestParam(defaultValue = "NET_PROFIT") metric: LeaderboardMetric,
        @RequestParam(defaultValue = "50") limit: Int,
        @RequestParam(defaultValue = "0") page: Int,
    ): ResponseEntity<LeaderboardDto> {
        val userId = UserId.fromString(userIdString)
        val leaderboard = leaderboardService.getLeaderboard(metric, userId, limit)

        // Add pagination info as headers
        return ResponseEntity
            .ok()
            .header("X-Page", page.toString())
            .header("X-Page-Size", limit.toString())
            .header("X-Total-Entries", leaderboard.totalEntries.toString())
            .body(leaderboard)
    }
}
