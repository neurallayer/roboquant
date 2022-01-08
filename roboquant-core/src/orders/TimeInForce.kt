/*
 * Copyright 2021 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.roboquant.orders

import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Time in force (TiF) allows to put an expiration and fill policy on an order. It determined how long an order remains
 * active before it is executed or expires. There are two aspects that can determine if an order expires:
 *
 * - How much time has passed since it was first placed.
 * - Is the order completely filled or net yet
 *
 */
interface TimeInForce {

    /**
     * Is the order expired given the current time (now) and when it was originally placed and the fill in comparison
     * to total fill
     *
     * @param time
     * @return
     */
    fun isExpired(order: Order, time: Instant, fill: Double, total: Double): Boolean

}


/**
 * Good Till Cancelled policy. The order will remain active until fully filled or is cancelled.
 *
 * In practice, most brokers allow such order to remain active for 90 days max. That is also what is
 * used a default value for this implementation
 *
 * @constructor Create new GTC tif
 */
class GTC(private val maxDays: Int = 90) : TimeInForce {

    /**
     * @see TimeInForce.isExpired
     *
     */
    override fun isExpired(order: Order, time: Instant, fill: Double, total: Double): Boolean {
        if (time == order.placed) return false
        val max = order.placed.plus(maxDays.toLong(), ChronoUnit.DAYS)
        return time > max
    }

    override fun toString(): String {
        return "GTC"
    }

}


/**
 * Good Till Date policy. The order will remain active until fully filled or a specified date
 *
 * @property date
 * @constructor Create new GTD tif
 */
class GTD(private val date: Instant) : TimeInForce {

    /**
     * @see TimeInForce.isExpired
     *
     */
    override fun isExpired(order: Order, time: Instant, fill: Double, total: Double) = time > date

    override fun toString(): String {
        return "GTD($date)"
    }
}


/**
 * Immediate or Cancel (IOC) policy. An immediate or cancel order is an order to buy or sell an asset that attempts
 * to execute all or part immediately and then cancels any unfilled portion of the order.
 *
 * @constructor Create new IOC tif
 */
class IOC : TimeInForce {

    /**
     *
     */
    override fun isExpired(order: Order, time: Instant, fill: Double, total: Double) = time > order.placed

    override fun toString(): String {
        return "IOC"
    }
}

/**
 * A DAY order is a stipulation placed on an order to a broker to execute a trade at a specific price
 * that expires at the end of the trading day if it is not completed.
 *
 * @constructor Create new DAY tif
 */
class DAY : TimeInForce {

    /**
     *
     */
    override fun isExpired(order: Order, time: Instant, fill: Double, total: Double): Boolean {
        val exchange = order.asset.exchange
        return !exchange.sameDay(order.placed, time)
    }

    override fun toString(): String {
        return "DAY"
    }

}


/**
 * Fill or Kill (FOK) policy. A Fill or Kill policy is to be executed immediately at the market or a specified price
 * or canceled if not filled.
 *
 * @constructor Create new FOK tif
 */
class FOK : TimeInForce {

    /**
     * @see TimeInForce.isExpired
     *
     */
    override fun isExpired(order: Order, time: Instant, fill: Double, total: Double): Boolean {
        return fill != 0.0 && fill != total
    }

    override fun toString(): String {
        return "FOK"
    }
}