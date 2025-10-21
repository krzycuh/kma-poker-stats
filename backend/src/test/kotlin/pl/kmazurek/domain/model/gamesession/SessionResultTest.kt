package pl.kmazurek.domain.model.gamesession

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import pl.kmazurek.domain.model.player.PlayerId
import pl.kmazurek.domain.model.shared.Money

class SessionResultTest {
    @Test
    fun `should create session result with valid data`() {
        // Given
        val sessionId = GameSessionId.generate()
        val playerId = PlayerId.generate()
        val buyIn = Money.ofUnits(100)
        val cashOut = Money.ofUnits(150)

        // When
        val result =
            SessionResult.create(
                sessionId = sessionId,
                playerId = playerId,
                buyIn = buyIn,
                cashOut = cashOut,
            )

        // Then
        result.id shouldNotBe null
        result.sessionId shouldBe sessionId
        result.playerId shouldBe playerId
        result.buyIn shouldBe buyIn
        result.cashOut shouldBe cashOut
    }

    @Test
    fun `should calculate positive profit correctly`() {
        // Given
        val result =
            SessionResult.create(
                sessionId = GameSessionId.generate(),
                playerId = PlayerId.generate(),
                buyIn = Money.ofUnits(100),
                cashOut = Money.ofUnits(150),
            )

        // When
        val profit = result.profit()

        // Then
        profit shouldBe Money.ofUnits(50)
        result.isWinning() shouldBe true
        result.isBreakEven() shouldBe false
        result.isLosing() shouldBe false
    }

    @Test
    fun `should calculate negative profit correctly`() {
        // Given
        val result =
            SessionResult.create(
                sessionId = GameSessionId.generate(),
                playerId = PlayerId.generate(),
                buyIn = Money.ofUnits(100),
                cashOut = Money.ofUnits(75),
            )

        // When
        val profit = result.profit()

        // Then
        profit shouldBe Money.ofUnits(-25)
        result.isWinning() shouldBe false
        result.isBreakEven() shouldBe false
        result.isLosing() shouldBe true
    }

    @Test
    fun `should identify break-even result`() {
        // Given
        val result =
            SessionResult.create(
                sessionId = GameSessionId.generate(),
                playerId = PlayerId.generate(),
                buyIn = Money.ofUnits(100),
                cashOut = Money.ofUnits(100),
            )

        // When/Then
        result.profit() shouldBe Money.ZERO
        result.isBreakEven() shouldBe true
        result.isWinning() shouldBe false
        result.isLosing() shouldBe false
    }

    @Test
    fun `should reject negative buy-in in session result`() {
        // When/Then - While Money allows negative values for profit/loss,
        // SessionResult validation should prevent negative buy-in
        shouldThrow<IllegalArgumentException> {
            SessionResult.create(
                sessionId = GameSessionId.generate(),
                playerId = PlayerId.generate(),
                buyIn = Money.ofUnits(-10),
                cashOut = Money.ofUnits(100),
            )
        }
    }

    @Test
    fun `should reject negative cash-out in session result`() {
        // When/Then - While Money allows negative values for profit/loss,
        // SessionResult validation should prevent negative cash-out
        shouldThrow<IllegalArgumentException> {
            SessionResult.create(
                sessionId = GameSessionId.generate(),
                playerId = PlayerId.generate(),
                buyIn = Money.ofUnits(100),
                cashOut = Money.ofUnits(-10),
            )
        }
    }

    @Test
    fun `should allow zero buy-in and cash-out`() {
        // When
        val result =
            SessionResult.create(
                sessionId = GameSessionId.generate(),
                playerId = PlayerId.generate(),
                buyIn = Money.ZERO,
                cashOut = Money.ZERO,
            )

        // Then
        result.buyIn shouldBe Money.ZERO
        result.cashOut shouldBe Money.ZERO
        result.isBreakEven() shouldBe true
    }

    @Test
    fun `should update result successfully`() {
        // Given
        val result =
            SessionResult.create(
                sessionId = GameSessionId.generate(),
                playerId = PlayerId.generate(),
                buyIn = Money.ofUnits(100),
                cashOut = Money.ofUnits(150),
                notes = "Old notes",
            )
        val newBuyIn = Money.ofUnits(200)
        val newCashOut = Money.ofUnits(250)
        val newNotes = "New notes"

        // When
        val updated =
            result.update(
                buyIn = newBuyIn,
                cashOut = newCashOut,
                notes = newNotes,
            )

        // Then
        updated.buyIn shouldBe newBuyIn
        updated.cashOut shouldBe newCashOut
        updated.notes shouldBe newNotes
        updated.profit() shouldBe Money.ofUnits(50)
    }

    @Test
    fun `should handle large profit amounts`() {
        // Given
        val result =
            SessionResult.create(
                sessionId = GameSessionId.generate(),
                playerId = PlayerId.generate(),
                buyIn = Money.ofUnits(1000),
                cashOut = Money.ofUnits(5000),
            )

        // When
        val profit = result.profit()

        // Then
        profit shouldBe Money.ofUnits(4000)
        result.isWinning() shouldBe true
    }

    @Test
    fun `should handle large loss amounts`() {
        // Given
        val result =
            SessionResult.create(
                sessionId = GameSessionId.generate(),
                playerId = PlayerId.generate(),
                buyIn = Money.ofUnits(5000),
                cashOut = Money.ofUnits(500),
            )

        // When
        val profit = result.profit()

        // Then
        profit shouldBe Money.ofUnits(-4500)
        result.isLosing() shouldBe true
    }
}
