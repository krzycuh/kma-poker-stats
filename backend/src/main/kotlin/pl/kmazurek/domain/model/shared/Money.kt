package pl.kmazurek.domain.model.shared

/**
 * Value Object representing money amount.
 * Stores amount in cents to avoid floating point precision issues.
 * Immutable by design.
 */
@JvmInline
value class Money private constructor(val amountInCents: Long) {
    operator fun plus(other: Money) = Money(amountInCents + other.amountInCents)

    operator fun minus(other: Money) = Money(amountInCents - other.amountInCents)

    operator fun compareTo(other: Money) = amountInCents.compareTo(other.amountInCents)

    operator fun times(factor: Int) = Money(amountInCents * factor)

    fun isPositive() = amountInCents > 0

    fun isNegative() = amountInCents < 0

    fun isZero() = amountInCents == 0L

    fun toDecimal() = amountInCents / 100.0

    companion object {
        val ZERO = Money(0)

        fun ofCents(cents: Long): Money {
            require(cents >= 0) { "Money amount must be non-negative, got $cents cents" }
            return Money(cents)
        }

        fun ofUnits(units: Int): Money {
            require(units >= 0) { "Money amount must be non-negative, got $units units" }
            return Money(units.toLong() * 100)
        }

        fun ofDecimal(amount: Double): Money {
            require(amount >= 0) { "Money amount must be non-negative, got $amount" }
            return Money((amount * 100).toLong())
        }
    }
}
