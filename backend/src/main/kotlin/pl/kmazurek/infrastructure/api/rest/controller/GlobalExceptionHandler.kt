package pl.kmazurek.infrastructure.api.rest.controller

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import pl.kmazurek.application.usecase.auth.EmailAlreadyExistsException
import pl.kmazurek.application.usecase.auth.InvalidCredentialsException
import pl.kmazurek.application.usecase.auth.InvalidRefreshTokenException
import pl.kmazurek.application.usecase.gamesession.GameSessionNotFoundException
import pl.kmazurek.application.usecase.player.LinkedUserNotFoundException
import pl.kmazurek.application.usecase.player.PlayerAlreadyExistsException
import pl.kmazurek.application.usecase.player.PlayerAlreadyLinkedException
import pl.kmazurek.application.usecase.player.PlayerInactiveException
import pl.kmazurek.application.usecase.player.PlayerNotLinkedException
import pl.kmazurek.application.usecase.player.PlayerNotFoundException
import pl.kmazurek.application.usecase.player.UserAlreadyLinkedException
import pl.kmazurek.application.usecase.sessionresult.SessionResultNotFoundException
import pl.kmazurek.application.usecase.user.InvalidPasswordException
import pl.kmazurek.application.usecase.user.UserNotFoundException

/**
 * Global exception handler for REST controllers
 */
@RestControllerAdvice
class GlobalExceptionHandler {
    companion object {
        private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    }

    @ExceptionHandler(EmailAlreadyExistsException::class)
    fun handleEmailAlreadyExists(ex: EmailAlreadyExistsException): ResponseEntity<ErrorResponse> {
        return buildErrorResponse(HttpStatus.CONFLICT, "Email already exists", ex)
    }

    @ExceptionHandler(InvalidCredentialsException::class)
    fun handleInvalidCredentials(ex: InvalidCredentialsException): ResponseEntity<ErrorResponse> {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Invalid credentials", ex)
    }

    @ExceptionHandler(InvalidRefreshTokenException::class)
    fun handleInvalidRefreshToken(ex: InvalidRefreshTokenException): ResponseEntity<ErrorResponse> {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Invalid refresh token", ex)
    }

    @ExceptionHandler(UserNotFoundException::class, LinkedUserNotFoundException::class)
    fun handleUserNotFound(ex: Exception): ResponseEntity<ErrorResponse> {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "User not found", ex)
    }

    @ExceptionHandler(PlayerAlreadyExistsException::class)
    fun handlePlayerAlreadyExists(ex: PlayerAlreadyExistsException): ResponseEntity<ErrorResponse> {
        return buildErrorResponse(HttpStatus.CONFLICT, "Player already exists", ex)
    }

    @ExceptionHandler(PlayerNotFoundException::class)
    fun handlePlayerNotFound(ex: PlayerNotFoundException): ResponseEntity<ErrorResponse> {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Player not found", ex)
    }

    @ExceptionHandler(PlayerAlreadyLinkedException::class)
    fun handlePlayerAlreadyLinked(ex: PlayerAlreadyLinkedException): ResponseEntity<ErrorResponse> {
        return buildErrorResponse(HttpStatus.CONFLICT, "Player already linked to a user", ex)
    }

    @ExceptionHandler(PlayerNotLinkedException::class)
    fun handlePlayerNotLinked(ex: PlayerNotLinkedException): ResponseEntity<ErrorResponse> {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Player not linked to any user", ex)
    }

    @ExceptionHandler(PlayerInactiveException::class)
    fun handlePlayerInactive(ex: PlayerInactiveException): ResponseEntity<ErrorResponse> {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Cannot link inactive player", ex)
    }

    @ExceptionHandler(UserAlreadyLinkedException::class)
    fun handleUserAlreadyLinked(ex: UserAlreadyLinkedException): ResponseEntity<ErrorResponse> {
        return buildErrorResponse(HttpStatus.CONFLICT, "User already linked to another player", ex)
    }

    @ExceptionHandler(GameSessionNotFoundException::class)
    fun handleGameSessionNotFound(ex: GameSessionNotFoundException): ResponseEntity<ErrorResponse> {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Game session not found", ex)
    }

    @ExceptionHandler(SessionResultNotFoundException::class)
    fun handleSessionResultNotFound(ex: SessionResultNotFoundException): ResponseEntity<ErrorResponse> {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Session result not found", ex)
    }

    @ExceptionHandler(InvalidPasswordException::class)
    fun handleInvalidPassword(ex: InvalidPasswordException): ResponseEntity<ErrorResponse> {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Invalid password", ex)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationErrors(ex: MethodArgumentNotValidException): ResponseEntity<ValidationErrorResponse> {
        val errors =
            ex.bindingResult.fieldErrors.associate {
                it.field to (it.defaultMessage ?: "Invalid value")
            }

        logException(HttpStatus.BAD_REQUEST, ex)

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ValidationErrorResponse("Validation failed", errors))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Invalid request", ex)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ErrorResponse> {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", ex)
    }

    private fun buildErrorResponse(
        status: HttpStatus,
        fallbackMessage: String,
        ex: Exception,
    ): ResponseEntity<ErrorResponse> {
        logException(status, ex)
        // For server errors (5xx), never expose exception messages to clients for security
        val clientMessage =
            if (status.is5xxServerError) {
                fallbackMessage
            } else {
                ex.message ?: fallbackMessage
            }
        return ResponseEntity
            .status(status)
            .body(ErrorResponse(clientMessage))
    }

    private fun logException(
        status: HttpStatus,
        ex: Exception,
    ) {
        val message = ex.message ?: status.reasonPhrase
        val exceptionName = ex::class.simpleName ?: "UnknownException"

        if (status.is5xxServerError) {
            logger.error(
                "HTTP {} ({}): {} - {}",
                status.value(),
                status.reasonPhrase,
                exceptionName,
                message,
                ex,
            )
        } else {
            logger.warn(
                "HTTP {} ({}): {} - {}",
                status.value(),
                status.reasonPhrase,
                exceptionName,
                message,
            )
        }
    }
}

data class ErrorResponse(val message: String)

data class ValidationErrorResponse(val message: String, val errors: Map<String, String>)
