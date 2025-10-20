package com.pokerstats.application.dto

import com.pokerstats.domain.model.user.UserRole
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * DTOs for authentication endpoints
 */

data class RegisterRequest(
    @field:Email(message = "Invalid email format")
    @field:NotBlank(message = "Email is required")
    val email: String,
    @field:NotBlank(message = "Password is required")
    @field:Size(min = 8, message = "Password must be at least 8 characters")
    val password: String,
    @field:NotBlank(message = "Name is required")
    @field:Size(max = 255, message = "Name cannot exceed 255 characters")
    val name: String,
)

data class LoginRequest(
    @field:Email(message = "Invalid email format")
    @field:NotBlank(message = "Email is required")
    val email: String,
    @field:NotBlank(message = "Password is required")
    val password: String,
)

data class RefreshTokenRequest(
    @field:NotBlank(message = "Refresh token is required")
    val refreshToken: String,
)

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: UserDto,
)

data class UserDto(
    val id: String,
    val email: String,
    val name: String,
    val role: UserRole,
    val avatarUrl: String?,
)
