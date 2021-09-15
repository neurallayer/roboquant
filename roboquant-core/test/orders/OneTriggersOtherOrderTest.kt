package org.roboquant.orders

import org.roboquant.TestData
import java.time.Instant
import org.junit.Test
import kotlin.test.assertTrue

internal class OneTriggersOtherOrderTest {

    @Test
    fun test() {
        val asset = TestData.usStock()
        val order = OneTriggersOtherOrder(
            MarketOrder(asset, 100.0),
            MarketOrder(asset, 200.0)
        )
        assertTrue(order.first is MarketOrder)
        order.execute(100.0, Instant.now())

    }

}