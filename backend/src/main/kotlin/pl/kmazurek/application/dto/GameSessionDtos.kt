package pl.kmazurek.application.dto

import pl.kmazurek.domain.model.gamesession.ExpenseSplit
import pl.kmazurek.domain.model.gamesession.GameSession
import pl.kmazurek.domain.model.gamesession.SessionExpense
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
    val placement: Int?,
    val notes: String?,
    val isSpectator: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val playerName: String? = null,
    val playerAvatarUrl: String? = null,
    val linkedUserId: String? = null,
) {
    companion object {
        fun fromDomain(
            result: SessionResult,
            playerName: String? = null,
            playerAvatarUrl: String? = null,
            linkedUserId: String? = null,
        ): SessionResultDto {
            return SessionResultDto(
                id = result.id.toString(),
                sessionId = result.sessionId.toString(),
                playerId = result.playerId.toString(),
                buyInCents = result.buyIn.amountInCents,
                cashOutCents = result.cashOut.amountInCents,
                profitCents = result.profit().amountInCents,
                placement = result.placement,
                notes = result.notes,
                isSpectator = result.isSpectator,
                createdAt = result.createdAt,
                updatedAt = result.updatedAt,
                playerName = playerName,
                playerAvatarUrl = playerAvatarUrl,
                linkedUserId = linkedUserId,
            )
        }
    }
}

data class GameSessionWithResultsDto(
    val session: GameSessionDto,
    val results: List<SessionResultDto>,
    val expenses: List<SessionExpenseDto> = emptyList(),
    val expenseSplit: ExpenseSplitDto? = null,
)

data class SessionExpenseDto(
    val id: String,
    val sessionId: String,
    val payerPlayerId: String,
    val payerPlayerName: String? = null,
    val description: String,
    val amountCents: Long,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun fromDomain(
            expense: SessionExpense,
            payerPlayerName: String? = null,
        ): SessionExpenseDto {
            return SessionExpenseDto(
                id = expense.id.toString(),
                sessionId = expense.sessionId.toString(),
                payerPlayerId = expense.payerPlayerId.toString(),
                payerPlayerName = payerPlayerName,
                description = expense.description,
                amountCents = expense.amount.amountInCents,
                createdAt = expense.createdAt,
                updatedAt = expense.updatedAt,
            )
        }
    }
}

data class ExpenseSplitDto(
    val totalExpensesCents: Long,
    val perPlayerShareCents: Long,
    val playerCount: Int,
    val playerBalances: List<PlayerBalanceDto>,
) {
    companion object {
        fun fromDomain(
            split: ExpenseSplit,
            playerNames: Map<String, String>,
        ): ExpenseSplitDto {
            return ExpenseSplitDto(
                totalExpensesCents = split.totalExpenses.amountInCents,
                perPlayerShareCents = split.perPlayerShare.amountInCents,
                playerCount = split.playerCount,
                playerBalances =
                    split.playerBalances.values.map { balance ->
                        PlayerBalanceDto(
                            playerId = balance.playerId.toString(),
                            playerName = playerNames[balance.playerId.toString()],
                            paidCents = balance.amountPaid.amountInCents,
                            owedCents = balance.amountOwed.amountInCents,
                            balanceCents = balance.balance.amountInCents,
                        )
                    },
            )
        }
    }
}

data class PlayerBalanceDto(
    val playerId: String,
    val playerName: String?,
    val paidCents: Long,
    val owedCents: Long,
    val balanceCents: Long,
)

data class CreateGameSessionRequest(
    val startTime: LocalDateTime,
    val endTime: LocalDateTime? = null,
    val location: String,
    val gameType: String,
    val minBuyInCents: Long,
    val notes: String? = null,
    val results: List<CreateSessionResultRequest>,
    val expenses: List<CreateSessionExpenseRequest>? = null,
)

data class CreateSessionResultRequest(
    val playerId: String,
    val buyInCents: Long,
    val cashOutCents: Long,
    val notes: String? = null,
    val isSpectator: Boolean = false,
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
    val isSpectator: Boolean = false,
)

data class CreateSessionExpenseRequest(
    val payerPlayerId: String,
    val description: String,
    val amountCents: Long,
)

data class UpdateSessionExpenseRequest(
    val payerPlayerId: String,
    val description: String,
    val amountCents: Long,
)
