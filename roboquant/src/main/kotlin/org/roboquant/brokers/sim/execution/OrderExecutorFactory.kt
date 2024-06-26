/*
 * Copyright 2020-2024 Neural Layer
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
package org.roboquant.brokers.sim.execution

import org.roboquant.orders.*
import kotlin.reflect.KClass



object OrderExecutorFactory {



    /**
     * All the registered [OrderExecutorFactory]
     */
    val factories = mutableMapOf<KClass<*>, (Order) -> OrderExecutor>()

    init {
        // register all the default included order handlers

        // Single Instruction types
        register<MarketOrder> { MarketOrderExecutor(it) }
        register<LimitOrder> { LimitOrderExecutor(it) }
        register<StopLimitOrder> { StopLimitOrderExecutor(it) }
        register<StopOrder> { StopOrderExecutor(it) }
        register<TrailLimitOrder> { TrailLimitOrderExecutor(it) }
        register<TrailOrder> { TrailOrderExecutor(it) }

        // Combined order types
        register<BracketOrder> { BracketOrderExecutor(it) }
        register<OCOOrder> { OCOOrderExecutor(it) }
        register<OTOOrder> { OTOOrderExecutor(it) }

    }

    /**
     * Return the order executor for the provided [order]. This will throw an exception if no [OrderExecutorFactory]
     * is registered for the order::class.
     */
    fun getExecutor(order: Order): OrderExecutor {
        val fn = factories.getValue(order::class)
        return fn(order)
    }

    /**
     * Unregister the order executor factory for order type [T]
     */
    inline fun <reified T : Instruction> unregister() {
        factories.remove(T::class)
    }

    inline fun <reified T : Order> register(noinline factory: (T) -> OrderExecutor) {
        @Suppress("UNCHECKED_CAST")
        factories[T::class] = factory as (Order) -> OrderExecutor
    }




}
