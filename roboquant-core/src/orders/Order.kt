package org.roboquant.orders

import org.roboquant.brokers.sim.Execution
import org.roboquant.common.Asset
import java.time.Instant

/**
 * Order is an instruction for a broker to initiate a certain action. Within roboquant it is the
 * [policy][org.roboquant.policies.Policy] that generates the orders.
 *
 * An order can cover a wide variety of use cases:
 *
 * - a new order (perhaps the most common use case), ranging from simple market orders to advanced order types
 * - cancellation of an existing order
 * - update of an existing order
 *
 * Please note it depends on the broker implementation which order types are supported.
 *
 */
abstract class Order(val asset: Asset) : Cloneable {

    /**
     * Unique ID across orders. When a JVM is (re)started, the ID reset again.
     */
    var id = getId().toString()
        private set

    /**
     * What is the status of this order
     */
    var status = OrderStatus.INITIAL


    /**
     * When was the order first placed. This timestamp is used during simulation to deal with time-in-force policies.
     * Before the order is actually placed, this attribute will contain the value `Instant.MIN`
     */
    var placed: Instant = Instant.MIN
        protected set

    /**
     * Last known price
     */
    var price: Double = Double.NaN
        protected set

    /**
     * If the order is not yet placed, place it now.
     *
     * @param now
     */
    protected fun place(price:Double, now: Instant) {
        if (placed == Instant.MIN) placed = now
        this.price = price
    }

    companion object {
        private var ID = 0L

        fun getId(): Long {
            synchronized(ID) {
                return ID++
            }
        }
    }

    public override fun clone(): Order {
        throw NotImplementedError("Concrete subclasses of Order need to override clone() method")
    }

    /**
     * Get the value of the order given the provided price. Used to see if there is enough buying power to place this order.
     * The default returns 0.0, which means no (known) value.
     *
     * @param price
     */
    open fun getValue(price: Double = this.price) = 0.0


    /**
     * Execute the order given the provided price and time. Any subclass of Order will need to implement
     * this method and return a list of [Execution]
     *
     * @param price
     * @param now
     * @return
     */
    abstract fun execute(price: Double, now: Instant) : List<Execution>


    /**
     * Copy current state into the passed object. This is used in the clone function of the subclasses
     *
     * @param result
     */
    protected fun copyTo(result: Order)  {
        result.id = id
        result.placed = placed
        result.status = status
    }

}

