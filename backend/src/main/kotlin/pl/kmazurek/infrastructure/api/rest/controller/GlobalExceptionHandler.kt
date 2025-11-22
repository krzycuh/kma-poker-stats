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
import pl.kmazurek.application.usecase.player.PlayerAlreadyExistsException
import pl.kmazurek.application.usecase.player.PlayerNotFoundException
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

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFound(ex: UserNotFoundException): ResponseEntity<ErrorResponse> {
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
        return ResponseEntity
            .status(status)
            .body(ErrorResponse(ex.message ?: fallbackMessage))
    }

    private fun logException(
        status: HttpStatus,
        ex: Exception,
    ) {
        val message = ex.message ?: status.reasonPhrase
        val exceptionName = ex::class.simpleName ?: "UnknownException"

        if (status.is5xxServerError) {
            logger.error(
                "Responding with {} ({}) due to {}: {}",
                status.value(),
                status.reasonPhrase,
                exceptionName,
                message,
                ex,
            )
        } else {
            logger.warn(
                "Handled {} with {} ({}): {}",
                exceptionName,
                status.value(),
                status.reasonPhrase,
                message,
            )
        }
    }
}

data class ErrorResponse(val message: String)

data class ValidationErrorResponse(val message: String, val errors: Map<String, String>)
