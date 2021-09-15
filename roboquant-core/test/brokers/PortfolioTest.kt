package org.roboquant.brokers

import org.junit.Test
import kotlin.test.*
import org.roboquant.TestData

internal class PortfolioTest {

    @Test
    fun testUpdatePortfolio() {
        val portfolio = Portfolio()
        val c = TestData.usStock()
        val position = Position(c, 100.0, 10.0)
        portfolio.updatePosition(position)
        assertEquals(100.0, portfolio.getPosition(c).quantity)
        assertEquals(10.0, portfolio.getPosition(c).cost)


        val position2 = Position(c, 100.0, 10.0)
        portfolio.updatePosition(position2)
        assertEquals(200.0, portfolio.getPosition(c).quantity)
        assertEquals(10.0, portfolio.getPosition(c).cost)

        val position3 = Position(c, -100.0, 10.0)
        portfolio.updatePosition(position3)
        assertEquals(100.0, portfolio.getPosition(c).quantity)
        assertEquals(10.0, portfolio.getPosition(c).cost)

        assertEquals(portfolio.positions.size, portfolio.longPositions.size + portfolio.shortPositions.size)

        val s = portfolio.summary()
        assertTrue(s.toString().isNotEmpty())

    }


}