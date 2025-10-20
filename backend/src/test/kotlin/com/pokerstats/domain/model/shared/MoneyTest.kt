package com.pokerstats.domain.model.shared

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class MoneyTest {
    @Test
    fun `should create money from units`() {
        val money = Money.ofUnits(100)
        assertEquals(10000L, money.amountInCents)
    }

    @Test
    fun `should create money from cents`() {
        val money = Money.ofCents(5000)
        assertEquals(5000L, money.amountInCents)
    }

    @Test
    fun `should create money from decimal`() {
        val money = Money.ofDecimal(123.45)
        assertEquals(12345L, money.amountInCents)
    }

    @Test
    fun `should add money correctly`() {
        val money1 = Money.ofUnits(100)
        val money2 = Money.ofUnits(50)
        val result = money1 + money2

        assertEquals(15000L, result.amountInCents)
    }

    @Test
    fun `should subtract money correctly`() {
        val money1 = Money.ofUnits(100)
        val money2 = Money.ofUnits(30)
        val result = money1 - money2

        assertEquals(7000L, result.amountInCents)
    }

    @Test
    fun `should compare money correctly`() {
        val money1 = Money.ofUnits(100)
        val money2 = Money.ofUnits(50)

        assertTrue(money1 > money2)
        assertFalse(money1 < money2)
        assertTrue(money2 < money1)
    }

    @Test
    fun `should reject negative amounts`() {
        assertThrows<IllegalArgumentException> {
            Money.ofUnits(-10)
        }

        assertThrows<IllegalArgumentException> {
            Money.ofCents(-100)
        }

        assertThrows<IllegalArgumentException> {
            Money.ofDecimal(-5.0)
        }
    }

    @Test
    fun `should identify positive negative and zero`() {
        val positive = Money.ofUnits(10)
        val zero = Money.ZERO

        assertTrue(positive.isPositive())
        assertFalse(positive.isNegative())
        assertTrue(zero.isZero())
    }

    @Test
    fun `should convert to decimal correctly`() {
        val money = Money.ofCents(12345)
        assertEquals(123.45, money.toDecimal(), 0.01)
    }
}
