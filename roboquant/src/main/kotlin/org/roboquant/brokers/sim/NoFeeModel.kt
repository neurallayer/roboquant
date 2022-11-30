package org.roboquant.brokers.sim

import org.roboquant.brokers.sim.execution.Execution

/**
 * Fee model that adds no additional fee or commissions.
 */
class NoFeeModel : FeeModel {

    override fun calculate(execution: Execution): Double = 0.0

}