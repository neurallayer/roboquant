package org.roboquant.brokers.sim.execution

import org.roboquant.brokers.Account
import org.roboquant.orders.Order
import org.roboquant.orders.OrderState
import org.roboquant.orders.OrderStatus
import java.time.Instant

/**
 * Mutable Order State implements [OrderState] and keeps track of the execution state of an order. After an order is
 * placed at a broker, the OrderState informs you what is happening with that order. The most common way to access this
 * information is through [Account.openOrders] and [Account.closedOrders]
 *
 * This is an open class and can be extended with more advanced implementations.
 *
 * @property order The underlying order, which is read-only
 * @property status The latest status
 * @property openedAt When was the order first placed is [Instant.MIN] until set
 * @property closedAt When was the order closed, default is [Instant.MAX] until set
 * @constructor Create new instance of OrderState
 */
class MutableOrderState (
    override val order: Order,
    override var status: OrderStatus = OrderStatus.INITIAL,
    override var openedAt: Instant = Instant.MIN,
    override var closedAt: Instant = Instant.MAX
) : OrderState, Cloneable {
    
    /**
     * Update the state
     */
    fun update(time: Instant, newStatus: OrderStatus = OrderStatus.ACCEPTED)  {
        if (status.closed) return
        if (openedAt == Instant.MIN) openedAt = time
        status = newStatus
        if (status.closed) closedAt = time
    }

    /**
     * Cancel the order, return true if successful, false otherwise
     */
    fun cancel(time: Instant) : Boolean {
        return if (status.open) {
            update(time, OrderStatus.CANCELLED)
            true
        } else {
            false
        }
    }

    /**
     * Get a clone that won't change
     */
    public override fun clone(): MutableOrderState {
        return MutableOrderState(order, status, openedAt, closedAt)
    }


}