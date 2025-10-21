package pl.kmazurek.domain.model.gamesession

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import pl.kmazurek.domain.model.shared.Money
import pl.kmazurek.domain.model.user.UserId
import java.time.LocalDateTime

class GameSessionTest {
    @Test
    fun `should create a new game session with valid data`() {
        // Given
        val startTime = LocalDateTime.now()
        val location = Location("John's House")
        val gameType = GameType.TEXAS_HOLDEM
        val minBuyIn = Money.ofUnits(50)
        val userId = UserId.generate()

        // When
        val session = GameSession.create(
            startTime = startTime,
            location = location,
            gameType = gameType,
            minBuyIn = minBuyIn,
            createdByUserId = userId,
        )

        // Then
        session.id shouldNotBe null
        session.startTime shouldBe startTime
        session.location shouldBe location
        session.gameType shouldBe gameType
        session.minBuyIn shouldBe minBuyIn
        session.createdByUserId shouldBe userId
        session.isDeleted shouldBe false
        session.isActive() shouldBe true
    }

    @Test
    fun `should reject negative minimum buy-in in game session`() {
        // When/Then - While Money allows negative values for profit/loss,
        // GameSession validation should prevent negative min buy-in
        shouldThrow<IllegalArgumentException> {
            GameSession.create(
                startTime = LocalDateTime.now(),
                location = Location("Test"),
                gameType = GameType.TEXAS_HOLDEM,
                minBuyIn = Money.ofUnits(-10),
            )
        }
    }

    @Test
    fun `should reject end time before start time`() {
        // Given
        val startTime = LocalDateTime.now()
        val endTime = startTime.minusHours(1)

        // When/Then
        shouldThrow<IllegalArgumentException> {
            GameSession.create(
                startTime = startTime,
                endTime = endTime,
                location = Location("Home"),
                gameType = GameType.TEXAS_HOLDEM,
                minBuyIn = Money.ofUnits(50),
            )
        }
    }

    @Test
    fun `should end session successfully`() {
        // Given
        val session = GameSession.create(
            startTime = LocalDateTime.now().minusHours(2),
            location = Location("Home"),
            gameType = GameType.TEXAS_HOLDEM,
            minBuyIn = Money.ofUnits(50),
        )
        val endTime = LocalDateTime.now()

        // When
        val endedSession = session.end(endTime)

        // Then
        endedSession.endTime shouldBe endTime
        endedSession.hasEnded() shouldBe true
        endedSession.isActive() shouldBe false
    }

    @Test
    fun `should reject ending session that already ended`() {
        // Given
        val session = GameSession.create(
            startTime = LocalDateTime.now().minusHours(2),
            location = Location("Home"),
            gameType = GameType.TEXAS_HOLDEM,
            minBuyIn = Money.ofUnits(50),
        ).end(LocalDateTime.now().minusHours(1))

        // When/Then
        shouldThrow<IllegalArgumentException> {
            session.end(LocalDateTime.now())
        }
    }

    @Test
    fun `should calculate session duration correctly`() {
        // Given
        val startTime = LocalDateTime.now().minusHours(3)
        val endTime = LocalDateTime.now()
        val session = GameSession.create(
            startTime = startTime,
            endTime = endTime,
            location = Location("Home"),
            gameType = GameType.TEXAS_HOLDEM,
            minBuyIn = Money.ofUnits(50),
        )

        // When
        val duration = session.duration()

        // Then
        duration shouldNotBe null
        duration!! shouldBe 180 // 3 hours = 180 minutes
    }

    @Test
    fun `should return null duration for active session`() {
        // Given
        val session = GameSession.create(
            startTime = LocalDateTime.now(),
            location = Location("Home"),
            gameType = GameType.TEXAS_HOLDEM,
            minBuyIn = Money.ofUnits(50),
        )

        // When
        val duration = session.duration()

        // Then
        duration shouldBe null
    }

    @Test
    fun `should update session successfully`() {
        // Given
        val session = GameSession.create(
            startTime = LocalDateTime.now(),
            location = Location("Old Location"),
            gameType = GameType.TEXAS_HOLDEM,
            minBuyIn = Money.ofUnits(50),
        )
        val newLocation = Location("New Location")
        val newMinBuyIn = Money.ofUnits(100)

        // When
        val updated = session.update(
            startTime = session.startTime,
            location = newLocation,
            gameType = GameType.OMAHA,
            minBuyIn = newMinBuyIn,
        )

        // Then
        updated.location shouldBe newLocation
        updated.gameType shouldBe GameType.OMAHA
        updated.minBuyIn shouldBe newMinBuyIn
    }

    @Test
    fun `should soft delete session`() {
        // Given
        val session = GameSession.create(
            startTime = LocalDateTime.now(),
            location = Location("Home"),
            gameType = GameType.TEXAS_HOLDEM,
            minBuyIn = Money.ofUnits(50),
        )

        // When
        val deleted = session.delete()

        // Then
        deleted.isDeleted shouldBe true
        deleted.isActive() shouldBe false
    }

    @Test
    fun `should reject deleting already deleted session`() {
        // Given
        val session = GameSession.create(
            startTime = LocalDateTime.now(),
            location = Location("Home"),
            gameType = GameType.TEXAS_HOLDEM,
            minBuyIn = Money.ofUnits(50),
        ).delete()

        // When/Then
        shouldThrow<IllegalArgumentException> {
            session.delete()
        }
    }

    @Test
    fun `should restore deleted session`() {
        // Given
        val session = GameSession.create(
            startTime = LocalDateTime.now(),
            location = Location("Home"),
            gameType = GameType.TEXAS_HOLDEM,
            minBuyIn = Money.ofUnits(50),
        ).delete()

        // When
        val restored = session.restore()

        // Then
        restored.isDeleted shouldBe false
        restored.isActive() shouldBe true
    }

    @Test
    fun `should reject restoring non-deleted session`() {
        // Given
        val session = GameSession.create(
            startTime = LocalDateTime.now(),
            location = Location("Home"),
            gameType = GameType.TEXAS_HOLDEM,
            minBuyIn = Money.ofUnits(50),
        )

        // When/Then
        shouldThrow<IllegalArgumentException> {
            session.restore()
        }
    }
}
