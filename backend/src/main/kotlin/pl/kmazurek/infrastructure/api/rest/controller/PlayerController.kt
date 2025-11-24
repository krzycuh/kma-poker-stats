package pl.kmazurek.infrastructure.api.rest.controller

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import pl.kmazurek.application.dto.CreatePlayerRequest
import pl.kmazurek.application.dto.LinkPlayerRequest
import pl.kmazurek.application.dto.PlayerDto
import pl.kmazurek.application.dto.UpdatePlayerRequest
import pl.kmazurek.application.usecase.player.CreatePlayer
import pl.kmazurek.application.usecase.player.CreatePlayerCommand
import pl.kmazurek.application.usecase.player.DeletePlayer
import pl.kmazurek.application.usecase.player.GetPlayer
import pl.kmazurek.application.usecase.player.LinkPlayerToUser
import pl.kmazurek.application.usecase.player.ListPlayers
import pl.kmazurek.application.usecase.player.ListPlayersQuery
import pl.kmazurek.application.usecase.player.UnlinkPlayerFromUser
import pl.kmazurek.application.usecase.player.UpdatePlayer
import pl.kmazurek.application.usecase.player.UpdatePlayerCommand
import pl.kmazurek.domain.model.player.PlayerId
import pl.kmazurek.domain.model.user.UserId

/**
 * REST Controller for player management endpoints
 * All endpoints require ADMIN role
 */
@RestController
@RequestMapping("/api/players")
class PlayerController(
    private val createPlayer: CreatePlayer,
    private val updatePlayer: UpdatePlayer,
    private val deletePlayer: DeletePlayer,
    private val getPlayer: GetPlayer,
    private val listPlayers: ListPlayers,
    private val linkPlayerToUser: LinkPlayerToUser,
    private val unlinkPlayerFromUser: UnlinkPlayerFromUser,
) {
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun listPlayers(
        @RequestParam(required = false) searchTerm: String?,
        @RequestParam(required = false, defaultValue = "false") includeInactive: Boolean,
    ): ResponseEntity<List<PlayerDto>> {
        val query = ListPlayersQuery(searchTerm, includeInactive)
        val players = listPlayers.execute(query)
        val dtos = players.map { PlayerDto.fromDomain(it) }
        return ResponseEntity.ok(dtos)
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun getPlayer(
        @PathVariable id: String,
    ): ResponseEntity<PlayerDto> {
        val playerId = PlayerId.fromString(id)
        val player = getPlayer.execute(playerId)
        return ResponseEntity.ok(PlayerDto.fromDomain(player))
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun createPlayer(
        @Valid @RequestBody request: CreatePlayerRequest,
    ): ResponseEntity<PlayerDto> {
        val command =
            CreatePlayerCommand(
                name = request.name,
                avatarUrl = request.avatarUrl,
                userId = request.userId,
            )
        val player = createPlayer.execute(command)
        return ResponseEntity.status(HttpStatus.CREATED).body(PlayerDto.fromDomain(player))
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun updatePlayer(
        @PathVariable id: String,
        @Valid @RequestBody request: UpdatePlayerRequest,
    ): ResponseEntity<PlayerDto> {
        val playerId = PlayerId.fromString(id)
        val command =
            UpdatePlayerCommand(
                name = request.name,
                avatarUrl = request.avatarUrl,
            )
        val player = updatePlayer.execute(playerId, command)
        return ResponseEntity.ok(PlayerDto.fromDomain(player))
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun deletePlayer(
        @PathVariable id: String,
    ): ResponseEntity<Map<String, String>> {
        val playerId = PlayerId.fromString(id)
        deletePlayer.execute(playerId)
        return ResponseEntity.ok(mapOf("message" to "Player deactivated successfully"))
    }

    @PostMapping("/{id}/link")
    @PreAuthorize("hasRole('ADMIN')")
    fun linkPlayer(
        @PathVariable id: String,
        @Valid @RequestBody request: LinkPlayerRequest,
    ): ResponseEntity<PlayerDto> {
        val playerId = PlayerId.fromString(id)
        val userId = UserId.fromString(request.userId)
        val player = linkPlayerToUser.execute(playerId, userId)
        return ResponseEntity.ok(PlayerDto.fromDomain(player))
    }

    @DeleteMapping("/{id}/link")
    @PreAuthorize("hasRole('ADMIN')")
    fun unlinkPlayer(
        @PathVariable id: String,
    ): ResponseEntity<PlayerDto> {
        val playerId = PlayerId.fromString(id)
        val player = unlinkPlayerFromUser.execute(playerId)
        return ResponseEntity.ok(PlayerDto.fromDomain(player))
    }
}
