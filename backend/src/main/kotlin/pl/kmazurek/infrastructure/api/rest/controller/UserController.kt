package pl.kmazurek.infrastructure.api.rest.controller

import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pl.kmazurek.application.dto.ChangePasswordRequest
import pl.kmazurek.application.dto.UpdateProfileRequest
import pl.kmazurek.application.dto.UserDto
import pl.kmazurek.application.service.UserDtoMapper
import pl.kmazurek.application.usecase.user.ChangeUserPassword
import pl.kmazurek.application.usecase.user.GetCurrentUser
import pl.kmazurek.application.usecase.user.UpdateUserProfile
import pl.kmazurek.domain.model.user.UserId

/**
 * REST Controller for user profile endpoints
 */
@RestController
@RequestMapping("/api/users")
class UserController(
    private val getCurrentUser: GetCurrentUser,
    private val updateUserProfile: UpdateUserProfile,
    private val changeUserPassword: ChangeUserPassword,
    private val userDtoMapper: UserDtoMapper,
) {
    @GetMapping("/me")
    fun getCurrentUserProfile(
        @AuthenticationPrincipal userIdString: String,
    ): ResponseEntity<UserDto> {
        val userId = UserId.fromString(userIdString)
        val user = getCurrentUser.execute(userId)
        return ResponseEntity.ok(userDtoMapper.fromDomain(user))
    }

    @PutMapping("/me")
    fun updateProfile(
        @AuthenticationPrincipal userIdString: String,
        @Valid @RequestBody request: UpdateProfileRequest,
    ): ResponseEntity<UserDto> {
        val userId = UserId.fromString(userIdString)
        val user = updateUserProfile.execute(userId, request.name, request.avatarUrl)
        return ResponseEntity.ok(userDtoMapper.fromDomain(user))
    }

    @PatchMapping("/me/password")
    fun changePassword(
        @AuthenticationPrincipal userIdString: String,
        @Valid @RequestBody request: ChangePasswordRequest,
    ): ResponseEntity<Map<String, String>> {
        val userId = UserId.fromString(userIdString)
        changeUserPassword.execute(userId, request.currentPassword, request.newPassword)
        return ResponseEntity.ok(mapOf("message" to "Password changed successfully"))
    }
}
