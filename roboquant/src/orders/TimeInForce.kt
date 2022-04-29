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

/**
 * Time in force (TiF) allows to put an expiration or fill policy on an order. It determines how long an order remains
 * active before it expires. There are two aspects that can determine if an order expires:
 *
 * - How much time has passed since it was first placed.
 * - Is the order completely filled or not yet
 *
 * When an order expires, the status is typically set [OrderStatus.EXPIRED] to indicate such event occurred.
 *
 */
interface TimeInForce


/**
 * Good Till Cancelled policy. The order will remain active until fully filled or is cancelled.
 *
 * In practice, most brokers allow such order to remain active for 60-90 days max. 90 [maxDays] is what is
 * used as a default value for this implementation.
 *
 * @constructor Create new GTC tif
 */
class GTC(val maxDays: Int = 90) : TimeInForce {

    override fun toString(): String = "GTC"

}


/**
 * Good Till Date policy. The order will remain active until fully filled or a specified date.
 *
 * @property date
 * @constructor Create new GTD tif
 */
class GTD(val date: Instant) : TimeInForce {

    override fun toString(): String = "GTD($date)"

}


/**
 * Immediate or Cancel (IOC) policy. An immediate or cancel order is an order to buy or sell an asset that attempts
 * to execute all or part immediately and then cancels any unfilled portion of the order.
 *
 * @constructor Create new IOC tif
 */
class IOC : TimeInForce {

    override fun toString(): String = "IOC"

}

/**
 * A DAY order is a stipulation placed on an order to a broker to execute a trade at a specific price
 * that expires at the end of the trading day if it is not completed. The definition of a day is determined by the
 * exchange the asset is traded on.
 *
 * @constructor Create new DAY tif
 */
class DAY : TimeInForce {

    override fun toString(): String = "DAY"

}


/**
 * Fill or Kill (FOK) policy. A Fill or Kill policy is to be executed immediately at the market or a specified price
 * or canceled if not filled.
 *
 * @constructor Create new FOK tif
 */
class FOK : TimeInForce {

    override fun toString(): String = "FOK"

}