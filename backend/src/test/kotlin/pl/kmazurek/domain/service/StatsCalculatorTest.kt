package pl.kmazurek.domain.service

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import pl.kmazurek.domain.model.gamesession.GameSession
import pl.kmazurek.domain.model.gamesession.GameType
import pl.kmazurek.domain.model.gamesession.Location
import pl.kmazurek.domain.model.gamesession.SessionResult
import pl.kmazurek.domain.model.player.PlayerId
import pl.kmazurek.domain.model.shared.Money
import java.time.LocalDateTime

class StatsCalculatorTest {
    private val calculator = StatsCalculator()

    @Test
    fun `should calculate total sessions for player`() {
        // Given
        val playerId = PlayerId.generate()
        val results = listOf(
            createResult(playerId, buyIn = 100, cashOut = 150),
            createResult(playerId, buyIn = 200, cashOut = 180),
        )

        // When
        val stats = calculator.calculatePlayerStats(playerId, results)

        // Then
        stats.totalSessions shouldBe 2
    }

    @Test
    fun `should calculate net profit correctly`() {
        // Given
        val playerId = PlayerId.generate()
        val results = listOf(
            createResult(playerId, buyIn = 100, cashOut = 150), // +50
            createResult(playerId, buyIn = 200, cashOut = 180), // -20
            createResult(playerId, buyIn = 100, cashOut = 200), // +100
        )

        // When
        val stats = calculator.calculatePlayerStats(playerId, results)

        // Then
        // Net profit: +50 -20 +100 = 130 units = 13000 cents
        stats.netProfit.amountInCents shouldBe 13000
    }

    @Test
    fun `should calculate total buy-in correctly`() {
        // Given
        val playerId = PlayerId.generate()
        val results = listOf(
            createResult(playerId, buyIn = 100, cashOut = 150),
            createResult(playerId, buyIn = 200, cashOut = 180),
        )

        // When
        val stats = calculator.calculatePlayerStats(playerId, results)

        // Then
        stats.totalBuyIn.amountInCents shouldBe 30000 // 100 + 200 units = 30000 cents
    }

    @Test
    fun `should calculate total cash-out correctly`() {
        // Given
        val playerId = PlayerId.generate()
        val results = listOf(
            createResult(playerId, buyIn = 100, cashOut = 150),
            createResult(playerId, buyIn = 200, cashOut = 180),
        )

        // When
        val stats = calculator.calculatePlayerStats(playerId, results)

        // Then
        stats.totalCashOut.amountInCents shouldBe 33000 // 150 + 180 units = 33000 cents
    }

    @Test
    fun `should calculate ROI correctly`() {
        // Given
        val playerId = PlayerId.generate()
        val results = listOf(
            createResult(playerId, buyIn = 100, cashOut = 150), // +50
            createResult(playerId, buyIn = 100, cashOut = 100), // 0
        )

        // When
        val stats = calculator.calculatePlayerStats(playerId, results)

        // Then
        // ROI = (netProfit / totalBuyIn) * 100 = (50 / 200) * 100 = 25%
        stats.roi shouldBe 25.0
    }

    @Test
    fun `should calculate win rate correctly`() {
        // Given
        val playerId = PlayerId.generate()
        val results = listOf(
            createResult(playerId, buyIn = 100, cashOut = 150), // Win
            createResult(playerId, buyIn = 100, cashOut = 50), // Loss
            createResult(playerId, buyIn = 100, cashOut = 200), // Win
            createResult(playerId, buyIn = 100, cashOut = 100), // Break-even
        )

        // When
        val stats = calculator.calculatePlayerStats(playerId, results)

        // Then
        // Win rate = 2 wins / 4 sessions = 0.5 = 50%
        stats.winRate shouldBe 50.0
    }

    @Test
    fun `should find biggest win`() {
        // Given
        val playerId = PlayerId.generate()
        val results = listOf(
            createResult(playerId, buyIn = 100, cashOut = 150), // +50
            createResult(playerId, buyIn = 100, cashOut = 500), // +400 (biggest win)
            createResult(playerId, buyIn = 100, cashOut = 200), // +100
        )

        // When
        val stats = calculator.calculatePlayerStats(playerId, results)

        // Then
        stats.biggestWin.amountInCents shouldBe 40000 // 400 units = 40000 cents
    }

