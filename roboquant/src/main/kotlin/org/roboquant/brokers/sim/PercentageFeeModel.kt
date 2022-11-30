package org.roboquant.brokers.sim

import org.roboquant.brokers.sim.execution.Execution
import kotlin.math.absoluteValue

/**
 * Default fee model, using a fixed percentage fee of total absolute value of the execution.
 *
 * @property feePercentage fee as a percentage of total execution cost, 0.01 = 1%. Default is 0.0
 * @constructor Create a new percentage fee model
 */
class PercentageFeeModel(
    private val feePercentage: Double = 0.0,
) : FeeModel {

    override fun calculate(execution: Execution): Double {
        return execution.value.absoluteValue * feePercentage
    }

}