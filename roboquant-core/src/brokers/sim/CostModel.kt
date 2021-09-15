package org.roboquant.brokers.sim

import org.roboquant.common.Cash
import org.roboquant.feeds.PriceAction
import org.roboquant.orders.Order
import java.lang.Double.max
import kotlin.math.absoluteValue
import kotlin.math.min

/**
 * Calculate the cost of an execution
 *
 */
interface CostModel {

    fun calculate(order: Order, execution: Execution, price: PriceAction): Pair<Double, Double>
}

/**
 * Default cost model, using a fixed percentage slippage expressed in basis points and no additional commission fee.
 *
 * @property slippage, default is 10 bips
 * @constructor Create new Default cost model
 */
class DefaultCostModel(private val slippage: Int = 10) : CostModel {

    override fun calculate(order: Order, execution: Execution, price: PriceAction): Pair<Double, Double> {
        val fee = 0.0
        val correction = if (execution.quantity > 0) 1.0 + slippage / 10_000.0 else 1.0 - slippage / 10_000.0
        val cost = execution.price * correction * execution.size()
        return Pair(cost, fee)
    }

}


/**
 * Cost model, using a fixed percentage slippage expressed in basis points and additional commission fee. The commission
 * fee is also in expressed in bips but has a minimum and maximum amount (for each currency used).
 *
 *
 * @property slippage, slippage in bips
 * @property fee, fee in bips
 *
 * @constructor Create empty Default cost model
 */
class CommissionBasedCostModel(
    private val slippage: Int = 5,
    private val fee: Int = 10,
    private val minimumAmount: Cash = Cash(),
    private val maximumAmount: Cash = Cash()
) : CostModel {

    override fun calculate(order: Order, execution: Execution, price: PriceAction): Pair<Double, Double> {
        val correction = if (execution.quantity > 0) 1.0 + slippage / 10_000.0 else 1.0 - slippage / 10_000.0
        val cost = execution.price * correction * execution.size()

        val currency = order.asset.currency
        var fee = cost.absoluteValue * (fee/10_000.0)
        fee = max(fee, minimumAmount.getAmount(currency))
        val maxAmount = maximumAmount.getAmount(currency)
        if (maxAmount != 0.0) fee = min(fee, maxAmount)
        return Pair(cost, fee)
    }

}