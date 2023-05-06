/*
 * Copyright 2020-2023 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.roboquant.jupyter.dataframe

/**
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.toDataFrame
import org.roboquant.brokers.Position
import org.roboquant.brokers.Trade
import org.roboquant.orders.OrderState

@JvmName("positionsDataFrame")
fun Iterable<Position>.toDataFrame() : DataFrame<Position> {

    return this.toDataFrame {

        "symbol" from { it.asset.symbol }
        "currency" from {it.asset.currency.currencyCode }
        "exposure" from { it.exposure.toBigDecimal() }
        "marketValue" from { it.marketValue.toBigDecimal() }
        "totalCost" from { it.totalCost.toBigDecimal() }
        "pnl" from { it.unrealizedPNL.toBigDecimal() }

        properties() {
            exclude(Position::asset)
            exclude(Position::exposure)
            exclude(Position::marketValue)
            exclude(Position::totalCost)
            exclude(Position::unrealizedPNL)
            exclude(Position::open)
            exclude(Position::closed)
        }
    }

}

@JvmName("orderStateDataFrame")
fun Iterable<OrderState>.toDataFrame() : DataFrame<OrderState> {

    return this.toDataFrame {

        "symbol" from { it.asset.symbol }
        "currency" from {it.asset.currency.currencyCode }
        "type" from { it.order.type }
        "info" from { it.order.info() }

        properties() {
            exclude(OrderState::asset)
            exclude(OrderState::order)
        }
    }

}

@JvmName("tradeDataFrame")
fun Iterable<Trade>.toDataFrame() : DataFrame<Trade> {

    return this.toDataFrame {

        "symbol" from { it.asset.symbol }
        "currency" from {it.asset.currency.currencyCode }
        "totalCost" from { it.totalCost.toBigDecimal() }

        properties() {
            exclude(Trade::asset)
            exclude(Trade::fee)
            exclude(Trade::pnl)
            exclude(Trade::totalCost)
        }
    }

}
*/