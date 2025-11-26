package pl.kmazurek.application.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import pl.kmazurek.application.dto.LeaderboardMetric
import pl.kmazurek.domain.model.gamesession.GameSessionId
import pl.kmazurek.domain.model.gamesession.SessionResult
import pl.kmazurek.domain.model.gamesession.SessionResultId
import pl.kmazurek.domain.model.player.Player
import pl.kmazurek.domain.model.player.PlayerId
import pl.kmazurek.domain.model.player.PlayerName
import pl.kmazurek.domain.model.shared.Money
import pl.kmazurek.domain.model.user.Email
import pl.kmazurek.domain.model.user.HashedPassword
import pl.kmazurek.domain.model.user.User
import pl.kmazurek.domain.model.user.UserId
import pl.kmazurek.domain.model.user.UserRole
import pl.kmazurek.domain.repository.PlayerRepository
import pl.kmazurek.domain.repository.SessionResultRepository
import pl.kmazurek.domain.repository.UserRepository
import pl.kmazurek.domain.service.StatsCalculator
import java.time.LocalDateTime

class LeaderboardServiceTest {
    private lateinit var playerRepository: PlayerRepository
    private lateinit var sessionResultRepository: SessionResultRepository
    private lateinit var userRepository: UserRepository
    private lateinit var service: LeaderboardService

    private val statsCalculator = StatsCalculator()

    @BeforeEach
    fun setup() {
        playerRepository = mockk()
        sessionResultRepository = mockk()
        userRepository = mockk()
        service = LeaderboardService(playerRepository, sessionResultRepository, statsCalculator, userRepository)
    }

    @Test
    fun `casual player sees only shared sessions`() {
        val userId = UserId.generate()
        val viewerPlayer = player("Viewer", userId)
        val rivalPlayer = player("Rival")
        val sharedSession = GameSessionId.generate()

        val viewerResult = sessionResult(sharedSession, viewerPlayer.id, buyIn = 2_000, cashOut = 3_000)
        val rivalSharedResult = sessionResult(sharedSession, rivalPlayer.id, buyIn = 1_000, cashOut = 500)

        every { userRepository.findById(userId) } returns user(userId, UserRole.CASUAL_PLAYER)
        every { playerRepository.findByUserId(userId) } returns viewerPlayer
        every { sessionResultRepository.findByPlayerId(viewerPlayer.id) } returns listOf(viewerResult)
        every { sessionResultRepository.findBySessionIds(setOf(sharedSession)) } returns listOf(viewerResult, rivalSharedResult)
        every { playerRepository.findByIds(setOf(viewerPlayer.id, rivalPlayer.id)) } returns listOf(viewerPlayer, rivalPlayer)

        val leaderboard = service.getLeaderboard(LeaderboardMetric.NET_PROFIT, userId, limit = 10)

        assertEquals(2, leaderboard.entries.size)
        assertEquals(viewerPlayer.id.toString(), leaderboard.entries.first().playerId)
        assertEquals("PLN 10.00", leaderboard.entries.first().valueFormatted)
        assertEquals(rivalPlayer.id.toString(), leaderboard.entries[1].playerId)
        assertEquals("-PLN 5.00", leaderboard.entries[1].valueFormatted)
        assertEquals(2, leaderboard.totalEntries)
        assertEquals(viewerPlayer.id.toString(), leaderboard.currentUserEntry?.playerId)

        verify(exactly = 1) { sessionResultRepository.findByPlayerId(viewerPlayer.id) }
        verify(exactly = 0) { sessionResultRepository.findByPlayerId(rivalPlayer.id) }
        verify(exactly = 0) { playerRepository.findAll(false) }
        verify(exactly = 1) { sessionResultRepository.findBySessionIds(setOf(sharedSession)) }
    }

    @Test
    fun `admin user sees global stats`() {
        val userId = UserId.generate()
        val adminUser = user(userId, UserRole.ADMIN)
        val playerOne = player("One")
        val playerTwo = player("Two")

        every { userRepository.findById(userId) } returns adminUser
        every { playerRepository.findByUserId(userId) } returns null
        every { playerRepository.findAll(false) } returns listOf(playerOne, playerTwo)
        every { sessionResultRepository.findByPlayerId(playerOne.id) } returns
            listOf(sessionResult(GameSessionId.generate(), playerOne.id, 1_000, 2_000))
        every { sessionResultRepository.findByPlayerId(playerTwo.id) } returns
            listOf(sessionResult(GameSessionId.generate(), playerTwo.id, 1_000, 1_500))

        val leaderboard = service.getLeaderboard(LeaderboardMetric.NET_PROFIT, userId, limit = 10)

        assertEquals(2, leaderboard.entries.size)
        assertTrue(leaderboard.entries.map { it.playerId }.containsAll(listOf(playerOne.id.toString(), playerTwo.id.toString())))
        assertEquals(2, leaderboard.totalEntries)
        assertEquals(null, leaderboard.currentUserEntry)

        verify(exactly = 1) { playerRepository.findAll(false) }
        verify(exactly = 0) { sessionResultRepository.findBySessionIds(any()) }
    }

    @Test
    fun `casual player without shared sessions gets empty leaderboard`() {
        val userId = UserId.generate()
        val viewerPlayer = player("Viewer", userId)

        every { userRepository.findById(userId) } returns user(userId, UserRole.CASUAL_PLAYER)
        every { playerRepository.findByUserId(userId) } returns viewerPlayer
        every { sessionResultRepository.findByPlayerId(viewerPlayer.id) } returns emptyList()

        val leaderboard = service.getLeaderboard(LeaderboardMetric.NET_PROFIT, userId, limit = 10)

        assertEquals(0, leaderboard.entries.size)
        assertEquals(0, leaderboard.totalEntries)
        assertEquals(null, leaderboard.currentUserEntry)

        verify(exactly = 0) { sessionResultRepository.findBySessionIds(any()) }
    }

    private fun player(
        name: String,
        userId: UserId? = null,
        id: PlayerId = PlayerId.generate(),
    ): Player {
        val now = LocalDateTime.now()
        return Player(
            id = id,
            name = PlayerName(name),
            avatarUrl = null,
            userId = userId,
            isActive = true,
            createdAt = now,
            updatedAt = now,
        )
    }

    private fun sessionResult(
        sessionId: GameSessionId,
        playerId: PlayerId,
        buyIn: Long,
        cashOut: Long,
    ): SessionResult {
        val now = LocalDateTime.now()
        return SessionResult(
            id = SessionResultId.generate(),
            sessionId = sessionId,
            playerId = playerId,
            buyIn = Money.ofCents(buyIn),
            cashOut = Money.ofCents(cashOut),
            notes = null,
            createdAt = now,
            updatedAt = now,
        )
    }

    private fun user(
        userId: UserId,
        role: UserRole,
    ): User {
        val now = LocalDateTime.now()
        return User(
            id = userId,
            email = Email("${role.name.lowercase()}@example.com"),
            passwordHash = HashedPassword("hash"),
            name = "User ${role.name}",
            role = role,
            avatarUrl = null,
            createdAt = now,
            updatedAt = now,
        )
    }
}
