package pl.kmazurek.infrastructure.api.rest.controller

import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import pl.kmazurek.application.dto.CreateGameSessionRequest
import pl.kmazurek.application.dto.GameSessionDto
import pl.kmazurek.application.dto.GameSessionWithResultsDto
import pl.kmazurek.application.dto.SessionResultDto
import pl.kmazurek.application.dto.UpdateGameSessionRequest
import pl.kmazurek.application.usecase.gamesession.CreateGameSession
import pl.kmazurek.application.usecase.gamesession.CreateGameSessionCommand
import pl.kmazurek.application.usecase.gamesession.CreateSessionResultCommand
import pl.kmazurek.application.usecase.gamesession.DeleteGameSession
import pl.kmazurek.application.usecase.gamesession.GetGameSession
import pl.kmazurek.application.usecase.gamesession.ListGameSessions
import pl.kmazurek.application.usecase.gamesession.ListGameSessionsQuery
import pl.kmazurek.application.usecase.gamesession.UpdateGameSession
import pl.kmazurek.application.usecase.gamesession.UpdateGameSessionCommand
import pl.kmazurek.domain.model.gamesession.GameSessionId
import pl.kmazurek.domain.model.user.UserId
import java.time.LocalDateTime

/**
 * REST Controller for game session endpoints
 */
@RestController
@RequestMapping("/api/sessions")
class GameSessionController(
    private val createGameSession: CreateGameSession,
    private val updateGameSession: UpdateGameSession,
    private val deleteGameSession: DeleteGameSession,
    private val getGameSession: GetGameSession,
    private val listGameSessions: ListGameSessions,
) {
    @GetMapping
    fun listSessions(
        @AuthenticationPrincipal userIdString: String?,
        @RequestParam(required = false) location: String?,
        @RequestParam(required = false) gameType: String?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startDate: LocalDateTime?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) endDate: LocalDateTime?,
        @RequestParam(required = false, defaultValue = "false") includeDeleted: Boolean,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "50") pageSize: Int,
        authentication: Authentication,
    ): ResponseEntity<List<GameSessionDto>> {
        val isAdmin = authentication.isAdmin()
        val filterByUserId = if (isAdmin) null else userIdString

        val query =
            ListGameSessionsQuery(
                userId = filterByUserId,
                location = location,
                gameType = gameType,
                startDate = startDate,
                endDate = endDate,
                includeDeleted = includeDeleted,
            )
        val allSessions = listGameSessions.execute(query)

        // Apply pagination
        val startIndex = page * pageSize
        val endIndex = minOf(startIndex + pageSize, allSessions.size)

        val paginatedSessions =
            if (startIndex < allSessions.size) {
                allSessions.subList(startIndex, endIndex)
            } else {
                emptyList()
            }

        val dtos = paginatedSessions.map { GameSessionDto.fromDomain(it) }
        return ResponseEntity.ok(dtos)
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CASUAL_PLAYER') or hasRole('ADMIN')")
    fun getSession(
        @PathVariable id: String,
        @AuthenticationPrincipal userIdString: String?,
        authentication: Authentication,
    ): ResponseEntity<GameSessionWithResultsDto> {
        val sessionId = GameSessionId.fromString(id)
        val isAdmin = authentication.isAdmin()
        val participantUserId =
            if (isAdmin) {
                null
            } else {
                userIdString?.let { UserId.fromString(it) }
                    ?: throw AccessDeniedException("User context is missing")
            }

        val result = getGameSession.execute(sessionId, participantUserId)

        val dto =
            GameSessionWithResultsDto(
                session = GameSessionDto.fromDomain(result.session),
                results =
                    result.results.map { sessionResult ->
                        val player = result.playersById[sessionResult.playerId]
                        SessionResultDto.fromDomain(
                            sessionResult,
                            playerName = player?.name?.value,
                            playerAvatarUrl = player?.avatarUrl,
                            linkedUserId = player?.userId?.toString(),
                        )
                    },
            )
        return ResponseEntity.ok(dto)
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun createSession(
        @AuthenticationPrincipal userIdString: String,
        @Valid @RequestBody request: CreateGameSessionRequest,
    ): ResponseEntity<GameSessionWithResultsDto> {
        val command =
            CreateGameSessionCommand(
                startTime = request.startTime,
                endTime = request.endTime,
                location = request.location,
                gameType = request.gameType,
                minBuyInCents = request.minBuyInCents,
                notes = request.notes,
                createdByUserId = userIdString,
                results =
                    request.results.map {
                        CreateSessionResultCommand(
                            playerId = it.playerId,
                            buyInCents = it.buyInCents,
                            cashOutCents = it.cashOutCents,
                            notes = it.notes,
                        )
                    },
            )
        val result = createGameSession.execute(command)
        val dto =
            GameSessionWithResultsDto(
                session = GameSessionDto.fromDomain(result.session),
                results = result.results.map { SessionResultDto.fromDomain(it) },
            )
        return ResponseEntity.status(HttpStatus.CREATED).body(dto)
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateSession(
        @PathVariable id: String,
        @Valid @RequestBody request: UpdateGameSessionRequest,
    ): ResponseEntity<GameSessionDto> {
        val sessionId = GameSessionId.fromString(id)
        val command =
            UpdateGameSessionCommand(
                startTime = request.startTime,
                endTime = request.endTime,
                location = request.location,
                gameType = request.gameType,
                minBuyInCents = request.minBuyInCents,
                notes = request.notes,
            )
        val session = updateGameSession.execute(sessionId, command)
        return ResponseEntity.ok(GameSessionDto.fromDomain(session))
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteSession(
        @PathVariable id: String,
    ): ResponseEntity<Map<String, String>> {
        val sessionId = GameSessionId.fromString(id)
        deleteGameSession.execute(sessionId)
        return ResponseEntity.ok(mapOf("message" to "Session deleted successfully"))
    }
}

private fun Authentication.isAdmin(): Boolean {
    return authorities.any { it.authority == "ROLE_ADMIN" }
}
