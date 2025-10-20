package pl.kmazurek.infrastructure.api.rest.controller

import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pl.kmazurek.application.dto.SessionResultDto
import pl.kmazurek.application.dto.UpdateSessionResultRequest
import pl.kmazurek.application.usecase.sessionresult.DeleteSessionResult
import pl.kmazurek.application.usecase.sessionresult.UpdateSessionResult
import pl.kmazurek.application.usecase.sessionresult.UpdateSessionResultCommand
import pl.kmazurek.domain.model.gamesession.SessionResultId

/**
 * REST Controller for session result endpoints
 */
@RestController
@RequestMapping("/api/results")
class SessionResultController(
    private val updateSessionResult: UpdateSessionResult,
    private val deleteSessionResult: DeleteSessionResult,
) {
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateResult(
        @PathVariable id: String,
        @Valid @RequestBody request: UpdateSessionResultRequest,
    ): ResponseEntity<SessionResultDto> {
        val resultId = SessionResultId.fromString(id)
        val command =
            UpdateSessionResultCommand(
                buyInCents = request.buyInCents,
                cashOutCents = request.cashOutCents,
                notes = request.notes,
            )
        val result = updateSessionResult.execute(resultId, command)
        return ResponseEntity.ok(SessionResultDto.fromDomain(result))
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteResult(
        @PathVariable id: String,
    ): ResponseEntity<Map<String, String>> {
        val resultId = SessionResultId.fromString(id)
        deleteSessionResult.execute(resultId)
        return ResponseEntity.ok(mapOf("message" to "Result deleted successfully"))
    }
}
