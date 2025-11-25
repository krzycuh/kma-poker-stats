package pl.kmazurek.application.service

import io.mockk.every
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import pl.kmazurek.application.usecase.player.PlayerAccessDeniedException
import pl.kmazurek.domain.model.gamesession.GameSessionId
import pl.kmazurek.domain.model.gamesession.SessionResult
import pl.kmazurek.domain.model.gamesession.SessionResultId
import pl.kmazurek.domain.model.player.Player
import pl.kmazurek.domain.model.player.PlayerId
import pl.kmazurek.domain.model.player.PlayerName
import pl.kmazurek.domain.model.shared.Money
import pl.kmazurek.domain.model.user.UserId
import pl.kmazurek.domain.repository.PlayerRepository
import pl.kmazurek.domain.repository.SessionResultRepository
import java.time.LocalDateTime

class PlayerNetworkServiceTest {
    private val playerRepository = mockk<PlayerRepository>()
    private val sessionResultRepository = mockk<SessionResultRepository>()
    private val service = PlayerNetworkService(playerRepository, sessionResultRepository)

    @Test
    fun `searchVisiblePlayers returns connected players sorted by shared sessions`() {
        val viewerUserId = UserId.generate()
        val viewerPlayer = player(name = "Viewer", id = PlayerId.generate(), userId = viewerUserId)
        val otherPlayerOne = player(name = "Charlie", id = PlayerId.generate())
        val otherPlayerTwo = player(name = "Alice", id = PlayerId.generate())

        val sessionOne = GameSessionId.generate()
        val sessionTwo = GameSessionId.generate()

        every { playerRepository.findByUserId(viewerUserId) } returns viewerPlayer
        every { sessionResultRepository.findByPlayerId(viewerPlayer.id) } returns listOf(
            sessionResult(sessionOne, viewerPlayer.id, createdAt = LocalDateTime.now().minusDays(2)),
            sessionResult(sessionTwo, viewerPlayer.id, createdAt = LocalDateTime.now().minusDays(1)),
        )

        every { sessionResultRepository.findBySessionIds(any()) } returns listOf(
            sessionResult(sessionOne, otherPlayerOne.id, createdAt = LocalDateTime.now().minusDays(2)),
            sessionResult(sessionTwo, otherPlayerOne.id, createdAt = LocalDateTime.now().minusDays(1)),
            sessionResult(sessionTwo, otherPlayerTwo.id, createdAt = LocalDateTime.now().minusDays(1)),
        )

        every { playerRepository.findByIds(any()) } returns listOf(
            otherPlayerOne,
            otherPlayerTwo,
        )

        val result = service.searchVisiblePlayers(viewerUserId, null, null)

        assertEquals(2, result.size)
        assertEquals(otherPlayerOne.id.toString(), result.first().playerId)
        assertEquals(2, result.first().sharedSessionsCount)
        assertEquals(otherPlayerTwo.id.toString(), result[1].playerId)
        assertEquals(1, result[1].sharedSessionsCount)
    }

    @Test
    fun `sharedSessionIdsBetween throws when there is no overlap`() {
        val viewerUserId = UserId.generate()
        val viewerPlayer = player(name = "Viewer", id = PlayerId.generate(), userId = viewerUserId)
        val targetPlayer = player(name = "Rival", id = PlayerId.generate())

        every { playerRepository.findByUserId(viewerUserId) } returns viewerPlayer
        every { sessionResultRepository.findByPlayerId(viewerPlayer.id) } returns listOf(
            sessionResult(GameSessionId.generate(), viewerPlayer.id, createdAt = LocalDateTime.now()),
        )
        every { sessionResultRepository.findByPlayerId(targetPlayer.id) } returns emptyList()
        every { playerRepository.findById(targetPlayer.id) } returns targetPlayer

        assertThrows(PlayerAccessDeniedException::class.java) {
            service.sharedSessionIdsBetween(viewerUserId, targetPlayer.id)
        }
    }

    private fun player(
        name: String,
        id: PlayerId,
        userId: UserId? = null,
    ): Player =
        Player(
            id = id,
            name = PlayerName(name),
            avatarUrl = null,
            userId = userId,
            isActive = true,
            createdAt = LocalDateTime.now().minusDays(10),
            updatedAt = LocalDateTime.now().minusDays(1),
        )

    private fun sessionResult(
        sessionId: GameSessionId,
        playerId: PlayerId,
        createdAt: LocalDateTime,
    ): SessionResult =
        SessionResult(
            id = SessionResultId.generate(),
            sessionId = sessionId,
            playerId = playerId,
            buyIn = Money.ofCents(1_000),
            cashOut = Money.ofCents(1_500),
            notes = null,
            createdAt = createdAt,
            updatedAt = createdAt,
        )
}
