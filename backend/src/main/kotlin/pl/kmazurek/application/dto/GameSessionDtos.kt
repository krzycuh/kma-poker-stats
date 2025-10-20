package pl.kmazurek.application.dto

import pl.kmazurek.domain.model.gamesession.GameSession
import pl.kmazurek.domain.model.gamesession.SessionResult
import java.time.LocalDateTime

data class GameSessionDto(
    val id: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime?,
    val location: String,
    val gameType: String,
    val minBuyInCents: Long,
    val notes: String?,
    val createdByUserId: String?,
    val isDeleted: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun fromDomain(session: GameSession): GameSessionDto {
            return GameSessionDto(
                id = session.id.toString(),
                startTime = session.startTime,
                endTime = session.endTime,
                location = session.location.value,
                gameType = session.gameType.name,
                minBuyInCents = session.minBuyIn.amountInCents,
                notes = session.notes,
                createdByUserId = session.createdByUserId?.toString(),
                isDeleted = session.isDeleted,
                createdAt = session.createdAt,
                updatedAt = session.updatedAt,
            )
        }
    }
}

data class SessionResultDto(
    val id: String,
    val sessionId: String,
    val playerId: String,
    val buyInCents: Long,
    val cashOutCents: Long,
    val profitCents: Long,
    val notes: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun fromDomain(result: SessionResult): SessionResultDto {
            return SessionResultDto(
                id = result.id.toString(),
                sessionId = result.sessionId.toString(),
                playerId = result.playerId.toString(),
                buyInCents = result.buyIn.amountInCents,
                cashOutCents = result.cashOut.amountInCents,
                profitCents = result.profit().amountInCents,
                notes = result.notes,
                createdAt = result.createdAt,
                updatedAt = result.updatedAt,
            )
        }
    }
}

data class GameSessionWithResultsDto(
    val session: GameSessionDto,
    val results: List<SessionResultDto>,
)

data class CreateGameSessionRequest(
    val startTime: LocalDateTime,
    val endTime: LocalDateTime? = null,
    val location: String,
    val gameType: String,
    val minBuyInCents: Long,
    val notes: String? = null,
    val results: List<CreateSessionResultRequest>,
)

data class CreateSessionResultRequest(
    val playerId: String,
    val buyInCents: Long,
    val cashOutCents: Long,
    val notes: String? = null,
)

data class UpdateGameSessionRequest(
    val startTime: LocalDateTime,
    val endTime: LocalDateTime? = null,
    val location: String,
    val gameType: String,
    val minBuyInCents: Long,
    val notes: String? = null,
)

data class UpdateSessionResultRequest(
    val buyInCents: Long,
    val cashOutCents: Long,
    val notes: String? = null,
)
