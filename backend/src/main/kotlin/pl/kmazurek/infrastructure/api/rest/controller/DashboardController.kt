package pl.kmazurek.infrastructure.api.rest.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pl.kmazurek.application.dto.AdminDashboardDto
import pl.kmazurek.application.dto.CasualPlayerDashboardDto
import pl.kmazurek.application.service.DashboardService
import pl.kmazurek.domain.model.user.UserId

/**
 * REST Controller for dashboard endpoints
 */
@RestController
@RequestMapping("/api/dashboard")
class DashboardController(
    private val dashboardService: DashboardService,
) {
    /**
     * Get dashboard for casual player
     * GET /api/dashboard/player
     */
    @GetMapping("/player")
    @PreAuthorize("hasRole('CASUAL_PLAYER') or hasRole('ADMIN')")
    fun getCasualPlayerDashboard(
        @AuthenticationPrincipal userIdString: String,
    ): ResponseEntity<CasualPlayerDashboardDto> {
        val userId = UserId.fromString(userIdString)
        val dashboard = dashboardService.getCasualPlayerDashboard(userId)
        return ResponseEntity.ok(dashboard)
    }

    /**
     * Get dashboard for admin
     * GET /api/dashboard/admin
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    fun getAdminDashboard(
        @AuthenticationPrincipal userIdString: String,
    ): ResponseEntity<AdminDashboardDto> {
        val userId = UserId.fromString(userIdString)
        val dashboard = dashboardService.getAdminDashboard(userId)
        return ResponseEntity.ok(dashboard)
    }
}
