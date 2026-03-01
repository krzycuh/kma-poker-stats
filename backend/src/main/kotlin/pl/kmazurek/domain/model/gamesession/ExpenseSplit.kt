package pl.kmazurek.domain.model.gamesession

import pl.kmazurek.domain.model.player.PlayerId
import pl.kmazurek.domain.model.shared.Money

/**
 * Value object representing the result of splitting session expenses among players.
 * Calculated dynamically, not persisted.
 *
 * Rounding: floor division, remainder cents distributed to first N players (sorted by ID).
 */
data class ExpenseSplit(
    val totalExpenses: Money,
    val perPlayerShare: Money,
    val playerCount: Int,
    val playerBalances: Map<PlayerId, PlayerBalance>,
) {
    data class PlayerBalance(
        val playerId: PlayerId,
        val amountPaid: Money,
        val amountOwed: Money,
        val balance: Money,
    )

    companion object {
        fun calculate(
            expenses: List<SessionExpense>,
            activePlayerIds: List<PlayerId>,
        ): ExpenseSplit {
            if (activePlayerIds.isEmpty() || expenses.isEmpty()) {
                return ExpenseSplit(
                    totalExpenses = Money.ZERO,
                    perPlayerShare = Money.ZERO,
                    playerCount = activePlayerIds.size,
                    playerBalances =
                        activePlayerIds.associateWith { playerId ->
                            PlayerBalance(
                                playerId = playerId,
                                amountPaid = Money.ZERO,
                                amountOwed = Money.ZERO,
                                balance = Money.ZERO,
                            )
                        },
                )
            }

            val totalCents = expenses.sumOf { it.amount.amountInCents }
            val playerCount = activePlayerIds.size
            val baseCentsPerPlayer = totalCents / playerCount
            val remainderCents = (totalCents % playerCount).toInt()

            // Sort player IDs for deterministic remainder distribution
            val sortedPlayerIds = activePlayerIds.sortedBy { it.value }

            // Calculate how much each player paid
            val paidByPlayer = mutableMapOf<PlayerId, Long>()
            for (expense in expenses) {
                paidByPlayer[expense.payerPlayerId] =
                    (paidByPlayer[expense.payerPlayerId] ?: 0L) + expense.amount.amountInCents
            }

            // Calculate balances
            val balances =
                sortedPlayerIds.mapIndexed { index, playerId ->
                    val owedCents = baseCentsPerPlayer + if (index < remainderCents) 1 else 0
                    val paidCents = paidByPlayer[playerId] ?: 0L
                    val balanceCents = paidCents - owedCents

                    playerId to
                        PlayerBalance(
                            playerId = playerId,
                            amountPaid = Money.ofCents(paidCents),
                            amountOwed = Money.ofCents(owedCents),
                            balance = Money.ofCents(balanceCents),
                        )
                }.toMap()

            return ExpenseSplit(
                totalExpenses = Money.ofCents(totalCents),
                perPlayerShare = Money.ofCents(baseCentsPerPlayer),
                playerCount = playerCount,
                playerBalances = balances,
            )
        }
    }
}
