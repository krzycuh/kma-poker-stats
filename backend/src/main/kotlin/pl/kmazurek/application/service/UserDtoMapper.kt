package pl.kmazurek.application.service

import org.springframework.stereotype.Component
import pl.kmazurek.application.dto.UserDto
import pl.kmazurek.domain.model.user.User
import pl.kmazurek.domain.repository.PlayerRepository

@Component
class UserDtoMapper(
    private val playerRepository: PlayerRepository,
) {
    fun fromDomain(user: User): UserDto {
        val linkedPlayerId = playerRepository.findByUserId(user.id)?.id?.toString()
        return UserDto(
            id = user.id.toString(),
            email = user.email.value,
            name = user.name,
            role = user.role,
            avatarUrl = user.avatarUrl,
            linkedPlayerId = linkedPlayerId,
        )
    }
}
