package pl.kmazurek.application.dto

import jakarta.validation.constraints.NotBlank
import pl.kmazurek.domain.model.player.Player
import java.time.LocalDateTime

data class PlayerDto(
    val id: String,
    val name: String,
    val avatarUrl: String?,
    val userId: String?,
    val isActive: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun fromDomain(player: Player): PlayerDto {
            return PlayerDto(
                id = player.id.toString(),
                name = player.name.value,
                avatarUrl = player.avatarUrl,
                userId = player.userId?.toString(),
                isActive = player.isActive,
                createdAt = player.createdAt,
                updatedAt = player.updatedAt,
            )
        }
    }
}

data class CreatePlayerRequest(
    val name: String,
    val avatarUrl: String? = null,
    val userId: String? = null,
)

data class UpdatePlayerRequest(
    val name: String,
    val avatarUrl: String? = null,
)

data class LinkPlayerRequest(
    @field:NotBlank(message = "User ID is required")
    val userId: String,
)
