package pl.kmazurek.infrastructure.api.rest.controller

import pl.kmazurek.application.dto.AuthResponse
import pl.kmazurek.application.dto.LoginRequest
import pl.kmazurek.application.dto.RefreshTokenRequest
import pl.kmazurek.application.dto.RegisterRequest
import pl.kmazurek.application.dto.UserDto
import pl.kmazurek.application.usecase.auth.LoginUser
import pl.kmazurek.application.usecase.auth.RefreshAccessToken
import pl.kmazurek.application.usecase.auth.RegisterUser
import pl.kmazurek.domain.model.user.User
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * REST Controller for authentication endpoints
 */
@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val registerUser: RegisterUser,
    private val loginUser: LoginUser,
    private val refreshAccessToken: RefreshAccessToken,
) {
    @PostMapping("/register")
    fun register(
        @Valid @RequestBody request: RegisterRequest,
    ): ResponseEntity<AuthResponse> {
        val user = registerUser.execute(request.email, request.password, request.name)

        // For simplicity, auto-login after registration
        val loginResult = loginUser.execute(request.email, request.password)

        val response =
            AuthResponse(
                accessToken = loginResult.accessToken,
                refreshToken = loginResult.refreshToken,
                user = user.toDto(),
            )

        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest,
    ): ResponseEntity<AuthResponse> {
        val result = loginUser.execute(request.email, request.password)

        val response =
            AuthResponse(
                accessToken = result.accessToken,
                refreshToken = result.refreshToken,
                user = result.user.toDto(),
            )

        return ResponseEntity.ok(response)
    }

    @PostMapping("/refresh")
    fun refresh(
        @Valid @RequestBody request: RefreshTokenRequest,
    ): ResponseEntity<Map<String, String>> {
        val newAccessToken = refreshAccessToken.execute(request.refreshToken)

        return ResponseEntity.ok(mapOf("accessToken" to newAccessToken))
    }

    @PostMapping("/logout")
    fun logout(): ResponseEntity<Map<String, String>> {
        // With JWT, logout is handled client-side by removing tokens
        // For now, just return success
        // TODO: Consider implementing token blacklist with Redis if needed
        return ResponseEntity.ok(mapOf("message" to "Logged out successfully"))
    }

    private fun User.toDto() =
        UserDto(
            id = id.toString(),
            email = email.value,
            name = name,
            role = role,
            avatarUrl = avatarUrl,
        )
}
