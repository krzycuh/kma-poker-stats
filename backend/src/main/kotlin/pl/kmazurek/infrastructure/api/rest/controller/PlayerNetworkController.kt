package pl.kmazurek.infrastructure.api.rest.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import pl.kmazurek.application.dto.SharedPlayerSummaryDto
import pl.kmazurek.application.service.PlayerNetworkService
import pl.kmazurek.domain.model.user.UserId

@RestController
@RequestMapping("/api/player-network")
class PlayerNetworkController(
    private val playerNetworkService: PlayerNetworkService,
) {
    @GetMapping("/players")
    @PreAuthorize("hasRole('CASUAL_PLAYER') or hasRole('ADMIN')")
    fun listSharedPlayers(
        @AuthenticationPrincipal userIdString: String,
        @RequestParam(required = false) searchTerm: String?,
        @RequestParam(required = false) limit: Int?,
    ): ResponseEntity<List<SharedPlayerSummaryDto>> {
        val userId = UserId.fromString(userIdString)
        val players = playerNetworkService.searchVisiblePlayers(userId, searchTerm, limit)
        return ResponseEntity.ok(players)
    }
}
