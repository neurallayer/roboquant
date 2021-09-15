package org.roboquant.orders

import org.roboquant.brokers.sim.Execution
import java.time.Instant


/**
 * Instruction to update an SimpleOrder. It is up to the broker implementation to translate the updated order to the correct
 * message, so it can be processed.
 *
 * Typically, only a part of an open order can be updated, like the limit price of a limit order. For many other
 * types of changes, an order needs to be cancelled and a new one needs to be created.
 **
 * @property originalOrder
 * @property updateOrder
 * @constructor Create empty Order update
 */
class UpdateOrder<T : SingleOrder>(val originalOrder: T, val updateOrder: T) : Order(originalOrder.asset) {

    init {
        require(originalOrder.asset == updateOrder.asset)
    }

    override fun execute(price: Double, now: Instant) : List<Execution> {
        place(price, now)

        when {
            originalOrder.status.closed -> status = OrderStatus.REJECTED
            updateOrder.quantity < originalOrder.fill -> status = OrderStatus.REJECTED
            else -> {
                originalOrder.quantity = updateOrder.quantity
            }
        }
        return listOf()
    }

    override fun getValue(price: Double): Double {
        return updateOrder.getValue(price)
    }

}