    @Test
    fun `should find biggest loss`() {
        // Given
        val playerId = PlayerId.generate()
        val results = listOf(
            createResult(playerId, buyIn = 100, cashOut = 50), // -50
            createResult(playerId, buyIn = 500, cashOut = 0), // -500 (biggest loss)
            createResult(playerId, buyIn = 100, cashOut = 20), // -80
        )

        // When
        val stats = calculator.calculatePlayerStats(playerId, results)

        // Then
        // Biggest loss is -500 cents = -50000 cents (in our internal representation)
        stats.biggestLoss.amountInCents shouldBe -50000
    }

    @Test
    fun `should calculate average session profit`() {
        // Given
        val playerId = PlayerId.generate()
        val results = listOf(
            createResult(playerId, buyIn = 100, cashOut = 150), // +50
            createResult(playerId, buyIn = 100, cashOut = 50), // -50
            createResult(playerId, buyIn = 100, cashOut = 200), // +100
            createResult(playerId, buyIn = 100, cashOut = 100), // 0
        )

        // When
        val stats = calculator.calculatePlayerStats(playerId, results)

        // Then
        // Average = (50 - 50 + 100 + 0) / 4 = 100 / 4 = 25 units = 2500 cents
        stats.averageSessionProfit.amountInCents shouldBe 2500
    }

    @Test
    fun `should handle player with no sessions`() {
        // Given
        val playerId = PlayerId.generate()
        val results = emptyList<SessionResult>()

        // When
        val stats = calculator.calculatePlayerStats(playerId, results)

        // Then
        stats.totalSessions shouldBe 0
        stats.netProfit.amountInCents shouldBe 0
        stats.totalBuyIn.amountInCents shouldBe 0
        stats.totalCashOut.amountInCents shouldBe 0
        stats.roi shouldBe 0.0
        stats.winRate shouldBe 0.0
        stats.biggestWin.amountInCents shouldBe 0
        stats.biggestLoss.amountInCents shouldBe 0
        stats.averageSessionProfit.amountInCents shouldBe 0
    }

    @Test
    fun `should handle all losing sessions`() {
        // Given
        val playerId = PlayerId.generate()
        val results = listOf(
            createResult(playerId, buyIn = 100, cashOut = 50),
            createResult(playerId, buyIn = 100, cashOut = 80),
        )

        // When
        val stats = calculator.calculatePlayerStats(playerId, results)

        // Then
        // Net profit: -50 -20 = -70 units = -7000 cents
        stats.netProfit.amountInCents shouldBe -7000
        stats.winRate shouldBe 0.0
        stats.roi shouldBe -35.0 // -70 / 200 = -0.35 = -35%
    }

    @Test
    fun `should calculate current streak correctly for winning streak`() {
        // Given
        val playerId = PlayerId.generate()
        val results = listOf(
            createResult(playerId, buyIn = 100, cashOut = 50), // Loss
            createResult(playerId, buyIn = 100, cashOut = 150), // Win
            createResult(playerId, buyIn = 100, cashOut = 200), // Win
            createResult(playerId, buyIn = 100, cashOut = 180), // Win
        )

        // When
        val stats = calculator.calculatePlayerStats(playerId, results)

        // Then
        stats.currentStreak shouldBe 3 // 3 wins in a row
    }

    @Test
    fun `should calculate current streak correctly for losing streak`() {
        // Given
        val playerId = PlayerId.generate()
        val results = listOf(
            createResult(playerId, buyIn = 100, cashOut = 200), // Win
            createResult(playerId, buyIn = 100, cashOut = 50), // Loss
            createResult(playerId, buyIn = 100, cashOut = 20), // Loss
        )

        // When
        val stats = calculator.calculatePlayerStats(playerId, results)

        // Then
        stats.currentStreak shouldBe -2 // 2 losses in a row
    }

    // Helper methods

    private fun createResult(
        playerId: PlayerId,
        buyIn: Int,
        cashOut: Int,
    ): SessionResult {
        return SessionResult.create(
            sessionId = pl.kmazurek.domain.model.gamesession.GameSessionId.generate(),
            playerId = playerId,
            buyIn = Money.ofUnits(buyIn),
            cashOut = Money.ofUnits(cashOut),
        )
    }
}
