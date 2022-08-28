/*
 * Copyright 2020-2022 Neural Layer
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

package org.roboquant.brokers

import org.roboquant.common.Amount
import org.roboquant.common.Currency
import java.time.Instant

/**
 * Interface for all types of currency converters that will convert an amount from one currency to another currency.
 * The interface allows currency conversions to take the following aspects into account (next to the two currencies):
 *
 * - the time of the conversion
 * - the amount to be converted
 *
 * Often you won't be using this interface directly, but rather invoke [Amount.convert].
 *
 */
interface ExchangeRates {

    /**
     * Get the exchange rate required to convert a monetary [amount] in [to] another currency at  specific moment
     * in [time].
     *
     * It depends on the implementation if all parameters are also actually used by the underlying algorithm. If a
     * conversion cannot be performed due to incorrect or missing configuration, it is expected to throw an exception.
     */
    fun getRate(amount: Amount, to: Currency, time: Instant): Double

    /**
     * Convert an [amount] into [to] a different currency. This default implementation uses the [getRate] to
     * calculate the exchange rate to use for this conversion.
     */
    fun convert(amount: Amount, to: Currency, time: Instant): Amount {
        val rate = getRate(amount, to, time)
        return Amount(to, amount.value * rate )
    }

}
