package pl.kmazurek.infrastructure.api.rest.controller

import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import pl.kmazurek.application.dto.CompleteStatsDto
import pl.kmazurek.application.service.StatsService
import pl.kmazurek.domain.model.user.UserId
import java.time.LocalDate

/**
 * REST Controller for statistics endpoints
 * Phase 5: Statistics & Analytics
 */
@RestController
@RequestMapping("/api/stats")
class StatsController(
    private val statsService: StatsService,
) {
    /**
     * Get complete statistics for current user
     * GET /api/stats/personal
     *
     * Optional query parameters:
     * - startDate: Filter sessions from this date (inclusive)
     * - endDate: Filter sessions until this date (inclusive)
     */
    @GetMapping("/personal")
    @PreAuthorize("hasRole('CASUAL_PLAYER') or hasRole('ADMIN')")
    fun getPersonalStats(
        @AuthenticationPrincipal userIdString: String,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        startDate: LocalDate?,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        endDate: LocalDate?,
    ): ResponseEntity<CompleteStatsDto> {
        val userId = UserId.fromString(userIdString)
        val stats = statsService.getCompleteStats(userId, startDate, endDate)
        return ResponseEntity.ok(stats)
    }
}
