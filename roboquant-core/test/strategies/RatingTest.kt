package org.roboquant.strategies

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue



internal class RatingTest {

    @Test
    fun isPositive() {
        val rating = Rating.BUY
        assertTrue(rating.isPositive())
    }

    @Test
    fun isNegative() {
        val rating = Rating.SELL
        assertTrue(rating.isNegative())
    }

    @Test
    fun inverse() {
        val rating = Rating.SELL
        assertEquals(rating.inverse(), Rating.BUY)
    }

    @Test
    fun conflicts() {
        val rating = Rating.SELL
        assertTrue(rating.conflicts(Rating.BUY))
    }

    @Test
    fun getValue() {
        val rating = Rating.SELL
        assertEquals(-2, rating.value)
    }
}