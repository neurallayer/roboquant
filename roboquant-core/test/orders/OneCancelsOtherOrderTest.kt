package org.roboquant.orders

import org.roboquant.TestData
import java.time.Instant
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OneCancelsOtherOrderTest {

    @Test
    fun test() {
        val asset = TestData.usStock()
        val order = OneCancelsOtherOrder(
            LimitOrder(asset, 100.0, 10.9),
            LimitOrder(asset, 200.0, 11.1)
        )
        assertTrue(order.first is LimitOrder)
        order.execute(11.0, Instant.now())

        assertFalse(order.first.executed)
        assertTrue(order.second.executed)
        assertTrue(order.first.status.aborted)
        assertEquals(OrderStatus.COMPLETED, order.second.status)

    }

}